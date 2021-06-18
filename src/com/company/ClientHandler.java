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
    int status = 1;

    // 玩家資料
    static int[] characterIndex = new int[6];       // 角色序 1~5
    private String packet = "";                     // 封包
    private final int index;                        // 這個 client 編號
    private static int whoseRound=1;                // client 編號從 1 開始
    private static final int[] playerX = {1670,1670,1670,1670,1670,1670};     // 玩家座標陣列
    private static final int[] playerY = {1000,1000,1000,1000,1000,1000};
    private static int[][] characterAbility = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
    // Might,Speed,Sanity,Knowledge
    private static int[] die = {0,0,0,0,0,0};
    int newPlateX, newPlateY, newPlateID;
    boolean newPlate = false;


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
        packet = "0" + ClientList.size();
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

    // handle players ability
    public void addAbility(){
        for(int i = 1;i<=5;i++){
            packet += die[i]; // 生死狀況
        }
        for(int i = 1;i<=5;i++){
            for(int j = 0; j < 4;j++){
                packet+=characterAbility[i][j];
            }
        }
    }
    // handle plates
    public void addPlate(){
        if(newPlate){
            packet += 1;
            packet += String.format("%02d", newPlateX);
            packet += String.format("%02d", newPlateY);
            packet += String.format("%02d", newPlateID);
            newPlate = false;
        }
        else packet += "0000000";
    }

    @Override
    public void run() {
        try{
            while (true) {

                // get packet from client
                msgFromClient = in.readLine();
                System.out.println("[CLIENT " + index + "] " + msgFromClient.replace("[CLIENT] ",""));

                // update characterAbility
                characterAbility[index][0] = msgFromClient.charAt(12) - '0'; // Might
                characterAbility[index][1] = msgFromClient.charAt(13) - '0'; // Speed
                characterAbility[index][2] = msgFromClient.charAt(14) - '0'; // Sanity
                characterAbility[index][3] = msgFromClient.charAt(15) - '0'; // Knowledge

                // fetch new open plate
                if(msgFromClient.charAt(16) == '1') {
                    newPlate = true;
                    newPlateX = Integer.parseInt(msgFromClient.substring(17, 19));
                    newPlateY = Integer.parseInt(msgFromClient.substring(19, 21));
                    newPlateID = Integer.parseInt(msgFromClient.substring(21, 23));
                }

                // initialize "packet to client"
                packet = "";
                packet += status;

                if(status == 1) {
                    addRound();
                    addPlayersPosition();
                    status = 2;
                }else{
                    addAbility();
                    addPlate();
                    status = 1;
                }
                outToAll();  // broadcast
            }

            // catching exceptions & close stream writer
        }catch(IOException e){
            System.err.println("IOException in client handler");
            System.err.println(e.getStackTrace());
            System.out.println("out : " + packet);
            System.out.println("in : " + msgFromClient);
        }finally {
            out.close();
            try{
                in.close();
            }catch (IOException e){
                System.out.println("out : " + packet);
                System.out.println("in : " + msgFromClient);
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