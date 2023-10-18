package ru.netology.utils;


import java.util.UUID;

public class IdGenerator {

    public static int generateId() {
        return Math.abs(
                UUID.randomUUID()
                        .toString()
                        .hashCode()
        );
    }
}