package ru.nofun.exbotask;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;


// Класс для записи в Excel
public class ExcelFileWriter implements AutoCloseable {
    private final String filepath;
    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private FileOutputStream fileOut;

    private Row currentRow;
    private int currentRowIndex;

    ExcelFileWriter(String filepath) throws IOException {
        this.filepath = filepath;
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet();

        this.fileOut = new FileOutputStream(this.filepath);

        this.currentRowIndex = 0;
        this.currentRow = sheet.createRow(currentRowIndex);

    }

    public void createNextRow() {
        this.currentRowIndex++;
        this.currentRow = sheet.createRow(this.currentRowIndex);
    }

    // Методы для записи разных типов данных в ячейки - String, int, double
    public void setCellInCurrentRow(int cellIndex, String value) {
        this.currentRow.createCell(cellIndex).setCellValue(value);
    }

    public void setCellInCurrentRow(int cellIndex, int value) {
        this.currentRow.createCell(cellIndex).setCellValue(value);
    }

    public void setCellInCurrentRow(int cellIndex, double value) {
        this.currentRow.createCell(cellIndex).setCellValue(value);
    }

    // Запись в файл
    public void writeToFile() throws IOException {
        workbook.write(fileOut);
    }

    public void close() throws IOException {
        fileOut.close();
    }
}