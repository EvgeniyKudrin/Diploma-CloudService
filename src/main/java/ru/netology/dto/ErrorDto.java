package ru.netology.dto;


import lombok.Data;
import ru.netology.utils.IdGenerator;

@Data
public class ErrorDto {
    private final Integer id;
    private final String errorMessage;

    public ErrorDto(String message) {
        this.id = IdGenerator.generateId();
        this.errorMessage = message;
    }
}