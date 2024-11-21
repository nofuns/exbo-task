package ru.nofun.exbotask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

// Класс для парсинга .json
public class JsonParser {
    public Gson gson = new Gson();

    //
    public <Type> Map<String, Type> parse(String filename, TypeToken typeToken) throws IOException {
        Map<String, Type> data;
        JsonReader reader = new JsonReader(new FileReader(filename));
        data = gson.fromJson(reader, typeToken.getType());

        return data;
    }
}
