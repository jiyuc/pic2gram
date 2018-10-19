package com.example.kulde.instagram.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.example.kulde.instagram.share.WifiDirectActivity;
import com.example.kulde.instagram.share.WifiDirectClient;

public class WDBroadcastReciever extends BroadcastReceiver {

    private static final String TAG = "WDBroadcastReciever";
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private WifiDirectActivity mainActivity;

    public WDBroadcastReciever(WifiP2pManager p2pManager, WifiP2pManager.Channel p2pChannel, WifiDirectActivity mainActivity) {
        this.p2pManager = p2pManager;
        this.p2pChannel = p2pChannel;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(p2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "1 onReceive() called " + "WIFI_P2P_STATE_CHANGED_ACTION");
            int state = intent.getIntExtra(p2pManager.EXTRA_WIFI_STATE, -1);

            if(state == p2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d(TAG, "got here");
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();

            } else{
                Log.d(TAG, "got somewhere else");
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
            }

        } else if(p2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "2 onReceive() called " + "WIFI_P2P_PEERS_CHANGED_ACTION");
            if(p2pManager!=null){
                p2pManager.requestPeers(p2pChannel, mainActivity.peerListListener);
            }
        } else if(p2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "3 onReceive() called " + "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            if(p2pManager==null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                p2pManager.requestConnectionInfo(p2pChannel, mainActivity.connectionInfoListener);
            } else {
                mainActivity.setStatus("Device Disconnected.");
            }
        } else if(p2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            Log.d(TAG, "4 onReceive() called " + "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
        }

    }
}