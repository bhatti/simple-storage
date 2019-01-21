package com.plexobject.storage.graphql

import java.time.ZoneId
import java.time.ZonedDateTime

import org.junit.Assert
import org.junit.Test

import graphql.language.StringValue

class ScalarsTest {
    @Test
    fun testSerialize() {
        Assert.assertEquals("2018-02-10T18:30:30-08:00", Scalars.dateTime.coercing.serialize(ZonedDateTime.of(2018, 2, 10, 18, 30, 30, 0, ZoneId.of("America/Los_Angeles"))))
    }

    @Test
    fun testParseValue() {
        Assert.assertEquals("2018-02-10T18:30:30-08:00", Scalars.dateTime.coercing.parseValue(ZonedDateTime.of(2018, 2, 10, 18, 30, 30, 0, ZoneId.of("America/Los_Angeles"))))
    }

    @Test
    fun testParseLiteral() {
        Assert.assertNotNull(Scalars.dateTime.coercing.parseLiteral(StringValue("2018-02-10T18:30:30-08:00")))
        Assert.assertNull(Scalars.dateTime.coercing.parseLiteral("xxxx"))
    }

}