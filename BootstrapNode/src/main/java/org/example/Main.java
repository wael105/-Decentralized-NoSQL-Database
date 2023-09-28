package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

class Server implements Runnable {

    private final Socket clientSocket;

    private final UserRegistrationService userRegistrationService;

    private final NodeManager nodeManager;

    public Server(Socket serverSocket, UserRegistrationService userRegistrationService, NodeManager nodeManager) {
        this.clientSocket = serverSocket;
        this.userRegistrationService = userRegistrationService;
        this.nodeManager = nodeManager;
    }

    @Override
    public void run() {
        try (BufferedReader clientRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true)) {

            String password = clientRead.readLine();
            User user = userRegistrationService.register(password);
            Node node = nodeManager.getNode(user.getHostName());
            clientWrite.println(user.getUserId());
            clientWrite.println("127.0.0.1");
            clientWrite.println(node.getClientPort());

            clientWrite.println("You are registered");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Docker docker = new Docker(Shell.INSTANCE, NodeProperties.INSTANCE);
        NodeManager nodeManager = new NodeManager();
        try {

            List<Node> nodes = docker.setupDockerNetworkAndContainers("NoSqlNetwork", "database-node");
            for (Node node : nodes) {
                nodeManager.addNode(node);
            }
        } catch (Exception e) {
            System.out.println("Error while creating docker network and containers");
            e.printStackTrace();
        }

        UserRegistrationService userRegistrationService = new UserRegistrationService(
                DiskService.INSTANCE,
                new DatabaseCommunicator(nodeManager.getNodes()),
                new LoadBalancer(nodeManager.getNodesNames())
        );

        // bootstrap client port
        int port = 1000;
        try (ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Bootstrap server started at port : " + port);
            while (true)
                new Thread(new Server(serverSocket.accept(), userRegistrationService, nodeManager)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}