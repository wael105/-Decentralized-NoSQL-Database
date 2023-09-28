package org.example;

import lombok.Getter;

@Getter
public enum BootstrapProperties {
    INSTANCE;

    private final String userDataLocation = "D:\\IntelliJ Projects\\Atypon\\BootstrapNode\\src\\main\\resources\\Users\\";
}
