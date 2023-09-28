package org.example;

import lombok.Getter;

@Getter
public class Node {
    private final String nodeName;
    private final int bootstrapPort;
    private final int clientPort;

    public Node(String nodeName, int bootstrapPort, int clientPort) {
        this.nodeName = nodeName;
        this.bootstrapPort = bootstrapPort;
        this.clientPort = clientPort;
    }

}
