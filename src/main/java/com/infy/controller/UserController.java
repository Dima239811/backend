package com.infy.controller;

import com.infy.dto.UserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserController {
    private static final String FILE_PATH = "data.txt";

    @PostMapping("/send-name")
    public ResponseEntity<String> receiveName(@RequestBody UserRequest request) {
        String name = request.getName();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Ошибка: имя не должно быть пустым!");
        }

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String lineToSave = String.format("[%s] Имя пользователя: %s", currentTime, name.trim());

        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(lineToSave);
            return ResponseEntity.ok("Имя '" + name + "' успешно сохранено!");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ошибка при записи файла на сервере.");
        }
    }

    @GetMapping("/get-names")
    public ResponseEntity<String> getNames() {
        File file = new File(FILE_PATH);

        // Если файл еще не создан — возвращаем понятное сообщение
        if (!file.exists()) {
            return ResponseEntity.ok("Файл data.txt пока пуст или не создан.");
        }

        try {
            // Считываем всё содержимое файла в строку
            String content = Files.readString(Paths.get(FILE_PATH));
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    @GetMapping("/get-by-date")
    public ResponseEntity<String> getUsersByDate(@RequestParam("date") String date) {
        String targetDate = date.trim();

        if (targetDate.isBlank()) {
            return ResponseEntity.badRequest().body("Дата не может быть пустой!");
        }

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return ResponseEntity.ok("Файл data.txt пуст или не найден.");
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));
            List<String> namesFound = new ArrayList<>();

            for (String line : lines) {
                if (line.isBlank()) continue;

                // Ищем строку, содержащую выбранную дату (например, "2026-09-24")
                if (line.contains(targetDate)) {
                    // Вытаскиваем имя из формата "[2026-09-24 15:30:00] Имя пользователя: Алексей"
                    if (line.contains("Имя пользователя: ")) {
                        String[] parts = line.split("Имя пользователя: ");
                        if (parts.length > 1) {
                            namesFound.add(parts[1].trim());
                        }
                    } else {
                        namesFound.add(line.trim());
                    }
                }
            }

            if (namesFound.isEmpty()) {
                return ResponseEntity.ok("За дату " + targetDate + " пользователей не найдено.");
            }

            // Возвращаем найденные имена через новую строку
            return ResponseEntity.ok(String.join("\n", namesFound));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при чтении файла: " + e.getMessage());
        }
    }
}
