package com.ramitsuri.podcasts.network.serialization

import com.ramitsuri.podcasts.network.model.CategoryDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject

internal object CategoriesDeserializer :
    JsonTransformingSerializer<List<CategoryDto>>(ListSerializer(CategoryDto.serializer())) {
    override fun transformDeserialize(element: JsonElement): JsonArray {
        return JsonArray(
            (element as JsonObject).entries.map { (k, v) ->
                buildJsonObject {
                    put("id", JsonPrimitive(k.toInt()))
                    put("name", v)
                }
            },
        )
    }
}
