package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        AdminConfig::class,
        RedeemOption::class,
        Transaction::class,
        Task::class,
        UserTaskStatus::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "survo_quick_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.appDao()
                    
                    // Seed initial Admin Config
                    dao.insertAdminConfig(AdminConfig())

                    // Seed default Tasks
                    dao.insertTask(Task(title = "Install GPay & Pay 1 INR", description = "Install Google Pay from Play Store and make your first transaction of minimum 1 INR.", coinsReward = 40, category = "Hot Offer"))
                    dao.insertTask(Task(title = "Share Survo Quick App", description = "Share this application with 5 friends on WhatsApp and invite them to join.", coinsReward = 20, category = "Daily Task"))
                    dao.insertTask(Task(title = "Complete Profile Details", description = "Fill out your profile details (Name, Surname, DOB, Avatar) in the Profile section.", coinsReward = 15, category = "Daily Task"))
                    dao.insertTask(Task(title = "Watch Sponsored Video", description = "Watch a short promotional video of 30 seconds entirely.", coinsReward = 10, category = "Video Reward"))
                    dao.insertTask(Task(title = "Daily Attendance Check-in", description = "Check-in daily to claim your standard check-in bonus.", coinsReward = 5, category = "Daily Task"))

                    // Seed default Redeem Options
                    dao.insertRedeemOption(RedeemOption(title = "Paytm Instant Cashout", coinsRequired = 100, paymentType = "Paytm Wallet", inputHint = "Enter registered Paytm phone number"))
                    dao.insertRedeemOption(RedeemOption(title = "UPI Instant Transfer", coinsRequired = 200, paymentType = "UPI Payment", inputHint = "Enter valid UPI ID (e.g., username@upi)"))
                    dao.insertRedeemOption(RedeemOption(title = "Amazon Pay Gift Card", coinsRequired = 500, paymentType = "Amazon Voucher", inputHint = "Enter your Email Address"))
                }
            }
        }
    }
}
