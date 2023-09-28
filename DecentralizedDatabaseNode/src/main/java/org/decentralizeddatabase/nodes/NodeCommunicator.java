package org.decentralizeddatabase.nodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.decentralizeddatabase.constants.*;
import org.decentralizeddatabase.models.requests.NodeRequest;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public enum NodeCommunicator {

    INSTANCE;

    private final ThreadPoolExecutor executor;

    private final NodeProperties nodeProperties;

    private final List<String> hosts;

    private final ObjectMapper objectMapper;

    NodeCommunicator() {
        this.nodeProperties = NodeProperties.INSTANCE;
        this.executor = new ThreadPoolExecutor(1, 1, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.objectMapper = new ObjectMapper();
        hosts = nodeProperties.getAllHostNames();

        System.out.println("property hostname: " + nodeProperties.getHostName());
        System.out.println("property port: " + nodeProperties.getNodePort());

    }

    public void syncWithAllNodes(Operation operation, Map<String, Object> data, String databaseName, String collectionName) {
        NodeRequest nodeRequest = buildNodeRequest(operation, data, databaseName, collectionName);

        executor.execute( () -> {
            for (int i = 1, port = nodeProperties.getBaseNodePort(); i <= hosts.size(); i++, port++) {
                String host = hosts.get(i - 1);

                if(host.equals(nodeProperties.getHostName()))
                    continue;

                try (Socket socket = new Socket(host, port)){
                    BufferedReader serverRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter serverWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                    System.out.println("sync request sent: " + objectMapper.writeValueAsString(nodeRequest));
                    serverWrite.println(objectMapper.writeValueAsString(nodeRequest));
                    String responseString = serverRead.readLine();
                    System.out.println("sync response received: " + responseString);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public String redirectRequest(Operation operation, Map<String, Object> data, String databaseName, String collectionName, String host) {
        NodeRequest nodeRequest = buildNodeRequest(operation, data, databaseName, collectionName);

        try (Socket socket = new Socket(host, nodeProperties.getBaseNodePort() + hosts.indexOf(host))){
            BufferedReader serverRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

            serverWrite.println(objectMapper.writeValueAsString(nodeRequest));
            return serverRead.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private NodeRequest buildNodeRequest(Operation operation, Map<String, Object> data, String databaseName, String collectionName) {
        return NodeRequest.builder()
                .nodeName(nodeProperties.getHostName())
                .data(data)
                .databaseName(databaseName)
                .collectionName(collectionName)
                .operation(operation)
                .build();
    }
}
