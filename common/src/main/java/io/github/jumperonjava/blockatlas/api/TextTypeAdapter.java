package io.github.jumperonjava.blockatlas.api;

import com.google.gson.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.lang.reflect.Type;

public class TextTypeAdapter implements JsonSerializer<Text>, JsonDeserializer<MutableText> {

    @Override
    public MutableText deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return new Text.Serializer(DynamicRegistryManager.EMPTY).deserialize(jsonElement, type, jsonDeserializationContext);
    }

    @Override
    public JsonElement serialize(Text text, Type type, JsonSerializationContext jsonSerializationContext) {
        return new Text.Serializer(DynamicRegistryManager.EMPTY).serialize(text, type, jsonSerializationContext);
    }
}
