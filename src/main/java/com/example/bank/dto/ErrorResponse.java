package com.example.bank.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Информация ошибок")
public class ErrorResponse {

    @Schema(
            description = "HTTP статус ошибки",
            example = "400"
    )
    private int status;

    @Schema(
            description = "Название ошибки",
            example = "Bad Request"
    )
    private String error;

    @Schema(
            description = "Сообщение об ошибке",
            example = "Неверный формат email"
    )
    private String message;

    @Schema(
            description = "Путь запроса, вызвавшего ошибку",
            example = "/api/users"
    )
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(
            description = "Дата и время возникновения ошибки",
            example = "2026-08-17 15:30:45"
    )
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}