package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    // 連線物件
    private Socket connection;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientHandler> ClientList;

    // 玩家封包
    String msgFromClient;
    private int status = 0;

    // 玩家資料
    static int[] characterIndex = new int[6];       // 角色序 1~5
    private String packet = "";                     // 封包
    private final int index;                        // 這個 client 編號
    private static int whoseRound=1;                // client 編號從 1 開始
    private static final int[] playerX = {1670,1670,1670,1670,1670,1670};     // 玩家座標陣列
    private static final int[] playerY = {1000,1000,1000,1000,1000,1000};

    // constructor
    public ClientHandler(int index,Socket connectionFormClient, ArrayList<ClientHandler> ClientList, int[] characterIndex) throws IOException {
        this.connection = connectionFormClient;
        this.ClientList = ClientList;
        this.index = index;
        this.characterIndex = characterIndex;
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        out = new PrintWriter(connection.getOutputStream(), true);
    }
    // 初始玩家封包
    public void Initialize(){
        packet = "0"+ClientList.size();
        for(int i = 1;i<=5;i++)
            packet += this.characterIndex[i];
        packet += index;
        outToAll();
    }

    // handle rounds
    public void addRound(){
        // 0 表示還沒
        if(msgFromClient.charAt(10) == '1'){
            whoseRound++;
            if(whoseRound > ClientList.size())
                whoseRound = 1;  // 超過玩家數->從首位繼續
        }
        packet += whoseRound;
    }

    // handle players position
    public void addPlayersPosition(){
        // store pos of this client
        playerX[index] = Integer.parseInt(msgFromClient.substring(0,5));
        playerY[index] = Integer.parseInt(msgFromClient.substring(5,10));

        // make packet (position part)
        for(int i=1;i<=5;i++){
            packet += String.format("%05d", playerX[i]);
            packet += String.format("%05d", playerY[i]);
        }
    }

    @Override
    public void run() {

        //out.println("[SERVER] Connection Established, Say Hello To Server");

        try{
            while (true) {

                // get packet from client
                msgFromClient = in.readLine();
                if(msgFromClient.charAt(11) == '1')
                    status = 1;

                // initialize "packet to client"
                packet = "";
                packet += status;

                switch (status){
                    case 0:
                        packet += (char)ClientList.size();
                        for(int a = 1;a<characterIndex.length;a++)
                            packet += characterIndex[a];
                        packet += index;
                        break;
                    case 1:
                        addRound();
                        addPlayersPosition();
                        break;
                }

                System.out.println("[CLIENT " + index + "] " + msgFromClient.replace("[CLIENT] ",""));

                // broadcast
                outToAll();
            }

        // don't really know whats going below
        }catch(IOException e){
            System.err.println("IOException in client handler");
            System.err.println(e.getStackTrace());
        }finally {
            out.close();
            try{
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void outToAll() {
        // let "aClient" travel through the list
        for (ClientHandler aClient : ClientList){

            // broadcast back
            aClient.out.println(packet);
        }
        System.out.println("[Client] Packet:"+packet);
    }
}
