package org.example;

import lombok.Getter;

@Getter
public enum NodeProperties {
    INSTANCE;
    private final int tcpStartingRange = 4000;
    private final int bootstrapPort = 3000;
    private final int clientPort = 2000;

    private final int numberOfNodes = 4;
}
