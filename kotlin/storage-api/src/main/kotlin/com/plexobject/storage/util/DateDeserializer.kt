package com.plexobject.storage.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import java.io.IOException
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.text.SimpleDateFormat
import java.util.*


class DateDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Date>(vc) {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonparser: JsonParser, context: DeserializationContext): Date {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val date = jsonparser.getText()
        return fmt.parse(date)
    }
}