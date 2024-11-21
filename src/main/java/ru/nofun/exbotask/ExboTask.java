package ru.nofun.exbotask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

// Класс для хранения json-объекта из task.json
@Getter
@AllArgsConstructor
class TaskList {
    private String       list_name;
    private List<String> list;
}

// Класс для хранения json-объекта из file.json
@Getter
@AllArgsConstructor
class Task {
    private int      id;
    private String   reward;
    private int      weight;
}

// Класс для хранения данных из items.csv
@Getter
@AllArgsConstructor
class Reward {
    private int money;
    private int details;
    private int reputation;
}

// Класс для хранения данных, необходимых для решения Задания 1
@Getter
@AllArgsConstructor
class NamedReward {
    private int     money;
    private int     details;
    private int     reputation;
    private String  reward;
}


// Хранит данные для последующей записи в Excel
@Getter
@AllArgsConstructor
class TaskExcelRowData {
    private String  listName;
    private String  objectName;
    private String  rewardKey;
    private int     money;
    private int     details;
    private int     reputation;
    private int     isUsed;
}

public class ExboTask {

    Map<String, TaskList>   taskList;
    Map<String, Task>       taskData;
    Map<String, Reward>     rewards;


    ExboTask(String taskListPath, String taskDataPath, String rewardDataPath) throws IOException {
        // Парсим json и записываем в виде пар ключ/значение
        // gson для парсинга использует простые классы без конструктора с отрытыми полями, повторяющими структуру json
        // имена полей в этих классах должны совпадать с именами полей в .json
        taskList = parseJson(taskListPath, new TypeToken<Map<String, TaskList>>() {});
        taskData = parseJson(taskDataPath, new TypeToken<Map<String, Task>>() {});

        // Парсинг csv в Map<String, Reward> для удобства поиска значений
        rewards = parseCSV(rewardDataPath); //items.csv
    }

    // Парсим CSV
    private Map<String, Reward> parseCSV(String filePath) throws IOException {
        Map<String, Reward> parsedRewards = new HashMap<>();
        FileReader fileReader = new FileReader(filePath);
        CSVParser parser = CSVParser.parse(fileReader, CSVFormat.DEFAULT);

        // Итерируемся по записям CSV таблицы
        for (CSVRecord csvRecord : parser) {

            Reward reward = new Reward(
                    Integer.parseInt(csvRecord.get(1)), // money
                    Integer.parseInt(csvRecord.get(2)), // details
                    Integer.parseInt(csvRecord.get(3))  // reputation
            );

            parsedRewards.put(csvRecord.get(0), reward);
        }

        return parsedRewards;
    }

    // Парсим Json
    private <Type> Map<String, Type> parseJson(String filePath, TypeToken typeToken) throws IOException {
        Gson gson = new Gson();
        Map<String, Type> data;
        JsonReader reader = new JsonReader(new FileReader(filePath));
        data = gson.fromJson(reader, typeToken.getType());

        return data;
    }

    // Запись готовых данных в json
    // В данном случае generic метод, т.к. Gson пользуется специальными классами для записи в файл
    private <Type> void writeJson(String filePath, Map<String, Type> jsonObject) throws IOException {
        FileWriter writer = new FileWriter(filePath);
        Gson gsonBuilder = new GsonBuilder().setPrettyPrinting().create();

        gsonBuilder.toJson(jsonObject, writer);
    }

