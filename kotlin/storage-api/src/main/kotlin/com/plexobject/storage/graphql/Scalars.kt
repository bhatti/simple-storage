package com.plexobject.storage.graphql

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType

object Scalars {
    val dateTime = GraphQLScalarType("DateTime", "DataTime scalar",
            object : Coercing<Any, String> {
                override fun serialize(input: Any): String {
                    // serialize the ZonedDateTime into string on the way out
                    return (input as ZonedDateTime).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }

                override fun parseValue(input: Any): Any {
                    return serialize(input)
                }

                override fun parseLiteral(input: Any): ZonedDateTime? {
                    // parse the string values coming in
                    return if (input is StringValue) {
                        ZonedDateTime.parse(input.value)
                    } else {
                        null
                    }
                }
            })
}
