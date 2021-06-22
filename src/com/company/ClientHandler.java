package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ClientHandler implements Runnable{

    // 連線物件
    private Socket connection;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientHandler> ClientList;

    // 玩家封包
    String msgFromClient;
    private int status = 1;

    // 玩家資料
    static int[] characterIndex = new int[6];       // 角色序 1~5
    private String packet = "";                     // 封包
    private final int index;                        // 這個 client 編號
    private static int whoseRound=1;                // client 編號從 1 開始
    private static final int[] playerX = {1670,1670,1670,1670,1670,1670};     // 玩家座標陣列
    private static final int[] playerY = {1000,1000,1000,1000,1000,1000};
    private static final int[] playerDie = {0,0,0,0,0,0};
    private static final int[][] playerAbility = {{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0},{0,0,0,0}};
    private String plate = "";
    private String cardNumber = "";
    private String data = "";

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
            do{
                whoseRound++;
                if(whoseRound > ClientList.size())
                    whoseRound = 1;  // 超過玩家數->從首位繼續
            }while (playerDie[whoseRound] == 1);
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

    // update players ability
    public void updatePlayersAbility(){
        playerAbility[index][0] = msgFromClient.charAt(12) - '0';
        playerAbility[index][1] = msgFromClient.charAt(13) - '0';
        playerAbility[index][2] = msgFromClient.charAt(14) - '0';
        playerAbility[index][3] = msgFromClient.charAt(15) - '0';
    }

    // handle players' death
    public void addPlayersDie(){
        for(int i = 1 ; i<= 5;i++){
            packet += playerDie[i];
        }
    }

    // handle players' ability
    public void addPlayersAbility(){
        for(int i = 1 ; i <= 5;i++){
            for(int j = 0;j < 4 ;j++){
                packet += playerAbility[i][j];
            }
        }
    }
    class rescue extends Thread{
        public void run(){
            while (true){
                Scanner scanner = new Scanner(System.in);
                String s = scanner.nextLine();
                if(s.equals("1")){
                    packet = data;
                    outToAll();
                }

            }

        }
    }

    @Override
    public void run() {

        rescue r = new rescue();
        r.start();
        try{
            while (true) {

                // get packet from client
                msgFromClient = in.readLine();

                updatePlayersAbility();

                // roundOver
                if(msgFromClient.charAt(10) == '1')
                    status = 1;
                // open new plate
                if(msgFromClient.charAt(16) == '1')
                    status = 3;
                // attack people
                if(msgFromClient.charAt(46) != '0')
                    status = 4;

                // initialize "packet to client"
                packet = "";
                packet += status;

                switch (status){
                    case 1:
                        addRound();
                        addPlayersPosition();
                        status = 2;  // next packet
                        break;
                    case 2:
                        addPlayersDie();
                        addPlayersAbility();
                        status = 1;
                        break;
                    case 3:
                        plate = msgFromClient.substring(16, 46);   // 16~45 new plate position
                        cardNumber = msgFromClient.substring(49, 57);  //  49~56 card number
                        packet += plate;
                        packet += cardNumber;
                        status = 1;
                        break;
                    case 4:
                        packet += msgFromClient.substring(46, 49); // 46~48 attack
                        status = 2;
                        break;
                }
                data = packet;
                // broadcast
                outToAll();
            }

        }catch(IOException e){
            outToAll();
            System.err.println("[IOException] IOException in client handler");
            System.err.println(e.getStackTrace());
        }catch (Exception e){
            outToAll();
            System.err.println("[Exception] "+e);
        } finally {
            outToAll();
            out.close();
            try{
                in.close();
            }catch (IOException e){
                e.printStackTrace();
            }catch (Exception e){
                System.err.println("[Exception2] "+e);
            }
        }
    }

    private void outToAll() {
        // let "aClient" travel through the list
        for (ClientHandler aClient : ClientList){

            // broadcast back
            aClient.out.println(packet);
        }
        System.out.println("[Client Packet]:"+packet);
    }
}
