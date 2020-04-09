package com.cnnc.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class BIODemo {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            System.out.println("socket bind on port: 5555");

            while (true) {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                while (true) {
                    System.out.println("accept client: " + socket.toString() + " message: " + bufferedReader.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
