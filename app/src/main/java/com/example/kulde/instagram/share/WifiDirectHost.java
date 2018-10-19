package com.example.kulde.instagram.share;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WifiDirectHost extends Thread {

    Socket socket;
    ServerSocket serverSocket;

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(3737);
            socket = serverSocket.accept();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeSocket(){
        try {
            this.socket.close();
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}