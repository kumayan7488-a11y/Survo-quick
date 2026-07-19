package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    // --- User Operations ---
    suspend fun getUserByEmail(email: String): User? = appDao.getUserByEmail(email)
    
    fun getUserByEmailFlow(email: String): Flow<User?> = appDao.getUserByEmailFlow(email)
    
    fun getAllUsersFlow(): Flow<List<User>> = appDao.getAllUsersFlow()
    
    suspend fun insertUser(user: User) = appDao.insertUser(user)
    
    suspend fun deleteUserByEmail(email: String) = appDao.deleteUserByEmail(email)

    suspend fun rewardUserCoins(email: String, rewardCoins: Int) {
        val user = appDao.getUserByEmail(email)
        if (user != null) {
            val updatedCoins = user.coins + rewardCoins
            appDao.updateUserCoins(email, updatedCoins)
        }
    }

    suspend fun setUserCoinsDirectly(email: String, newCoinBalance: Int) {
        appDao.updateUserCoins(email, newCoinBalance)
    }

    // --- Admin Config Operations ---
    suspend fun getAdminConfig(): AdminConfig {
        return appDao.getAdminConfig() ?: AdminConfig().also {
            appDao.insertAdminConfig(it)
        }
    }

    fun getAdminConfigFlow(): Flow<AdminConfig?> = appDao.getAdminConfigFlow()

    suspend fun saveAdminConfig(config: AdminConfig) {
        appDao.insertAdminConfig(config)
    }

    // --- Redeem Options Operations ---
    fun getAllRedeemOptions(): Flow<List<RedeemOption>> = appDao.getAllRedeemOptionsFlow()

    suspend fun addRedeemOption(option: RedeemOption) = appDao.insertRedeemOption(option)

    suspend fun deleteRedeemOption(option: RedeemOption) = appDao.deleteRedeemOption(option)

    // --- Transaction/Withdrawal Operations ---
    fun getAllTransactions(): Flow<List<Transaction>> = appDao.getAllTransactionsFlow()

    fun getTransactionsForUser(email: String): Flow<List<Transaction>> = appDao.getTransactionsForUserFlow(email)

    suspend fun requestWithdrawal(email: String, coins: Int, redeemOption: RedeemOption, details: String): Boolean {
        val user = appDao.getUserByEmail(email) ?: return false
        if (user.coins < coins) return false // Insufficient balance

        // Calculate INR based on current Exchange settings
        val config = getAdminConfig()
        val inrAmount = coins.toFloat() * config.exchangeInr / config.exchangeCoins

        // 1. Subtract coins from user
        appDao.updateUserCoins(email, user.coins - coins)

        // 2. Create withdrawal transaction
        val transaction = Transaction(
            userEmail = email,
            coinsAmount = coins,
            inrAmount = inrAmount,
            redeemOptionTitle = redeemOption.title,
            details = details,
            status = "PENDING"
        )
        appDao.insertTransaction(transaction)
        return true
    }

    suspend fun approveWithdrawal(transactionId: Int, code: String): Boolean {
        val transaction = appDao.getTransactionById(transactionId) ?: return false
        if (transaction.status == "PENDING") {
            val updatedTransaction = transaction.copy(
                status = "APPROVED",
                code = code
            )
            appDao.insertTransaction(updatedTransaction)
            return true
        }
        return false
    }

    // --- Task Operations ---
    fun getAllTasks(): Flow<List<Task>> = appDao.getAllTasksFlow()

    suspend fun addTask(task: Task) = appDao.insertTask(task)

    suspend fun deleteTask(task: Task) = appDao.deleteTask(task)

    // --- User Task Status Operations ---
    fun getTaskStatusesForUser(email: String): Flow<List<UserTaskStatus>> = appDao.getTaskStatusesForUserFlow(email)

    suspend fun submitTaskCompletion(email: String, taskId: Int, isSuccess: Boolean): Boolean {
        val user = appDao.getUserByEmail(email) ?: return false
        
        // Check if already completed or failed
        val existing = appDao.getTaskStatus(email, taskId)
        if (existing != null) return false // Already has a status

        val statusString = if (isSuccess) "COMPLETED" else "FAILED"
        val status = UserTaskStatus(userEmail = email, taskId = taskId, status = statusString)
        appDao.insertTaskStatus(status)

        // If completed, add coins reward to the user
        if (isSuccess) {
            val taskList = appDao.getAllTasksFlow().firstOrNull() ?: emptyList()
            val task = taskList.find { it.id == taskId }
            if (task != null) {
                appDao.updateUserCoins(email, user.coins + task.coinsReward)
            }
        }
        return true
    }
}
