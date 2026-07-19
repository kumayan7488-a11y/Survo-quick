package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val password: String,
    val name: String,
    val surname: String,
    val dob: String,
    val avatarId: Int, // Index for selected predefined avatar
    val coins: Int = 100 // Default sign-up bonus of 100 coins
)

@Entity(tableName = "admin_config")
data class AdminConfig(
    @PrimaryKey val id: Int = 1,
    val geminiApiKey: String = "",
    val cpxSurveyLink: String = "https://cpx-research.com/index.php?member_id=",
    val exchangeCoins: Int = 10,
    val exchangeInr: Int = 10,
    val adminEmail: String = "kumayan7488@gmail.com",
    val adminPassword: String = "admin123"
)

@Entity(tableName = "redeem_options")
data class RedeemOption(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val coinsRequired: Int,
    val paymentType: String, // "UPI", "Paytm", "Amazon Voucher", etc.
    val inputHint: String // "Enter your UPI ID", "Enter Mobile Number", etc.
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val coinsAmount: Int,
    val inrAmount: Float,
    val redeemOptionTitle: String,
    val details: String, // UPI ID, phone number etc.
    val status: String, // "PENDING", "APPROVED"
    val code: String = "", // Transaction reference / voucher code to copy
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val coinsReward: Int,
    val category: String = "Hot Offer" // "Hot Offer", "Survey", etc.
)

@Entity(tableName = "user_task_statuses", primaryKeys = ["userEmail", "taskId"])
data class UserTaskStatus(
    val userEmail: String,
    val taskId: Int,
    val status: String // "COMPLETED", "FAILED"
)
