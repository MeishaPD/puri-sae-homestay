package brawijaya.example.purisaehomestay.utils

import java.text.ParseException
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

        return try {
            getDateFormat().parse(dateString)
        } catch (e: ParseException) {
            null
        }
    }

    fun getMillisFromDate(dateString: String): Long? {
        return parseDate(dateString)?.time
    }

    fun isCheckOutBeforeCheckIn(checkInDate: String, checkOutDate: String): Boolean {
        if (checkInDate.isEmpty() || checkOutDate.isEmpty()) return false

        val checkIn = parseDate(checkInDate)
        val checkOut = parseDate(checkOutDate)

        if (checkIn == null || checkOut == null) return false

        return checkOut.before(checkIn)
    }

    fun formatDate(date: Date): String {
        return getDateFormat().format(date)
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
}