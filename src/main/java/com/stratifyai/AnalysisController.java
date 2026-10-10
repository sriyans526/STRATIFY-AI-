package com.stratifyai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*")
public class AnalysisController {

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeData(
            @RequestParam("file") MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename().toLowerCase();
            Map<String, List<Double>> columnData = new LinkedHashMap<>();

            // CSV file
            if (fileName.endsWith(".csv") || fileName.endsWith(".txt")) {
                String content = new String(file.getBytes());
                String[] lines = content.split("\\n");
                String[] headers = lines[0].split(",");

                for (String header : headers) {
                    columnData.put(header.trim(), new ArrayList<>());
                }

                for (int i = 1; i < lines.length; i++) {
                    if (lines[i].trim().isEmpty()) continue;
                    String[] values = lines[i].split(",");
                    for (int j = 0; j < headers.length && j < values.length; j++) {
                        try {
                            double val = Double.parseDouble(values[j].trim());
                            columnData.get(headers[j].trim()).add(val);
                        } catch (NumberFormatException e) {
                            // skip non-numeric
                        }
                    }
                }

            // Excel file
            } else if (fileName.endsWith(".xlsx")) {
                Workbook workbook = new XSSFWorkbook(file.getInputStream());
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                for (Cell cell : headerRow) {
                    columnData.put(cell.getStringCellValue().trim(), new ArrayList<>());
                }

                String[] headers = columnData.keySet().toArray(new String[0]);

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    for (int j = 0; j < headers.length; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                            columnData.get(headers[j]).add(cell.getNumericCellValue());
                        }
                    }
                }
                workbook.close();

            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Unsupported file type! Please upload CSV, TXT or XLSX file.");
                return ResponseEntity.badRequest().body(error);
            }

            // Analyze each column
            Map<String, Object> analysis = new LinkedHashMap<>();

            for (Map.Entry<String, List<Double>> entry : columnData.entrySet()) {
                String col = entry.getKey();
                List<Double> values = entry.getValue();
                if (values.isEmpty()) continue;

                double sum = 0, max = values.get(0), min = values.get(0);
                for (double v : values) {
                    sum += v;
                    if (v > max) max = v;
                    if (v < min) min = v;
                }
                double avg = sum / values.size();

                double first = values.get(0);
                double last = values.get(values.size() - 1);
                String pattern;
                if (last > first) pattern = "Increasing Trend ↑";
                else if (last < first) pattern = "Decreasing Trend ↓";
                else pattern = "Stable Trend →";

                int[] intData = new int[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    intData[i] = values.get(i).intValue();
                }
                BaseModel model = new AdvancedForecastModel();
                int prediction = model.predict(intData);

                Map<String, Object> colAnalysis = new LinkedHashMap<>();
                colAnalysis.put("average", Math.round(avg * 100.0) / 100.0);
                colAnalysis.put("max", max);
                colAnalysis.put("min", min);
                colAnalysis.put("pattern", pattern);
                colAnalysis.put("prediction", prediction);
                colAnalysis.put("values", values);
                colAnalysis.put("sum", Math.round(sum * 100.0) / 100.0);

                analysis.put(col, colAnalysis);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fileName", file.getOriginalFilename());
            result.put("fileType", fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase());
            result.put("totalRows", columnData.values().stream().mapToInt(List::size).max().orElse(0));
            result.put("totalColumns", columnData.size());
            result.put("analysis", analysis);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Could not read file: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}