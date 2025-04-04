package com.mak;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
//лекция
public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public Client(Socket socket, String userName) {
        this.socket = socket;
        this.name = userName;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                String message = scanner.nextLine();
                bufferedWriter.write(name+": " +message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }
        catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroup;
                while (socket.isConnected()) {
                 try {
                    messageFromGroup = bufferedReader.readLine();
                    System.out.println(messageFromGroup);
                }
                 catch (IOException e) {
                 closeEverything(socket, bufferedReader, bufferedWriter);}
                }
            }

        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
             try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();
        Socket socket = new Socket("localhost", 1300);
        Client client = new Client(socket, name);
        client.listenForMessage();
        client.sendMessage();
    }
}
