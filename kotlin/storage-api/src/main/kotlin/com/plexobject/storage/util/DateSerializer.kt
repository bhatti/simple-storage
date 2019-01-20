package com.plexobject.storage.util

import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.text.SimpleDateFormat
import java.util.*


class DateSerializer @JvmOverloads constructor(t: Class<Date>? = null) : StdSerializer<Date>(t) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: Date, gen: JsonGenerator, arg2: SerializerProvider) {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        gen.writeString(fmt.format(value))
    }
}