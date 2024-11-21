package ru.nofun.exbotask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

// Класс для записи в .json
public class JsonWriter implements AutoCloseable {
    private final Gson gsonBuilder;
    private final FileWriter writer;

    JsonWriter(String filePath) throws IOException {
        writer = new FileWriter(filePath);
        // setPrettyPrinting() - добавляет табуляцию при записи.
        gsonBuilder = new GsonBuilder().setPrettyPrinting().create();
    }

    public <Type> void write(Map<String, Type> jsonObject) throws IOException {
        gsonBuilder.toJson(jsonObject, writer);
    }

    public void close() throws IOException {
        writer.close();
    }
}
