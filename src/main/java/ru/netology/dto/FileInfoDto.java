package ru.netology.dto;


import lombok.Data;

@Data
public class FileInfoDto {

    private final String filename;
    private final String size;
}
