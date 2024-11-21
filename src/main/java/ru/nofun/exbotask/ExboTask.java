package ru.nofun.exbotask;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.csv.CSVRecord;

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

    Map<String, TaskList> taskList;
    Map<String, Task> taskData;
    Map<String, Reward> rewards;


    ExboTask(String taskListPath, String taskDataPath, String rewardDataPath) throws IOException {
        JsonParser jsonParser = new JsonParser();

        // Парсим json и записываем в виде пар ключ/значение
        // gson для парсинга использует простые классы без конструктора с отрытыми полями, повторяющими структуру json
        // имена полей в этих классах должны совпадать с именами полей в .json
        taskList = jsonParser.parse(taskListPath, new TypeToken<Map<String, TaskList>>() {});
        taskData = jsonParser.parse(taskDataPath, new TypeToken<Map<String, Task>>() {});

        // Парсинг csv в Map<String, Reward> для удобства поиска значений
        rewards = parseRewards(rewardDataPath); //items.csv
    }

    public void doFirstTask(String resultPath) {
        /*
        ЗАДАНИЕ 1.
        */

        // Отсеиваем повторяющиеся objectName
        // таким образом получаем контракты, которые точно используются в list в task.json
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
        writeJson(resultPath, result);
    }

    public void doSecondTask(String resultPath) {
        /*
        ЗАДАНИЕ 2.
        */

        List<TaskExcelRowData> excelData = new ArrayList<>();
        // Итерируясь по всем list, собираем необходимые для записи данные
        for (Map.Entry<String, TaskList> entry : taskList.entrySet()) {
            List<String> lst = entry.getValue().getList();

            // Итерируемся по list
            for (String objectName : lst) {
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

        // Ищем reward_key, которые не упоминаются в file.json
        // Добавляем их в конец списка, list_name и object_name оставляем пустыми
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
                String listName = "";
                String objectName = "";

                Reward reward = rewards.get(rewardKey);
                int money = reward.getMoney();
                int details = reward.getDetails();
                int reputation = reward.getReputation();
                int isUsed = 0;

                excelData.add(new TaskExcelRowData(listName, objectName, rewardKey, money, details, reputation, isUsed));
            }
        }

        // Сортируем полученные данные
        excelData.sort(Comparator.comparing(TaskExcelRowData::getListName));

        // Записываем таблицу в .xlsx
        writeExcel(resultPath, excelData);
    }

    // Парсинг CSV таблицы
    Map<String, Reward> parseRewards(String filePath) throws IOException {
        Map<String, Reward> parsedRewards = new HashMap<>();
        CsvParser csvParser = new CsvParser(filePath);
        for (CSVRecord csvRecord : csvParser) {
            List<String> record = csvParser.toList(csvRecord);

            Reward reward = new Reward(
                    Integer.parseInt(record.get(1)), // money
                    Integer.parseInt(record.get(2)), // details
                    Integer.parseInt(record.get(3))  // reputation
            );

            parsedRewards.put(record.get(0), reward);
        }

        return parsedRewards;
    }

    // Запись готовых данных в json
    // В данном случае generic метод, т.к. Gson пользуется специальными классами для записи в файл
    private <Type> void writeJson(String filePath, Map<String, Type> jsonObject) {
        try (JsonWriter jsonWriter = new JsonWriter(filePath)) {
            jsonWriter.write(jsonObject);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file: " + filePath, e);
        }
    }

    // Запись готовых данных в Excel
    private void writeExcel(String resultPath, List<TaskExcelRowData> excelData) {
        try ( ExcelFileWriter excelFileWriter = new ExcelFileWriter(resultPath)) {
            for (TaskExcelRowData excelRow : excelData) {
                // Записываем данные в соответствующие ячейки
                excelFileWriter.setCellInCurrentRow(0, excelRow.getListName());
                excelFileWriter.setCellInCurrentRow(1, excelRow.getObjectName());
                excelFileWriter.setCellInCurrentRow(2, excelRow.getRewardKey());
                excelFileWriter.setCellInCurrentRow(3, excelRow.getMoney());
                excelFileWriter.setCellInCurrentRow(4, excelRow.getDetails());
                excelFileWriter.setCellInCurrentRow(5, excelRow.getReputation());
                excelFileWriter.setCellInCurrentRow(6, excelRow.getIsUsed());

                // Создаем следующую строку в таблице
                excelFileWriter.createNextRow();
            }

            excelFileWriter.writeToFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open file: " + resultPath, e);
        }
    }
}