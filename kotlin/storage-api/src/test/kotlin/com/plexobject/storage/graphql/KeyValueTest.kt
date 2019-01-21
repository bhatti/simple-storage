package com.plexobject.storage.graphql

import org.junit.Assert
import org.junit.Test

class KeyValueTest {
    @Test
    fun testConstructor() {
        val (key, value) = KeyValue("key", "value")
        Assert.assertEquals("key", key)
        Assert.assertEquals("value", value)
    }

}
