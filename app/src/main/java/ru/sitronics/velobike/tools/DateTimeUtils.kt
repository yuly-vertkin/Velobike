package ru.sitronics.velobike.tools

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getDateTimeStr(time: Long, pattern: String) : String =
    getDateTimeStr(Date(time), pattern)

fun getDateTimeStr(time: Date, pattern: String) : String {
    val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
    return timeFormat.format(time)
}

fun formatDateTimeStr(time: String, inputPattern: String, outputPattern: String) : String {
    var result = time
    val inputTimeFormat = SimpleDateFormat(inputPattern, Locale.getDefault())
    val outputTimeFormat = SimpleDateFormat(outputPattern, Locale.getDefault())

    try {
        inputTimeFormat.parse(time)?.let {
            result = outputTimeFormat.format(it)
        }
    } catch (_: ParseException) { }
    return result
}

fun getNumberStr(number: Float, pattern: String) : String {
    val formatter = NumberFormat.getInstance() as DecimalFormat
    formatter.applyPattern(pattern)
    return formatter.format(number)
}

