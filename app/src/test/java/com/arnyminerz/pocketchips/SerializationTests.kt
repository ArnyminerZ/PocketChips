package com.arnyminerz.pocketchips

import com.arnyminerz.pocketchips.communications.deserialize
import com.arnyminerz.pocketchips.communications.serialized
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SerializationTests {
    private fun assertInvalid(vararg data: Pair<String, String>) {
        assertThrows(IllegalArgumentException::class.java) {
            serialized(*data)
        }
    }

    @Test
    fun test_SerializedObject_forbidden() {
        // Check valid combination
        serialized("legal-key" to "legal-value")

        // Check invalid combinations
        assertInvalid("legal-key" to "illegal=value")
        assertInvalid("legal-key" to "illegal|value")
        assertInvalid("illegal=key" to "legal-value")
        assertInvalid("illegal|key" to "legal-value")
        assertInvalid("illegal=key" to "illegal=value")
        assertInvalid("illegal=key" to "illegal|value")
        assertInvalid("illegal|key" to "illegal=value")
        assertInvalid("illegal|key" to "illegal|value")
    }

    @Test
    fun test_SerializedObject_serialization() {
        val obj = serialized("key" to "value", "another" to "one")
        assertEquals(
            mapOf("key" to "value", "another" to "one"),
            obj.data
        )
    }

    @Test
    fun test_SerializedObject_bytes() {
        val obj = serialized("key" to "value", "another" to "one")
        assertEquals(
            "key=value|another=one",
            obj.bytes.decodeToString()
        )
    }

    @Test
    fun test_SerializedObject_equals() {
        val obj1 = serialized("key" to "value", "another" to "one")
        val obj2 = serialized("key" to "value", "another" to "one")
        val obj3 = serialized("key" to "value")
        val obj4 = serialized("another" to "one")

        assertEquals(obj1, obj1)
        assertEquals(obj1, obj2)

        assertNotEquals(obj1, obj3)
        assertNotEquals(obj1, obj4)
        assertNotEquals(obj2, obj3)
        assertNotEquals(obj2, obj3)
        assertNotEquals(obj3, obj4)
    }

    @Test
    fun test_SerializedObject_deserialization() {
        val obj = serialized("key" to "value", "another" to "one")
        val bytes = obj.bytes
        val deserialized = bytes.deserialize()
        assertEquals(obj, deserialized)
    }

    @Test
    fun test_SerializedObject_get() {
        val obj = serialized("key" to "value", "another" to "one")
        assertEquals("value", obj["key"])
        assertEquals("one", obj["another"])
        assertThrows(NoSuchElementException::class.java) { obj["invalid"] }
    }

    @Test
    fun test_SerializedObject_size() {
        val obj = serialized("key" to "value", "another" to "one")
        assertEquals(2, obj.size)
    }
}