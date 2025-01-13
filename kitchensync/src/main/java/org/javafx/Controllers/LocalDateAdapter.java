package org.javafx.Controllers;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParseException;

import java.time.LocalDate;

public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    @Override
    public JsonElement serialize(LocalDate src, java.lang.reflect.Type typeOfSrc, com.google.gson.JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public LocalDate deserialize(JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws JsonParseException {
        try {
            return LocalDate.parse(json.getAsString());
        } catch (Exception e) {
            throw new JsonParseException("Invalid date format for LocalDate: " + json.getAsString(), e);
        }
    }
}
