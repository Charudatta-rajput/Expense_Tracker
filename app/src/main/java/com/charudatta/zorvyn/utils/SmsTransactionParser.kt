package com.charudatta.zorvyn.utils

// ─── Parsed result ────────────────────────────────────────────────────────────

data class DetectedTransaction(
    val amount: Double,
    val type: String,        // "expense" or "income"
    val merchant: String?,   // extracted merchant/UPI name if available
    val rawSms: String
)

// ─── SMS Parser ───────────────────────────────────────────────────────────────

object SmsTransactionParser {

    // Matches: "debited", "deducted", "paid", "spent", "withdrawn" → expense
    //          "credited", "received", "deposited"                 → income
    private val DEBIT_KEYWORDS  = listOf("debited", "deducted", "paid", "spent", "withdrawn", "sent")
    private val CREDIT_KEYWORDS = listOf("credited", "received", "deposited", "refund")

    // Matches amounts like: Rs.500, Rs 500, INR 500, ₹500, ₹ 1,500.00
    private val AMOUNT_REGEX = Regex(
        """(?:rs\.?|inr|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Matches UPI/merchant: "to <name>", "at <name>", "VPA <name>"
    private val MERCHANT_REGEX = Regex(
        """(?:to|at|vpa)\s+([A-Za-z0-9@.\-_ ]{3,30})""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Returns a DetectedTransaction if the SMS looks like a bank transaction,
     * null otherwise.
     */
    fun parse(smsBody: String): DetectedTransaction? {
        val lower = smsBody.lowercase()

        // Must contain an amount
        val amountMatch = AMOUNT_REGEX.find(smsBody) ?: return null
        val amount = amountMatch.groupValues[1]
            .replace(",", "")
            .toDoubleOrNull() ?: return null

        // Must be above a trivial threshold (avoid OTP SMS with numbers)
        if (amount < 1.0) return null

        // Determine type
        val type = when {
            DEBIT_KEYWORDS.any  { lower.contains(it) } -> "expense"
            CREDIT_KEYWORDS.any { lower.contains(it) } -> "income"
            else -> return null   // not a transaction SMS
        }

        // Optional merchant
        val merchant = MERCHANT_REGEX.find(smsBody)?.groupValues?.get(1)?.trim()

        return DetectedTransaction(
            amount   = amount,
            type     = type,
            merchant = merchant,
            rawSms   = smsBody
        )
    }
}