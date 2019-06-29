package com.eugencz.bluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "robotellog";


    Handler handler;
    private StringBuilder sb = new StringBuilder();
    String sbprint = null;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int RECIEVE_MESSAGE = 1;

    private  static InputStream InStream;
    private  static OutputStream OutStream;

    private BluetoothAdapter btAdapter = null;
    static BluetoothDevice btDevice = null;
    public static BluetoothSocket btSocket = null;

    private static ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Context ctx;
    Vibrator vibrator;


    Button left, right, forward, backward, plus, minus, send;
    ImageView ivLeftSensor, ivCenterSensor, ivRightSensor;
    EditText pass;
    TextView batteryLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onResume(){
        super.onResume();

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("HC-05")) //match the name of your device
                {
                    Log.d(TAG,device.getName());
                    btDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "try connect...");
        try {
            btSocket = createBluetoothSocket(btDevice);
        } catch (IOException e) {
            Log.d("Fatal Error", "socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();


        // Establish the connection.  This will block until it connects.
        Log.d(TAG, "...Connecting...");
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
            Toast.makeText(MainActivity.this, "Connection OK", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            try {
                btSocket.close();
                Log.d(TAG, "....Connection not ok...");
                Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
            } catch (IOException e2) {
                Log.d("Fatal Error", "unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }
        
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                           // if end-of-line,
                            sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());

                            Log.d(TAG, "am primit "+sbprint);
                            parseMessage(sbprint);


                        }

                        break;
                }
            };
        };
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        ivLeftSensor = findViewById(R.id.ivLeftSensor);
        ivCenterSensor = findViewById(R.id.ivCenterSensor);
        ivRightSensor = findViewById(R.id.ivRightSensor);
        pass = findViewById(R.id.pass);
        send = findViewById(R.id.send);
        batteryLevel = findViewById(R.id.batteryLevel);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(pass.getText().toString());
                vibrator.vibrate(100); // 100 miliseconds
            }
        });
        plus = findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("i");
                vibrator.vibrate(100); // 100 miliseconds
            }
        });
        minus = findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrator.vibrate(100); // 100 miliseconds
                sendMessage("d");
            }
        });
        left = findViewById(R.id.left);

        
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    vibrator.vibrate(200); // 200 miliseconds
                    Log.d(TAG, "send: "+"l");
                    sendMessage("l");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    vibrator.vibrate(200); // 200 miliseconds
                    Log.d(TAG, "send: "+"s");
                    sendMessage("s");
                }
                return true;
            }
        });

        right = findViewById(R.id.right);
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(200); // 200 miliseconds
                    Log.d(TAG, "send: "+"r");
                    sendMessage("r");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    vibrator.vibrate(200); // 200 miliseconds
                    Log.d(TAG, "send: "+"s");
                    sendMessage("s");
                }
                return true;
            }
        });

        backward = findViewById(R.id.backward);

        
        backward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(100); // 100 miliseconds
                    Log.d(TAG, "send: "+"b");
                    sendMessage("b");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    vibrator.vibrate(100); // 100 miliseconds
                    Log.d(TAG, "send: "+"s");
                    sendMessage("s");
                }
                return true;
            }
        });


        forward = findViewById(R.id.forward);

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    vibrator.vibrate(200); // 100 miliseconds
                    Log.d(TAG, "send: "+"f");
                    sendMessage("f");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    vibrator.vibrate(200); // 100 miliseconds
                    Log.d(TAG, "send: "+"s");
                    sendMessage("s");
                }
                return true;
            }
        });
    }
    private class ConnectedThread extends Thread {


        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            InStream = tmpIn;
            OutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()
            
            while (true) {
                try {
                    if(btSocket == null){
                        Log.d(TAG, "Socket is closed! Restart socket!");
                        try {
                            btSocket = createBluetoothSocket(btDevice);
                        } catch (IOException e) {
                            Log.d("Fatal Error", "socket create failed: " + e.getMessage() + ".");
                        }
                    }
                    // Read from the InputStream
                    bytes = InStream.read(buffer);        // Get number of bytes and message in "buffer"
                    handle.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message);
            byte[] msgBuffer = message.getBytes();
            try {
                OutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
                btSocket =null;
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.d(TAG, "Could not create Insecure RFComm Connection",e);
                //finish();
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    private void checkBTState() {
        if(btAdapter==null) {
            Log.e("Fatal Error", "Bluetooth not supported");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                Log.d(TAG, "...Bluetooth OFF...");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void sendMessage(String message){
        mConnectedThread.write(message);
    }

    private void parseMessage(String message){
        try {
            String[] parts = message.split(" ");
            String strLeftSensor = parts[0]; // left
            String strCenterSensor = parts[1]; // center
            String strRightSensor = parts[2]; // right
            String battery = parts[3]; // battery

            double leftSensor = Double.parseDouble(strLeftSensor);
            double centerSensor = Double.parseDouble(strCenterSensor);
            double rightSensor = Double.parseDouble(strRightSensor);

            if(leftSensor ==0){
                ivLeftSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor1, getApplicationContext().getTheme()));
            }else if(leftSensor == 25){
                ivLeftSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor2, getApplicationContext().getTheme()));
            }else if (leftSensor == 50){
                ivLeftSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor3, getApplicationContext().getTheme()));
            }else if(leftSensor == 75){
                ivLeftSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor4, getApplicationContext().getTheme()));
            }else if(leftSensor == 100){
                ivLeftSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor5, getApplicationContext().getTheme()));
            }

            if(rightSensor ==0){
                ivRightSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor1, getApplicationContext().getTheme()));
            }else if(rightSensor == 25){
                ivRightSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor2, getApplicationContext().getTheme()));
            }else if (rightSensor == 50){
                ivRightSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor3, getApplicationContext().getTheme()));
            }else if(rightSensor == 75){
                ivRightSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor4, getApplicationContext().getTheme()));
            }else if(rightSensor == 100){
                ivRightSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor5, getApplicationContext().getTheme()));
            }
            
            if(centerSensor ==0){
                ivCenterSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor1, getApplicationContext().getTheme()));
            }else if(centerSensor == 25){
                ivCenterSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor2, getApplicationContext().getTheme()));
            }else if (centerSensor == 50){
                ivCenterSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor3, getApplicationContext().getTheme()));
            }else if(centerSensor == 75){
                ivCenterSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor4, getApplicationContext().getTheme()));
            }else if(centerSensor == 100){
                ivCenterSensor.setImageDrawable(getResources().getDrawable(R.drawable.sensor5, getApplicationContext().getTheme()));
            }

            batteryLevel.setText(battery);
        }
        catch (Exception e){
            Log.d(TAG, "error occured");
        }
    }
}
