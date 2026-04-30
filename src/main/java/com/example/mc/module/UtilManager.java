package com.example.mc.module;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UtilManager {

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String now() {
        return LocalDateTime.now().format(formatter);
    }
}
