package com.example.kulde.instagram.share;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kulde.instagram.R;
//import com.example.kulde.instagram.Utils.DownloadTask;
import com.example.kulde.instagram.Utils.CommResources;
import com.example.kulde.instagram.Utils.WDBroadcastReciever;
import com.example.kulde.instagram.camera.FilterActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class WifiDirectActivity extends AppCompatActivity {

    public static final String TAG = "WifiActivity";
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static final int MESSAGE_READ = 1;

    private TextView connectionStatus, displayMessage;
    private Button buttonToggleWifi, buttonDiscover;
    private ListView listDevices;
    private ImageView imgview;

    WifiManager wifimanager;
    WifiP2pManager p2pManager;
    WifiP2pManager.Channel p2pChannel;

    WDBroadcastReciever bcastReciever;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNames;
    WifiP2pDevice[] devices;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_direct);
        if (checkPermission()) {
            requestPermissionAndContinue();
        }

        initWidgets();
        initButtonListener();
        updateImage();
    }

    private void updateImage() {
        Bitmap bmp;
        if(CommResources.edit_template != null){
            bmp = CommResources.edit_template;
        }else{
            bmp = CommResources.photoFinishBitmap;
        }
        imgview.setImageBitmap(bmp);
        imgview.setRotation(- CommResources.rotationdegree);

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_READ:
                    byte[] readbuff = (byte[]) msg.obj;
                    String tempMsg = new String(readbuff, 0, msg.arg1);
                    displayMessage.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Storage permission is required!");
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(WifiDirectActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(WifiDirectActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    private void initButtonListener(){
        this.buttonToggleWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifimanager.isWifiEnabled()){
                    wifimanager.setWifiEnabled(false);
                    buttonToggleWifi.setText("ON");
                } else {
                    wifimanager.setWifiEnabled(true);
                    buttonToggleWifi.setText("OFF");
                }
            }
        });

        this.buttonDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess() called");
                        connectionStatus.setText("Discovering ...");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "onFailure() called with: reason = [" + reason + "]");
                        connectionStatus.setText("Discovery failed to start");
                    }
                });
            }
        });

        listDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = devices[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Can not connect", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
            }
        });


    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerlist) {
            Collection<WifiP2pDevice> refreshedPeers = peerlist.getDeviceList();
            Log.d(TAG, "onPeersAvailable() called with: refreshedPeers = [" + refreshedPeers.size() + "]");
            if(!peerlist.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerlist.getDeviceList());
                deviceNames = new String[peerlist.getDeviceList().size()];
                devices = new WifiP2pDevice[peerlist.getDeviceList().size()];
                int i = 0;

                for(WifiP2pDevice device : peerlist.getDeviceList()){
                    deviceNames[i] = device.deviceName;
                    devices[i] = device;
                    i++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNames);
                listDevices.setAdapter(adapter);
            }

            if(peers.size() == 0){
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddr = info.groupOwnerAddress;
            if(info.groupFormed && info.isGroupOwner){
                connectionStatus.setText("now Host");
                serverClass = new ServerClass();
                serverClass.start();
            } else if(info.groupFormed){
                connectionStatus.setText("now Client");
                clientClass = new ClientClass(groupOwnerAddr);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(bcastReciever, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bcastReciever);
    }

    private void initWidgets() {
        this.wifimanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        this.p2pChannel = p2pManager.initialize(this, getMainLooper(), null);
        this.bcastReciever = new WDBroadcastReciever(p2pManager, p2pChannel, WifiDirectActivity.this);

        this.connectionStatus = findViewById(R.id.TextStatus);
        this.buttonToggleWifi = findViewById(R.id.ButtonToggleWifi);
        if(wifimanager.isWifiEnabled()){
            buttonToggleWifi.setText("OFF");
        } else {
            buttonToggleWifi.setText("ON");
        }
        this.buttonDiscover = findViewById(R.id.ButtonDiscover);
        this.listDevices = findViewById(R.id.listNode);
        this.imgview = findViewById(R.id.imageShare);

        intentFilter = new IntentFilter();
        intentFilter.addAction(p2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(p2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    public void setStatus(String s) {
        this.connectionStatus.setText(s);
    }

    public void setDisplayMessage(String s) {
        this.displayMessage.setText(s);
    }

    public class ServerClass extends Thread {

        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try{
                serverSocket = new ServerSocket(3737);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
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

    public class ClientClass extends Thread {

        Socket socket;
        String hostIP;

        public ClientClass(InetAddress hostIP) {
            this.hostIP = hostIP.getHostAddress();
            this.socket = new Socket();
        }

        public void closeSocket(){
            try {
//                sendReceive.stop();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                this.socket.connect(new InetSocketAddress(this.hostIP, 3737), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class SendReceive extends Thread {

        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket socket) {
            this.socket = socket;
            try{
                this.inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket!= null){
                try{
                    bytes = inputStream.read(buffer);
                    if(bytes > 0){
                        handler.obtainMessage(MESSAGE_READ, bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes){
            try {
//                Log.d(TAG,"The photo info is "+ bytes);
                this.outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
