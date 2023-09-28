package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public enum DiskService {

    INSTANCE;

    private final ObjectMapper objectMapper;

    DiskService() {
        this.objectMapper = new ObjectMapper();
    }

    public <T> void write(String filePath, T data) {
        try {
            objectMapper.writeValue(new java.io.FileWriter(filePath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public <T> T read(String filePath, Class<T> className) {
        T requestObject = null;
        try {
            requestObject = objectMapper.readValue(new java.io.FileReader(filePath), className);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestObject;
    }

    public void createDirectory(String filePath) {
        try {
            Files.createDirectory(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean doesFileExist(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
}
