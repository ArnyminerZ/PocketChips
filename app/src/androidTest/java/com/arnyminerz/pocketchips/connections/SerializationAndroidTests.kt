package com.arnyminerz.pocketchips.connections

import android.content.Intent
import com.arnyminerz.pocketchips.communications.Serializable
import com.arnyminerz.pocketchips.communications.SerializedObject
import com.arnyminerz.pocketchips.communications.Serializer
import com.arnyminerz.pocketchips.communications.deserialize
import com.arnyminerz.pocketchips.communications.getSerializedExtra
import com.arnyminerz.pocketchips.communications.putExtra
import com.arnyminerz.pocketchips.communications.serialized
import org.junit.Assert.assertEquals
import org.junit.Test

class SerializationAndroidTests {
    data class TestSerializable(
        val key: String = "sample"
    ): Serializable {
        companion object: Serializer<TestSerializable> {
            override fun fromSerializedObject(serialized: SerializedObject): TestSerializable =
                TestSerializable(serialized["key"])
        }

        override fun serialize(): SerializedObject = serialized("key" to "sample")
    }

    @Test
    fun test_Intent_getSerializedExtra() {
        val serializable = TestSerializable()
        val intent = Intent().apply {
            putExtra("extra", serializable.serialize().bytes)
        }
        val extra: TestSerializable? = intent.getSerializedExtra<TestSerializable, TestSerializable.Companion>("extra")

        assertEquals(serializable, extra)
    }

    @Test
    fun test_Intent_putExtra_Serializable() {
        val serializable = TestSerializable()
        val intent = Intent().apply {
            putExtra("extra", serializable)
        }
        val extra = intent.getByteArrayExtra("extra")
            ?.deserialize()
            ?.let { TestSerializable.fromSerializedObject(it) }

        assertEquals(serializable, extra)
    }
}
