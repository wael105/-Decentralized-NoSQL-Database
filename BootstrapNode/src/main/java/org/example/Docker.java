package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Docker {
    private final Shell shell;

    private final NodeProperties nodeProperties;

    public Docker(Shell shell, NodeProperties nodeProperties) {
        this.shell = shell;
        this.nodeProperties = nodeProperties;
    }

    public List<Node> setupDockerNetworkAndContainers(String networkName, String imageName) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        shell.runCommand(String.format("docker network create %s", networkName));
        if (doNodesExist())
            return startContainers();
        else
            return addContainers(networkName, imageName);
    }

    private List<Node> startContainers() {
        int bootstrapPort = nodeProperties.getBootstrapPort();
        int clientPort = nodeProperties.getClientPort();
        List<Node> nodes = new ArrayList<>();
        int numberOfNodes = nodeProperties.getNumberOfNodes();
        String name = "node-";
        for (int i = 1; i <= numberOfNodes; i++, bootstrapPort++, clientPort++) {
            try {
                String hostName = name + i;

                String command = String.format("docker start %s", hostName);
                shell.runCommand(command);

                nodes.add(new Node(hostName, bootstrapPort, clientPort));
            } catch (Exception e) {
                System.out.println("Error while starting container :" + i);
                System.out.println(e);
            }
        }
        return nodes;
    }

    private boolean doNodesExist() {
        int result = 0;
        try {
            result = Integer.parseInt(shell.runCommand("docker ps -aq | find /c /v \"\""));
        } catch (Exception e) {
            System.out.println("Error while checking if containers exist");
            System.out.println(e);
        }

        return result == nodeProperties.getNumberOfNodes();
    }

    private List<Node> addContainers(String networkName, String imageName) {
        int tcpPort = nodeProperties.getTcpStartingRange();
        int baseTcpPort = tcpPort;
        int bootstrapPort = nodeProperties.getBootstrapPort();
        int clientPort = nodeProperties.getClientPort();
        List<Node> nodes = new ArrayList<>();
        int numberOfNodes = nodeProperties.getNumberOfNodes();
        String name = "node-";
        for (int i = 1; i <= numberOfNodes; i++, tcpPort++, bootstrapPort++, clientPort++) {
            try {
                String hostName = name + i;
                String runContainer = String.format(
                        "docker run -e NAME=%s -e NODE_NUMBER=%s -e TCP_PORT=%s -e BOOTSTRAP_PORT=%s -e CLIENT_PORT=%s -e NUMBER_OF_NODES=%s --hostname %s --network=%s --name %s -p %s:%s/tcp -p %s:%s/tcp -p %s:%s/tcp  -itd %s",
                        name, i, baseTcpPort, bootstrapPort, clientPort, numberOfNodes, hostName, networkName, hostName, tcpPort, tcpPort, clientPort, clientPort, bootstrapPort, bootstrapPort, imageName);

                shell.runCommand(runContainer);

                nodes.add(new Node(hostName, bootstrapPort, clientPort));
            } catch (Exception e) {
                System.out.println("Error while creating container :" + i);
                System.out.println(e);
            }
        }

        return nodes;
    }
}
