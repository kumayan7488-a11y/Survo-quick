package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AppViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    // --- Authentication State ---
    private val _loggedInUserEmail = MutableStateFlow<String?>(null)
    val loggedInUserEmail: StateFlow<String?> = _loggedInUserEmail.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val loggedInUser: StateFlow<User?> = _loggedInUserEmail
        .flatMapLatest { email ->
            if (email != null) {
                repository.getUserByEmailFlow(email)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    private val _signupSuccess = MutableStateFlow(false)
    val signupSuccess: StateFlow<Boolean> = _signupSuccess.asStateFlow()

    var lastSignedUpEmail: String? = null
        private set

    // --- Admin Config State ---
    val adminConfig: StateFlow<AdminConfig?> = repository.getAdminConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Admin Users List State ---
    val allUsers: StateFlow<List<User>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Redeem Options State ---
    val redeemOptions: StateFlow<List<RedeemOption>> = repository.getAllRedeemOptions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Transactions State ---
    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userTransactions: StateFlow<List<Transaction>> = _loggedInUserEmail
        .flatMapLatest { email ->
            if (email != null) {
                repository.getTransactionsForUser(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Tasks State ---
    val allTasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userTaskStatuses: StateFlow<List<UserTaskStatus>> = _loggedInUserEmail
        .flatMapLatest { email ->
            if (email != null) {
                repository.getTaskStatusesForUser(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks that are NOT completed/failed by user
    val availableTasks: StateFlow<List<Task>> = combine(allTasks, userTaskStatuses) { tasks, statuses ->
        val completedIds = statuses.map { it.taskId }.toSet()
        tasks.filter { it.id !in completedIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Chat Help State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! Welcome to Survo Quick Help. How can I assist you today?", false))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()


    // --- Actions ---

    fun clearAuthStates() {
        _authError.value = null
        _authSuccess.value = false
        _signupSuccess.value = false
    }

    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearAuthStates()
            if (email.isBlank() || password.isBlank()) {
                _authError.value = "Email and Password cannot be empty."
                return@launch
            }

            // Check if Admin
            val currentConfig = repository.getAdminConfig()
            if (email.lowercase() == currentConfig.adminEmail.lowercase()) {
                if (password == currentConfig.adminPassword) {
                    _isAdmin.value = true
                    _loggedInUserEmail.value = currentConfig.adminEmail
                    _authSuccess.value = true
                } else {
                    _authError.value = "Incorrect password."
                }
                return@launch
            }

            // Normal User
            val user = repository.getUserByEmail(email)
            if (user == null) {
                _authError.value = "not signed up yet please signed up"
            } else if (user.password != password) {
                _authError.value = "Incorrect password."
            } else {
                _isAdmin.value = false
                _loggedInUserEmail.value = email
                _authSuccess.value = true
            }
        }
    }

    fun signup(email: String, password: String, name: String, surname: String, dob: String, avatarId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            clearAuthStates()
            if (email.isBlank() || password.isBlank() || name.isBlank() || surname.isBlank() || dob.isBlank()) {
                _authError.value = "All fields are required."
                return@launch
            }

            val currentConfig = repository.getAdminConfig()
            if (email.lowercase() == currentConfig.adminEmail.lowercase()) {
                _authError.value = "This email is not eligible for registration."
                return@launch
            }

            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                _authError.value = "Email is already registered. Please login."
                return@launch
            }

            val newUser = User(
                email = email,
                password = password,
                name = name,
                surname = surname,
                dob = dob,
                avatarId = avatarId,
                coins = 100 // Sign up bonus
            )
            repository.insertUser(newUser)
            lastSignedUpEmail = email
            _signupSuccess.value = true
        }
    }

    fun confirmSignup() {
        val email = lastSignedUpEmail ?: return
        _isAdmin.value = false
        _loggedInUserEmail.value = email
        _authSuccess.value = true
        _signupSuccess.value = false
    }

    fun forgotPassword(email: String, newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            clearAuthStates()
            if (email.isBlank() || newPassword.isBlank()) {
                _authError.value = "All fields are required."
                return@launch
            }

            val existingUser = repository.getUserByEmail(email)
            if (existingUser == null) {
                _authError.value = "User not registered."
                return@launch
            }

            val updatedUser = existingUser.copy(password = newPassword)
            repository.insertUser(updatedUser)
            _authSuccess.value = true
        }
    }

    fun logout() {
        _loggedInUserEmail.value = null
        _isAdmin.value = false
        clearAuthStates()
        // Clear chat
        _chatMessages.value = listOf(ChatMessage("Hello! Welcome to Survo Quick Help. How can I assist you today?", false))
    }

    fun deleteAccountPermanent() {
        viewModelScope.launch(Dispatchers.IO) {
            val email = _loggedInUserEmail.value
            if (email != null && !_isAdmin.value) {
                repository.deleteUserByEmail(email)
                logout()
            }
        }
    }

    fun updateProfile(name: String, surname: String, dob: String, avatarId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = _loggedInUserEmail.value
            if (email != null) {
                val user = repository.getUserByEmail(email)
                if (user != null) {
                    val updatedUser = user.copy(
                        name = name,
                        surname = surname,
                        dob = dob,
                        avatarId = avatarId
                    )
                    repository.insertUser(updatedUser)
                }
            }
        }
    }

    // --- Admin Actions ---

    fun rewardUserCoins(userEmail: String, coins: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.rewardUserCoins(userEmail, coins)
        }
    }

    fun saveAdminConfig(geminiApiKey: String, cpxSurveyLink: String, exchangeCoins: Int, exchangeInr: Int, adminEmail: String? = null, adminPassword: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = repository.getAdminConfig()
            val updatedConfig = current.copy(
                geminiApiKey = geminiApiKey,
                cpxSurveyLink = cpxSurveyLink,
                exchangeCoins = exchangeCoins,
                exchangeInr = exchangeInr,
                adminEmail = adminEmail ?: current.adminEmail,
                adminPassword = adminPassword ?: current.adminPassword
            )
            repository.saveAdminConfig(updatedConfig)
        }
    }

    fun addRedeemOption(title: String, coinsRequired: Int, paymentType: String, inputHint: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val option = RedeemOption(
                title = title,
                coinsRequired = coinsRequired,
                paymentType = paymentType,
                inputHint = inputHint
            )
            repository.addRedeemOption(option)
        }
    }

    fun deleteRedeemOption(option: RedeemOption) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRedeemOption(option)
        }
    }

    fun approveTransaction(transactionId: Int, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveWithdrawal(transactionId, code)
        }
    }

    fun adminAddTask(title: String, description: String, coinsReward: Int, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = Task(
                title = title,
                description = description,
                coinsReward = coinsReward,
                category = category
            )
            repository.addTask(task)
        }
    }

    fun adminDeleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }


    // --- User Actions ---

    fun completeTask(taskId: Int, isSuccess: Boolean, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = _loggedInUserEmail.value
            if (email != null) {
                val res = repository.submitTaskCompletion(email, taskId, isSuccess)
                viewModelScope.launch(Dispatchers.Main) {
                    onFinished(res)
                }
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    onFinished(false)
                }
            }
        }
    }

    fun requestWithdrawal(option: RedeemOption, details: String, onFinished: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = _loggedInUserEmail.value
            if (email != null) {
                val res = repository.requestWithdrawal(email, option.coinsRequired, option, details)
                viewModelScope.launch(Dispatchers.Main) {
                    onFinished(res)
                }
            } else {
                viewModelScope.launch(Dispatchers.Main) {
                    onFinished(false)
                }
            }
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text, true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isChatLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val configuredKey = adminConfig.value?.geminiApiKey ?: ""
            val apiKey = if (configuredKey.isNotBlank()) {
                configuredKey
            } else {
                com.example.BuildConfig.GEMINI_API_KEY
            }
            
            // Map history to Gemini API format (simple chat history conversion)
            val chatHistory = _chatMessages.value.dropLast(1).map { msg ->
                GeminiContent(parts = listOf(GeminiPart(text = msg.text)))
            }

            val replyText = GeminiClient.getHelpResponse(apiKey, text, chatHistory)
            
            viewModelScope.launch(Dispatchers.Main) {
                _chatMessages.value = _chatMessages.value + ChatMessage(replyText, false)
                _isChatLoading.value = false
            }
        }
    }
}

class AppViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            return AppViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
