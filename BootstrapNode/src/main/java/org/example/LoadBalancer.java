package org.example;

import java.util.List;

public class LoadBalancer {

    private final List<String> hosts;

    private int currentHostIndex = 0;

    public LoadBalancer(List<String> nodes) {
        this.hosts = nodes;
    }

    public String getNextNodeName() {
        return hosts.get(currentHostIndex++ % hosts.size());
    }
}
