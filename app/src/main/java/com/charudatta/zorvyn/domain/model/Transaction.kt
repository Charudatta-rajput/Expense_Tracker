package com.charudatta.zorvyn.domain.model

data class Transaction(
    val id: Int,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val note: String
)