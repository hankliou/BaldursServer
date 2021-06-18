package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    // set port
    private static final int PORT = 9090;

    // index of client
    static int index = 1;

    // character index
    static int[] characterIndex = new int[6];

    // place to store various threads
    private static ArrayList<ClientHandler> clientList = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws IOException {

        // set a port for client
        ServerSocket OpenedPort = new ServerSocket(PORT);
        System.out.println("[SERVER] Waiting for client connection...");

        // randomly generate 5 num
        Random r = new Random();
        boolean[] a = {false, false, false, false, false, false, false};
        for(int i=1;i<=5;i++){
            int tmp;
            do{
                tmp = r.nextInt(6)+1;
            }while (a[tmp]);
            characterIndex[i] = tmp;
            a[characterIndex[i]]=true;
        }

        while(true){

            // wait for connection establish
            Socket connection = OpenedPort.accept();
            System.out.println("[SERVER] Welcome client " + index + " connected to server! ");

            // create a client thread
            ClientHandler clientThread = new ClientHandler(index, connection, clientList, characterIndex);
            index ++;

            // add it into ArrayList
            clientList.add(clientThread);

            // client initialize package
            clientThread.Initialize();

            // ask executor to run
            pool.execute(clientThread);
        }
    }
}
