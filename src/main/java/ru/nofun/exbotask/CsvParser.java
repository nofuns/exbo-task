package ru.nofun.exbotask;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


// Класс для парсинга CSV таблиц
public class CsvParser implements Iterable<CSVRecord> {
    private CSVParser parser;
    private FileReader fileReader;

    // Возвращаем запись в виде List
    public List<String> toList(CSVRecord record) {
        List<String> list = new ArrayList<>();
        for (String line : record)
            list.add(line);

        return list;
    }

    CsvParser(String filename) throws IOException {
        this.fileReader = new FileReader(filename);
        parser = CSVParser.parse(fileReader, CSVFormat.DEFAULT);
    }

    // Реализуем интерфейс Iterable
    public Iterator<CSVRecord> iterator() {
        return this.parser.iterator();
    }

}