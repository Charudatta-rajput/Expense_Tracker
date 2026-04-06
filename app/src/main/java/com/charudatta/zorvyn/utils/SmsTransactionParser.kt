package com.charudatta.zorvyn.utils


data class DetectedTransaction(
    val amount: Double,
    val type: String,
    val merchant: String?,
    val rawSms: String
)

object SmsTransactionParser {


    private val DEBIT_KEYWORDS  = listOf("debited", "deducted", "paid", "spent", "withdrawn", "sent")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "deposited", "refund")


    private val AMOUNT_REGEX = Regex(
        """(?:rs\.?|inr|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    private val MERCHANT_REGEX = Regex(
        """(?:(?:to|from|by|at|vpa)\s+([A-Za-z0-9@.\-_& ]{2,40}?))\s*(?:on|via|for|using|ref|upi|a\/c|ac|${'$'})""",
        RegexOption.IGNORE_CASE
    )

    fun parse(smsBody: String): DetectedTransaction? {
        val lower = smsBody.lowercase()


        val amountMatch = AMOUNT_REGEX.find(smsBody) ?: return null
        val amount = amountMatch.groupValues[1]
            .replace(",", "")
            .toDoubleOrNull() ?: return null


        if (amount < 1.0) return null


        val type = when {
            DEBIT_KEYWORDS.any  { lower.contains(it) } -> "expense"
            CREDIT_KEYWORDS.any { lower.contains(it) } -> "income"
            else -> return null
        }


        val merchant = MERCHANT_REGEX.find(smsBody)?.groupValues?.get(1)?.trim()

        return DetectedTransaction(
            amount   = amount,
            type     = type,
            merchant = merchant,
            rawSms   = smsBody
        )
    }
}