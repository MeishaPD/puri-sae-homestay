package brawijaya.example.purisaehomestay.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

object DateUtils {
    private val dateFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        }
    }

    private fun getDateFormat(): SimpleDateFormat {
        return dateFormatThreadLocal.get()
    }

    fun parseDate(dateString: String): Date? {
        if (dateString.isEmpty()) return null

        return runCatching {
            getDateFormat().parse(dateString)
        }.getOrNull()
    }

    fun getMillisFromDate(dateString: String): Long? {
        return parseDate(dateString)?.time
    }

    fun formatDate(date: Date, pattern: String = "dd/MM/yyyy"): String {
        val dateFormat = SimpleDateFormat(pattern, Locale("id", "ID"))
        return dateFormat.format(date)
    }

    fun formatDate(year: Int, month: Int, dayOfMonth: Int): String {
        return formatDate(getDateFromComponents(year, month, dayOfMonth))
    }

    fun getDateFromComponents(year: Int, month: Int, dayOfMonth: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return calendar.time
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    fun isValidCheckOutDate(checkInDate: String, checkOutDate: String): Boolean {
        if (checkInDate.isEmpty() || checkOutDate.isEmpty()) return false

        val checkIn = parseDate(checkInDate)
        val checkOut = parseDate(checkOutDate)

        if (checkIn == null || checkOut == null) return false

        return checkOut.after(checkIn)
    }

    fun calculateNights(checkInDate: String, checkOutDate: String): Int {
        return try {
            val checkIn = parseDate(checkInDate)
            val checkOut = parseDate(checkOutDate)
            if (checkIn != null && checkOut != null) {
                val diffInMillis = checkOut.time - checkIn.time
                (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            } else 1
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Normalize a date to start of day (00:00:00.000)
     */
    fun normalizeToStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Normalize a date to end of day (23:59:59.999)
     */
    fun normalizeToEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }

    /**
     * Check if current date is within the given date range (inclusive)
     * Normalizes dates for proper comparison
     */
    fun isDateInRange(startDate: Date, endDate: Date, dateToCheck: Date = getCurrentDate()): Boolean {
        val normalizedStart = normalizeToStartOfDay(startDate)
        val normalizedEnd = normalizeToEndOfDay(endDate)
        val normalizedCheck = normalizeToStartOfDay(dateToCheck)

        return !normalizedCheck.before(normalizedStart) && !normalizedCheck.after(normalizedEnd)
    }

    /**
     * Check if a date is before another date (day-level comparison)
     */
    fun isDateBefore(date1: Date, date2: Date): Boolean {
        val normalized1 = normalizeToStartOfDay(date1)
        val normalized2 = normalizeToStartOfDay(date2)
        return normalized1.before(normalized2)
    }

    /**
     * Check if a date is after another date (day-level comparison)
     */
    fun isDateAfter(date1: Date, date2: Date): Boolean {
        val normalized1 = normalizeToStartOfDay(date1)
        val normalized2 = normalizeToStartOfDay(date2)
        return normalized1.after(normalized2)
    }

    /**
     * Check if two dates are the same day
     */
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val normalized1 = normalizeToStartOfDay(date1)
        val normalized2 = normalizeToStartOfDay(date2)
        return normalized1.time == normalized2.time
    }
}