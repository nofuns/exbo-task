package ru.nofun.exbotask;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            ExboTask exboTask = new ExboTask(
                    "data/task.json",
                    "data/file.json",
                    "data/items.csv"
            );
            exboTask.doFirstTask("data/result.json");
            exboTask.doSecondTask("data/result.xlsx");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}