package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    private static final String SERVER_IP = "192.168.56.1";
    private static final int SERVER_PORT = 9090;

    public static void main(String[] args) throws IOException {

        // connect to serer
        Socket connection = new Socket(SERVER_IP, SERVER_PORT);

        // create threads
        ServerConnection serverCon = new ServerConnection(connection);

        // msg io
        PrintWriter out = new PrintWriter(connection.getOutputStream(), true);

        // keyboard input
        BufferedReader keyboardInput = new BufferedReader(new InputStreamReader(System.in));

        // start the thread, here we don't need to "pool" cause it has only one thread
        new Thread(serverCon).start();

        // infinite loop to keep on server
        while(true){

            // input
            String msgToServer = keyboardInput.readLine();

            // quit
            if(keyboardInput.equals("quit")) break;

            // send msg to server
            out.println("[CLIENT] " + msgToServer);
        }

        connection.close();
        System.exit(0);
    }
}
