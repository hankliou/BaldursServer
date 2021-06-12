package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection implements Runnable{

    private Socket connection;
    private BufferedReader in;

    public ServerConnection(Socket connection) throws IOException {
        this.connection = connection;
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String msgFromServer = in.readLine();

                if (msgFromServer == null) break;

                System.out.println(msgFromServer);
            }
        } catch (IOException e) {
                e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
