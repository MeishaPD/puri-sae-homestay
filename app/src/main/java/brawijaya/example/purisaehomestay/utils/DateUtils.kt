package brawijaya.example.purisaehomestay.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: ParseException) {
            null
        }
    }

    fun getMillisFromDate(dateString: String): Long? {
        return parseDate(dateString)?.time
    }

    fun isCheckOutBeforeCheckIn(checkInDate: String, checkOutDate: String): Boolean {
        if (checkInDate.isNotEmpty() || checkOutDate.isEmpty()) return false

        val checkIn = parseDate(checkInDate)
        val checkOut = parseDate(checkOutDate)

        if (checkIn == null || checkOut == null) return false

        return checkOut.before(checkIn)
    }
}