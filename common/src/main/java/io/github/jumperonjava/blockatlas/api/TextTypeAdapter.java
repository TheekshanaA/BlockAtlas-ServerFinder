package io.github.jumperonjava.blockatlas.api;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.lang.reflect.Type;

public class TextTypeAdapter implements JsonSerializer<Text>, JsonDeserializer<MutableText> {

    @Override
    public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return TextCodecs.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                .result()
                .map(t -> (MutableText) t)
                .orElse(Text.literal(jsonElement.isJsonPrimitive() ? jsonElement.getAsString() : jsonElement.toString()));
    }

    @Override
    public JsonElement serialize(Text text, Type type, JsonSerializationContext jsonSerializationContext) {
        return TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text)
                .result()
                .orElse(new JsonPrimitive(text.getString()));
    }
}
