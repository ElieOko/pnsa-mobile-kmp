package app.partners.pnsa.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

object FlexibleBoolSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBool", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        return NullableFlexibleBoolSerializer.deserialize(decoder) ?: false
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}

object NullableFlexibleBoolSerializer : KSerializer<Boolean?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NullableFlexibleBool", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean? {
        val jsonDecoder = decoder as? JsonDecoder ?: return try {
            decoder.decodeBoolean()
        } catch (_: Exception) {
            null
        }
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> parseFlexibleBool(element)
            else -> null
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean?) {
        if (value == null) encoder.encodeNull() else encoder.encodeBoolean(value)
    }
}

fun parseFlexibleBool(raw: JsonPrimitive): Boolean? {
    if (raw.content.isBlank() || raw.content == "null") return null
    raw.booleanOrNull?.let { return it }
    raw.intOrNull?.let { return it != 0 }
    return when (raw.content.trim().lowercase()) {
        "1", "true", "yes", "oui" -> true
        "0", "false", "no", "non" -> false
        else -> null
    }
}
