package com.mak.chat.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private final Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket) {
            this.socket = socket;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println("Client " + name + " connected");
            broadcastMessage("Server: " + name + " connected");
        } catch (IOException e) {
            closeEverything(socket, bufferedWriter, bufferedReader);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }
            catch (IOException e) {
                closeEverything(socket, bufferedWriter, bufferedReader);
                break;
            }
        }
    }

    private void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClient();
        try {

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        String[] splitMessage = message.split(" ");
        String privateName = splitMessage[1];

        privateName = privateName.replace("@", "");
        privateName = privateName.replace(":", "");

        if (splitMessage[1].charAt(0) == '@') {
            for (ClientManager client : clients) {
                try {

                    if (client.name.equals(privateName)) {

                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter, bufferedReader);

                }
            }
        } else if (splitMessage[1].charAt(0) != '@') {
            for (ClientManager client : clients) {
                try {
                    if (!client.name.equals(name)) {
                        client.bufferedWriter.write(message);
                        client.bufferedWriter.newLine();
//                        client.bufferedWriter.write(name);
//                        client.bufferedWriter.newLine();
//                        client.bufferedWriter.write(privateName);
//                        client.bufferedWriter.newLine();
//

                        client.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedWriter, bufferedReader);

                }
            }
        }
    }

    private void removeClient() {
        clients.remove(this);
        System.out.println(name + " disconnected");
        broadcastMessage("Server: " +name + "disconnected");
    }
}