    // Запись готовых данных в Excel
    // Для удобства и упрощения кода создан класс ExcelFileWriter
    private void writeExcel(String resultPath, List<TaskExcelRowData> excelData) {
        try (XSSFWorkbook workbook      = new XSSFWorkbook();
             FileOutputStream fileOut   = new FileOutputStream(resultPath)) {

            XSSFSheet sheet             = workbook.createSheet();
            int currentRowIndex    = 0;

            for (TaskExcelRowData excelRow : excelData) {
                // Записываем данные в соответствующие ячейки
                // Создаем следующую строку в таблице
                Row currentRow         = sheet.createRow(currentRowIndex);
                currentRow.createCell(0).setCellValue(excelRow.getListName());
                currentRow.createCell(1).setCellValue(excelRow.getObjectName());
                currentRow.createCell(2).setCellValue(excelRow.getRewardKey());
                currentRow.createCell(3).setCellValue(excelRow.getMoney());
                currentRow.createCell(4).setCellValue(excelRow.getDetails());
                currentRow.createCell(5).setCellValue(excelRow.getReputation());
                currentRow.createCell(6).setCellValue(excelRow.getIsUsed());

                // Для создания новой строки в Excel
                currentRowIndex++;
            }

            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file: " + resultPath, e);
        }
    }

    ///////////////////////////////////////
    public void doTask1(String resultPath) {
        /*
        ЗАДАНИЕ 1.
        */

        // Отсеиваем повторяющиеся objectName, таким образом получаем контракты, которые точно используются в list в task.json
        Set<String> uniqueTasksFromList = new HashSet<>();
        for (TaskList tasks : taskList.values()) {
            uniqueTasksFromList.addAll(tasks.getList());
        }

        // Собрав все необходимые данные, записываем в Map<> итоговый результат
        Map<String, NamedReward> result = new HashMap<>();
        for (String taskTitle : uniqueTasksFromList) {
            String rewardKey = taskData.get(taskTitle).getReward();

            Reward reward = rewards.get(rewardKey);
            if (reward == null)
                continue;

            // Собираем все данные в новый формат
            NamedReward namedReward = new NamedReward(
                    reward.getMoney(),
                    reward.getDetails(),
                    reward.getReputation(),
                    rewardKey
            );

            result.put(taskTitle, namedReward);
        }

        // Записываем результат в .json
        try {
            writeJson(resultPath, result);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file: " + resultPath, e);
        }
    }

    public void doTask2(String resultPath) {
        /*
        ЗАДАНИЕ 2.
        */

        List<TaskExcelRowData> excelData = new ArrayList<>();
        // Итерируясь по всем list, собираем необходимые для записи данные
        for (Map.Entry<String, TaskList> entry : taskList.entrySet()) {
            List<String> lst = entry.getValue().getList();

            // Итерируемся по list
            for (String objectName : lst) {
                // Собираем нужные данные из распаршеных файлов
                String listName = entry.getKey();
                String rewardKey = taskData.get(objectName).getReward();

                Reward reward = rewards.get(rewardKey);
                int money = reward.getMoney();
                int details = reward.getDetails();
                int reputation = reward.getReputation();
                int isUsed = 1; // В данном случае все object_key точно используются

                excelData.add(new TaskExcelRowData(listName, objectName, rewardKey, money, details, reputation, isUsed));
            }
        }

        // Сначала сортируем данные, т.к. после выполнения следующей части задания появятся пустые поля("")
        excelData.sort(Comparator.comparing(TaskExcelRowData::getListName));

        // Ищем reward_key, которые не упоминаются в file.json и добавляем их в конец списка, list_name и object_name оставляем пустыми
        for (Map.Entry<String, Reward> entry : rewards.entrySet()) {
            String rewardKey = entry.getKey();
            boolean found = false;
            for (Task task : taskData.values()) {
                if (task.getReward().equals(rewardKey)) {
                    found = true;
                    break;
                }
            }

            // если reward_key не упоминается в taskData(file.json), записываем его в таблицу
            if(!found) {
                /* В данном случае нам нужны только данные из Reward
                    listName и objectName остаются пустыми при записи в таблице
                    Можно заменить на null */
                String listName     = "";
                String objectName   = "";

                Reward reward   = rewards.get(rewardKey);
                int money       = reward.getMoney();
                int details     = reward.getDetails();
                int reputation  = reward.getReputation();
                int isUsed      = 0;

                excelData.add(new TaskExcelRowData(listName, objectName, rewardKey, money, details, reputation, isUsed));
            }
        }

        // Записываем таблицу в .xlsx
        writeExcel(resultPath, excelData);
    }
}