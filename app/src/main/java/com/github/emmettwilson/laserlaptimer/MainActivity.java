package com.github.emmettwilson.laserlaptimer;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private UDPReceiver udpReceiver;
    private MediaPlayer mediaPlayer;
    private long[] times = {0,0,0,0,0};
    private int index = 0;
    private View startEventButton;
    private View resetEventButton;
    private View eventData;
    private View countdown;
    private TextView lap1;
    private TextView lap2;
    private TextView lap3;
    private TextView lap4;
    private TextView total;
    private List<TextView> textViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startEventButton = findViewById(R.id.start_event);
        resetEventButton = findViewById(R.id.reset);
        eventData = findViewById(R.id.event_data);
        countdown = findViewById(R.id.countdown);
        textViewList = new ArrayList<>();

        lap1 = (TextView) findViewById(R.id.lap_1_tv);
        lap2 = (TextView)findViewById(R.id.lap_2_tv);
        lap3 = (TextView)findViewById(R.id.lap_3_tv);
        lap4 = (TextView)findViewById(R.id.lap_4_tv);
        total = (TextView)findViewById(R.id.total_tv);

        textViewList.add(lap1);
        textViewList.add(lap2);
        textViewList.add(lap3);
        textViewList.add(lap4);

    }

    @Override
    protected void onStart() {

        super.onStart();


        mediaPlayer = MediaPlayer.create(this, R.raw.gate);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                countdown.setVisibility(View.GONE);
                eventData.setVisibility(View.VISIBLE);
                times[0] = System.currentTimeMillis();
                udpReceiver = new UDPReceiver();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        udpReceiver.execute();
                    }
                }, 1000);
            }
        });

        startEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                startEventButton.setVisibility(View.INVISIBLE);
                countdown.setVisibility(View.VISIBLE);
            }
        });

        resetEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEventButton.setVisibility(View.VISIBLE);
                resetEventButton.setVisibility(View.GONE);
                eventData.setVisibility(View.GONE);
                for (TextView textView : textViewList){
                    textView.setText("");
                }
                total.setText("");
                index = 0;
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        udpReceiver.cancel(true);
    }


    //NOTE: To send a broadcast run this from a terminal emulator
    //$ echo Some Message | socat - UDP-DATAGRAM:192.168.1.255:9876,broadcast

    private class UDPReceiver extends AsyncTask<String, Void, String> {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

                    if(index < 4){
                        index ++;
                        times[index] = System.currentTimeMillis();
                        Looper mainLooper = Looper.getMainLooper();
                        Handler handler = new Handler(mainLooper);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateView();
                            }
                        });
                    } else {
                        return "finished";
                    }

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

    private void updateView() {

        if(index > 0){
            for(int i = 0; i < index; i++){
                long lapStart = times[i];
                long lapEnd = times[i+1];
                long accumulatedTime = lapEnd - lapStart;
                int secs = (int) (accumulatedTime / 1000);
                int millis = (int) (accumulatedTime % 1000);
                textViewList.get(i).setText( String.format("%02d", secs) + "." + String.format("%03d", millis));
            }
        }

        if(index == 4){
            long lapStart = times[0];
            long lapEnd = times[4];
            long accumulatedTime = lapEnd - lapStart;
            int secs = (int) (accumulatedTime / 1000);
            int millis = (int) (accumulatedTime % 1000);
            total.setText(String.format("%02d", secs) + "." + String.format("%03d", millis));
            resetEventButton.setVisibility(View.VISIBLE);
        }


    }

}
