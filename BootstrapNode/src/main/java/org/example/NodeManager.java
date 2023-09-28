package org.example;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeManager {
    private final Map<String, Node> nodes;

    NodeManager() {
        nodes = new ConcurrentHashMap<>();
    }

    public void addNode(Node node) {
        nodes.put(node.getNodeName(), node);
    }

    public Node getNode(String nodeName) {
        return nodes.get(nodeName);
    }

    public List<Node> getNodes() {
        return List.copyOf(nodes.values());
    }

    public List<String> getNodesNames() {
        return List.copyOf(nodes.keySet());
    }
}
