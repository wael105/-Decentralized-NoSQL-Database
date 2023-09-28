package org.decentralizeddatabase.constants;

import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public enum NodeProperties {
    INSTANCE;

    private final String databaseLocation = "./file_system";

    private final String userDataLocation = "./user_data";

    private final String hostName = System.getenv("NAME") + System.getenv("NODE_NUMBER");

    private final int clientPort = Integer.parseInt(System.getenv("CLIENT_PORT"));

    private final int bootstrapPort = Integer.parseInt(System.getenv("BOOTSTRAP_PORT"));

    private final int baseNodePort = Integer.parseInt(System.getenv("TCP_PORT"));

    private final int nodePort = Integer.parseInt(System.getenv("TCP_PORT")) + Integer.parseInt(System.getenv("NODE_NUMBER")) - 1;

    private final int numberOfNodes = Integer.parseInt(System.getenv("NUMBER_OF_NODES"));

    public List<String> getAllHostNames() {
        List<String> hostNames = new CopyOnWriteArrayList<>();
        for (int i = 1; i <= numberOfNodes; i++) {
            hostNames.add(System.getenv("NAME") + i);
        }

        return hostNames;
    }
}
