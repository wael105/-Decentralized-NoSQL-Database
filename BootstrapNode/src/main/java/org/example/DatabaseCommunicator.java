package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class DatabaseCommunicator {

    private final List<Node> nodes;

    private final ObjectMapper objectMapper;

    public DatabaseCommunicator(List<Node> nodes) {
        objectMapper = new ObjectMapper();
        this.nodes = nodes;
    }

    public void registerUserToDatabaseNodes(User user)  {

        String userJson = null;

        try {
            userJson = objectMapper.writeValueAsString(user);
            System.out.println("user: \n" + userJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        for (Node node : nodes) {
            try (Socket socket = new Socket("127.0.0.1", node.getBootstrapPort())){

                PrintWriter clientWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("Sending user to server: " + socket.getInetAddress() + ":" + socket.getPort());
                clientWrite.println(userJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
