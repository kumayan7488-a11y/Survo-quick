package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Operations ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmailFlow(email: String): Flow<User?>

    @Query("SELECT * FROM users ORDER BY email ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET coins = :coins WHERE email = :email")
    suspend fun updateUserCoins(email: String, coins: Int)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)

    // --- Admin Config Operations ---
    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    suspend fun getAdminConfig(): AdminConfig?

    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    fun getAdminConfigFlow(): Flow<AdminConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminConfig(config: AdminConfig)

    // --- Redeem Options Operations ---
    @Query("SELECT * FROM redeem_options ORDER BY coinsRequired ASC")
    fun getAllRedeemOptionsFlow(): Flow<List<RedeemOption>>

    @Query("SELECT * FROM redeem_options WHERE id = :id LIMIT 1")
    suspend fun getRedeemOptionById(id: Int): RedeemOption?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemOption(option: RedeemOption)

    @Delete
    suspend fun deleteRedeemOption(option: RedeemOption)

    // --- Transaction Operations ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(email: String): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Int): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // --- Task Operations ---
    @Query("SELECT * FROM tasks ORDER BY coinsReward DESC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // --- User Task Status Operations ---
    @Query("SELECT * FROM user_task_statuses WHERE userEmail = :userEmail")
    fun getTaskStatusesForUserFlow(userEmail: String): Flow<List<UserTaskStatus>>

    @Query("SELECT * FROM user_task_statuses WHERE userEmail = :userEmail AND taskId = :taskId LIMIT 1")
    suspend fun getTaskStatus(userEmail: String, taskId: Int): UserTaskStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskStatus(status: UserTaskStatus)
}
