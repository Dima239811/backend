package com.infy.controller;

import com.infy.dto.UserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
}
