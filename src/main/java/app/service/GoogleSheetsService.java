package app.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service("UstanovkaChatGPTbot")
public class GoogleSheetsService {

    private final Drive drive;
    private final Sheets sheets;

    public GoogleSheetsService(Sheets sheets, Drive drive) {
        this.sheets = sheets;
        this.drive = drive;
    }


    /**
     * Создает новый отчет в отдельной таблице
     *
     * @param reportName Название отчета
     * @param headers    Заголовки столбцов
     * @param data       Данные для записи
     * @return URL созданной таблицы
     */
    public String createIndividualReport(String reportName,
                                         List<String> headers,
                                         List<List<Object>> data) throws IOException {
        // 1. Создаем новую таблицу
        Spreadsheet newSpreadsheet = createNewSpreadsheet(reportName);
        String spreadsheetId = newSpreadsheet.getSpreadsheetId();

        // 2. Формируем данные с заголовками
        List<List<Object>> allData = new ArrayList<>();
        allData.add(new ArrayList<>(headers));
        allData.addAll(data);

        // 3. Записываем данные
        writeToSpreadsheet(spreadsheetId, "A1", allData);

        setAnyoneCanEdit(spreadsheetId);

        return newSpreadsheet.getSpreadsheetUrl();
    }

    /**
     * Создает новую таблицу с уникальным именем
     */
    private Spreadsheet createNewSpreadsheet(String baseName) throws IOException {
        String uniqueName = generateUniqueName(baseName);

        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(uniqueName));

        return sheets.spreadsheets()
                .create(spreadsheet)
                .execute();
    }

    /**
     * Генерирует уникальное имя таблицы
     */
    private String generateUniqueName(String baseName) {
        return baseName.concat(" - ").concat(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).concat(" - ")
                .concat(UUID.randomUUID().toString().substring(0, 4));
    }

    /**
     * Запись данных в указанный диапазон таблицы
     */
    private void writeToSpreadsheet(String spreadsheetId,
                                    String startCell,
                                    List<List<Object>> values) throws IOException {
        ValueRange body = new ValueRange().setValues(values);

        sheets.spreadsheets().values()
                .update(spreadsheetId, startCell, body)
                .setValueInputOption("RAW")
                .execute();
    }

    /**
     * Делает таблицу доступной «по ссылке» для чтения и редактирования.
     *
     * @param fileId ID таблицы (Spreadsheet)
     */
    private void setAnyoneCanEdit(String fileId) throws IOException {
        Permission permission = new Permission()
                .setType("anyone")          // доступ без авторизации
                .setRole("writer")          // «редактор» (для только-чтения используй "reader")
                .setAllowFileDiscovery(false); // именно «по ссылке», а не «публично в поиске»

        drive.permissions()
                .create(fileId, permission)
                .setFields("id")              // лишние поля не нужны
                .execute();
    }



    public String createIndividualReport(String reportName,
                                         List<String> listNames,
                                         Map<String, List<String>> headers,
                                         Map<String, List<List<Object>>> data) throws IOException {
        /* ---------- 1. создаём пустую таблицу ---------- */
        Spreadsheet newSpreadsheet = createNewSpreadsheet(reportName);
        String spreadsheetId = newSpreadsheet.getSpreadsheetId();

        /* ---------- 2. переименовываем дефолтный лист и добавляем остальные ---------- */
        if (!listNames.isEmpty()) {
            String first = listNames.getFirst();
            int defaultSheetId = newSpreadsheet.getSheets()
                                               .getFirst()
                                               .getProperties()
                                               .getSheetId();

            /* 2-а) переименовать базовый лист */
            renameSheet(spreadsheetId, defaultSheetId, first);

            /* 2-б) добавить остальные */
            if (listNames.size() > 1) {
                addSheets(spreadsheetId, listNames.subList(1, listNames.size()));
            }
        }

        /* ---------- 3. записываем данные по каждому листу ---------- */
        for (String sheetName : listNames) {
            List<List<Object>> rows = new ArrayList<>();
            rows.add(new ArrayList<>(headers.getOrDefault(sheetName, List.of())));
            rows.addAll(data.getOrDefault(sheetName, List.of()));

            writeToSpreadsheet(spreadsheetId, sheetName + "!A1", rows);
        }

        /* ---------- 4. открываем доступ по ссылке ---------- */
        setAnyoneCanEdit(spreadsheetId);

        return newSpreadsheet.getSpreadsheetUrl();
    }

    /* ================================================================
       D   Y   N   A   M   I   C      S   H   E   E   T   S
       ================================================================ */

    /** Добавляет новые листы с указанными названиями */
    private void addSheets(String spreadsheetId, List<String> sheetTitles) throws IOException {
        if (sheetTitles.isEmpty()) return;

        var requests = new ArrayList<com.google.api.services.sheets.v4.model.Request>();
        for (String title : sheetTitles) {
            var add = new com.google.api.services.sheets.v4.model.AddSheetRequest()
                    .setProperties(new com.google.api.services.sheets.v4.model.SheetProperties()
                            .setTitle(title));
            requests.add(new com.google.api.services.sheets.v4.model.Request().setAddSheet(add));
        }
        var body = new com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }

    /** Переименовывает существующий лист */
    private void renameSheet(String spreadsheetId, int sheetId, String newTitle) throws IOException {
        var update = new com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest()
                .setProperties(new com.google.api.services.sheets.v4.model.SheetProperties()
                        .setSheetId(sheetId)
                        .setTitle(newTitle))
                .setFields("title");
        var body = new com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest()
                .setRequests(List.of(
                        new com.google.api.services.sheets.v4.model.Request()
                                .setUpdateSheetProperties(update)
                ));
        sheets.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }
}