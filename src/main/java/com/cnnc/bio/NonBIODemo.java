package com.cnnc.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NonBIODemo {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            System.out.println("socket bind on port: 6666");

            while (true) {
                final Socket socket = serverSocket.accept();
                System.out.println("accept socket: " + socket.toString());
                new Thread() {
                    @Override
                    public void run() {
                        InputStream inputStream = null;
                        try {
                            inputStream = socket.getInputStream();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                            while (true) {
                                System.out.println("accept message from socket: " + socket.toString() + " " + bufferedReader.readLine());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
