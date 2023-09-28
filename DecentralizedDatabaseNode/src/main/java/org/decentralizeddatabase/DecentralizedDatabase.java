package org.decentralizeddatabase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.decentralizeddatabase.authentication.AuthenticationService;
import org.decentralizeddatabase.constants.NodeProperties;
import org.decentralizeddatabase.models.User;
import org.decentralizeddatabase.models.requests.NodeRequest;
import org.decentralizeddatabase.models.requests.UserRequest;
import org.decentralizeddatabase.nodes.NodeCommunicator;
import org.decentralizeddatabase.services.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class ClientServer implements Runnable {

    private final Socket clientSocket;

    private final DatabaseFacade databaseFacade;

    private final ObjectMapper objectMapper;

    private final AuthenticationService authenticationService;

    public ClientServer(Socket serverSocket, AuthenticationService authenticationService) {
        this.clientSocket = serverSocket;
        this.databaseFacade = new DatabaseFacade(
                DatabaseService.INSTANCE,
                CollectionService.INSTANCE,
                DocumentService.INSTANCE,
                ValidationService.INSTANCE,
                NodeCommunicator.INSTANCE);

        this.objectMapper = new ObjectMapper();
        this.authenticationService = authenticationService;
    }

    @Override
    public void run() {
        try {
            BufferedReader clientRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            while(true) {
                System.out.println("Waiting for login");
                int id = Integer.parseInt(clientRead.readLine());
                String password = clientRead.readLine();
                if (!authenticationService.isUserRegisteredToUseThisNode(id, password)) {
                    clientWrite.println(false);
                    continue;
                }
                clientWrite.println(true);
                break;
            }

            while (true) {
                System.out.println("Waiting for request");
                String jsonString = clientRead.readLine();

                UserRequest request = objectMapper.readValue(jsonString, UserRequest.class);
                Object object = databaseFacade.processRequest(request);

                clientWrite.println(objectMapper.writeValueAsString(object));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class NodeServer implements Runnable {

    private final Socket clientSocket;

    private final DatabaseFacade databaseFacade;

    private final ObjectMapper objectMapper;

    public NodeServer(Socket serverSocket) {
        this.clientSocket = serverSocket;
        this.databaseFacade = new DatabaseFacade(
                DatabaseService.INSTANCE,
                CollectionService.INSTANCE,
                DocumentService.INSTANCE,
                ValidationService.INSTANCE,
                NodeCommunicator.INSTANCE);

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            BufferedReader clientRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);

            System.out.println("Waiting for sync request");
            String jsonString = clientRead.readLine();
            System.out.println("Sync request: " + jsonString);
            NodeRequest request = objectMapper.readValue(jsonString, NodeRequest.class);
            System.out.println("sync request: " + request.toString());
            System.out.println("sync request: received");
            Object object = databaseFacade.processRequest(request);

            clientWrite.println(objectMapper.writeValueAsString(object));
            System.out.println("sync response sent: " + objectMapper.writeValueAsString(object));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class BootstrapServer implements Runnable {

    private final Socket clientSocket;

    private final AuthenticationService authenticationService;

    private final ObjectMapper objectMapper;

    public BootstrapServer(Socket serverSocket, AuthenticationService authenticationService) {
        this.clientSocket = serverSocket;
        this.authenticationService = authenticationService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            BufferedReader clientRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
            String input = clientRead.readLine();
            User user = objectMapper.readValue(input, User.class);
            authenticationService.registerUser(user);
            System.out.println("User registered " + user.getUserId());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class Bootstrap implements Runnable {

    @Override
    public void run() {
        int port = NodeProperties.INSTANCE.getBootstrapPort();
        System.out.println("Bootstrap port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while (true)
                new Thread(new BootstrapServer(serverSocket.accept(), AuthenticationService.INSTANCE)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class Node implements Runnable {

    @Override
    public void run() {
        int port = NodeProperties.INSTANCE.getNodePort();
        System.out.println("Node port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while (true)
                new Thread(new NodeServer(serverSocket.accept())).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class DecentralizedDatabase {

    public static void main(String[] args) {

        new Thread(new Bootstrap()).start();
        new Thread(new Node()).start();

        int port = NodeProperties.INSTANCE.getClientPort();
        System.out.println("hostname : " + NodeProperties.INSTANCE.getHostName());
        System.out.println("Node number : " + System.getenv("NODE_NUMBER"));
        System.out.println("Client port : " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while (true)
                new Thread(new ClientServer(serverSocket.accept(), AuthenticationService.INSTANCE)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
