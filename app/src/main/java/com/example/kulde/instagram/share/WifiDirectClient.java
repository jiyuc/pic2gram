package com.example.kulde.instagram.share;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiDirectClient extends Thread {

    Socket socket;
    String hostIP;

    public WifiDirectClient(InetAddress hostIP) {
        this.hostIP = hostIP.getHostAddress();
        this.socket = new Socket();
    }

    public void closeSocket(){
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.socket.connect(new InetSocketAddress(this.hostIP, 3737), 500);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
