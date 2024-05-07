package ru.sitronics.velobike.data.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateDeserializer: JsonDeserializer<Date?> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement, typeOF: Type, context: JsonDeserializationContext
    ): Date? {
        for (format in dateFormats) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(jsonElement.asString)
            } catch (_: ParseException) { }
        }
        return null
//        throw JsonParseException(
//            "Unparseable date: \"" + jsonElement.asString +
//            "\". Supported formats: " + dateFormats.joinToString { x -> x }
//        )
    }

    companion object{
        val dateFormats = listOf("yyyy-MM-dd'T'HH:mm", "dd.hh:mm:ss")
    }
}