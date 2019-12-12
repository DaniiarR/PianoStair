package kg.flaterlab.pianostairs;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity implements SoundPool.OnLoadCompleteListener {

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service

    final int MAX_STREAMS = 5;

    SoundPool sp;

    int[] notes;

    TextView myLabel;
    EditText myTextbox;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    HashMap<String, Integer>  ints;
    volatile boolean stopWorker;

    Date date;
    long[] last;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button openButton = findViewById(R.id.open);
        Button closeButton = findViewById(R.id.close);
        Button cButton = findViewById(R.id.cButton);
        Button dButton = findViewById(R.id.dButton);
        Button eButton = findViewById(R.id.eButton);
        Button fButton = findViewById(R.id.fButton);
        Button gButton = findViewById(R.id.gButton);
        Button aButton = findViewById(R.id.aButton);
        Button bButton = findViewById(R.id.bButton);
        Button c2Button = findViewById(R.id.c2Button);
        myLabel = (TextView)findViewById(R.id.label);

        last = new long[6];

        ints = new HashMap<String, Integer>();

        ints.put("1", 0);
        ints.put("2", 1);
        ints.put("3", 2);
        ints.put("4", 3);
        ints.put("5", 4);
        ints.put("6", 5);


        sp = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(this);

        notes = new int[8];

        notes[0] = sp.load(this, R.raw.c1, 1);
        notes[1] = sp.load(this, R.raw.d, 2);
        notes[2] = sp.load(this, R.raw.e, 3);
        notes[3] = sp.load(this, R.raw.f, 4);
        notes[4] = sp.load(this, R.raw.g, 5);
        notes[5] = sp.load(this, R.raw.a, 6);
        notes[6] = sp.load(this, R.raw.b, 7);
        notes[7] = sp.load(this, R.raw.c, 8);

        //Open Button
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });

        cButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('c');
            }
        });
        dButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('d');
            }
        });
        eButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('e');
            }
        });
        fButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('f');
            }
        });
        gButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('g');
            }
        });
        aButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('a');
            }
        });
        bButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('b');
            }
        });
        c2Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playClick('x');
            }
        });



        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
    }

    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        Log.d(TAG, "onLoadComplete, sampleId = " + sampleId + ", status = " + status);
    }

    void findBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            myLabel.setText("No bluetooth adapter available");
        }

        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().equals("HC-06")) {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

        myLabel.setText("Bluetooth Opened");

    }

    public void playClick(char note) {
//        date = new Date();
//        long now = date.getTime();
//        if(now - last[num] > 200) {
//            sp.play(notes[num], 1, 1, 0, 0, 1);
//            last[num] = now;
//        }

        switch (note) {
            case 'c':
                sp.play(notes[0], 1, 1, 0, 0, 1);
                break;
            case 'd':
                sp.play(notes[1], 1, 1, 0, 0, 1);
                break;
            case 'e':
                sp.play(notes[2], 1, 1, 0, 0, 1);
                break;
            case 'f':
                sp.play(notes[3], 1, 1, 0, 0, 1);
                break;
            case 'g':
                sp.play(notes[4], 1, 1, 0, 0, 1);
                break;
            case 'a':
                sp.play(notes[5], 1, 1, 0, 0, 1);
                break;
            case 'b':
                sp.play(notes[6], 1, 1, 0, 0, 1);
                break;
            case 'x':
                sp.play(notes[7], 1, 1, 0, 0, 1);
                break;
        }
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, StandardCharsets.UTF_8);
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            char a = data.charAt(0);
                                            playClick(a);
                                        }
                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }



    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }


}