package com.github.emmettwilson.laserlaptimer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private UDPReceiver udpReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





    }

    @Override
    protected void onStart() {
        super.onStart();
        udpReceiver = new UDPReceiver();

        udpReceiver.execute();

    }


    @Override
    protected void onStop() {
        super.onStop();
        udpReceiver.cancel(true);
    }


    //NOTE: To send a broadcast run this from a terminal emulator
    //$ echo Some Message | socat - UDP-DATAGRAM:192.168.1.255:9876,broadcast

    private class UDPReceiver extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                DatagramSocket clientSocket=new DatagramSocket(9876);
                byte[] receivedData = new byte[1024];
                while(true)
                {
                    DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
                    clientSocket.receive(packet);
                    final String data = new String(packet.getData());

                    Looper mainLooper = Looper.getMainLooper();
                    Handler handler = new Handler(mainLooper);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } catch (Exception e) {
                Log.e("UDP", "S: Error", e);
            }

        return "Connection failed";
    }

    @Override
    protected void onPostExecute(String result) {
    }
}

}
