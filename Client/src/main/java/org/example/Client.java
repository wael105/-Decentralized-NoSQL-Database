package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String bootstrapAddress = "127.0.0.1";
        int bootstrapPort = 1000;
        int serverPort = 0;
        int id;
        String serverAddress = "";
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        try (Socket clientSocket = new Socket(bootstrapAddress, bootstrapPort);
             BufferedReader serverRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter serverWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
        ){

            System.out.println("Enter Password: ");
            String pass = userInput.readLine();
            serverWrite.println(pass);
            id = Integer.parseInt(serverRead.readLine());
            serverAddress = serverRead.readLine();
            serverPort = Integer.parseInt(serverRead.readLine());
            System.out.println(id);
            System.out.println(serverAddress);
            System.out.println(serverPort);
        } catch (IOException e){
            e.printStackTrace();
        }

        try (Socket clientSocket = new Socket(serverAddress, serverPort);
             BufferedReader serverRead = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter serverWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
        ){
            boolean loginResponse = false;
            while (!loginResponse) {
                System.out.println("Enter User Name: ");
                serverWrite.println(userInput.readLine());
                System.out.println("Enter Password: ");
                String password = userInput.readLine();
                serverWrite.println(password);
                String r = serverRead.readLine();
                System.out.println(r);
                loginResponse = Boolean.parseBoolean(r);
            }

            while (true) {
                System.out.println("Enter Request: ");
                String request = userInput.readLine();
                serverWrite.println(request);
                String response = serverRead.readLine();
                System.out.println(response);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}