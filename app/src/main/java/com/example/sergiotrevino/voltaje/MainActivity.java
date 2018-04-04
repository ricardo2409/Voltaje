package com.example.sergiotrevino.voltaje;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnConnect, btnOffset, btnVoltaje, btnConfigurar;
    static TextView tvVoltaje1, tvVoltaje2, tvVoltaje3;
    EditText etVoltaje;
    boolean connected = false;
    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static OutputStream outputStream;
    public static InputStream inputStream;
    boolean stopThread;
    static Thread thread;
    static Handler handler = new Handler();
    static String s;
    static String a;
    static String tokens[];

    static String control = "Status";
    static boolean socketConectado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnOffset = (Button) findViewById(R.id.btnOffset);
        btnVoltaje = (Button) findViewById(R.id.btnGanancia);
        btnConfigurar = (Button) findViewById(R.id.btnConfigurar);
        tvVoltaje1 = (TextView) findViewById(R.id.tvVoltaje1);
        tvVoltaje2 = (TextView) findViewById(R.id.tvVoltaje2);
        tvVoltaje3 = (TextView) findViewById(R.id.tvVoltaje3);
        etVoltaje = (EditText) findViewById(R.id.etGanancia);

        btnConnect.setOnClickListener(this);
        btnOffset.setOnClickListener(this);
        btnVoltaje.setOnClickListener(this);
        btnConfigurar.setOnClickListener(this);
    }

    //Identifica el device BT
    public boolean BTinit()
    {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Este dispositivo no soporta bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Favor de conectar un dispositivo", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {

                //Suponiendo que solo haya un bondedDevice
                device = iterator;
                found = true;
                //Toast.makeText(getApplicationContext(), "Conectado a: " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        return found;
    }

    //Conexión al device BT
    public boolean BTconnect()
    {
        try
        {
            conectar();
        }
        catch(IOException e)
        {
            Toast.makeText(getApplicationContext(), "Conexión no exitosa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            connected = false;
        }

        return connected;
    }

    public void conectar() throws IOException{
        socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Crea un socket para manejar la conexión
        socket.connect();
        socketConectado = true;
        Log.d("Socket ", String.valueOf(socket.isConnected()));
        Toast.makeText(getApplicationContext(), "Conexión exitosa", Toast.LENGTH_SHORT).show();
        connected = true;
        //tvConect.setText("Conectado a " + device.getName());
        btnConnect.setText("Desconectar módulo Bluetooth");
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        beginListenForData();

        //waitMs(5000);
        //closeSocket();

    }


    void beginListenForData() {
        stopThread = false;
        thread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        //waitMs(1000);
                        final int byteCount = inputStream.available();
                        if(byteCount > 0) {
                            //try{ thread.sleep(100); }catch(InterruptedException e){ }
                            byte[] packetBytes = new byte[byteCount];
                            inputStream.read(packetBytes);
                            s = new String(packetBytes);
                            System.out.println("Linea: " + s);
                            //waitMs(1000);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(s.contains("s,")){
                                        if(s.length() >= 26 && s.length() <= 34){
                                            //System.out.println("S: " + s);
                                            a = s.substring(s.indexOf("s,"), s.length() - 1);
                                            System.out.println("A: " + a);
                                            readMessage(a);
                                        }
                                    }else{
                                        System.out.println("Else");
                                        System.out.println("S: " + s);

                                        /*try
                                        {
                                            readBytesBufferedReader();

                                        }
                                        catch (IOException ex) { }
                                        */
                                    }
                                }
                            });
                        }
                    }
                    catch (IOException ex) {
                        stopThread = true;
                    }
                }
                System.out.println("Stop thread es true");
            }
        });
        thread.start();
    }

    void sendPlus() throws IOException{

        Runnable r = new Runnable() {
            @Override
            public void run(){

                try
                {
                    System.out.println("Estoy en sendPlus");
                    String msg = "+++";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 100);


    }

    //Manda "ATI5\r" al radio1
    void sendData() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try
                {
                    System.out.println("Estoy en sendData");
                    String msg = "ATI5\r";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 100);


    }

    //Lee lo que le manda el radio cuando recibe "ATI5\r"
    public void readBytesBufferedReader(String line) throws IOException{
        System.out.println("Estoy en readBytesBufferedReader");
        ArrayList<String> list = new ArrayList<String>();
        //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        for(int i = 0; i < 19; i++) {
            list.add(line);
            System.out.println(line);
            // process line
        }

        System.out.println(list);
        System.out.println();
        readValues(list);
        control = "Status";

    }

    //Función que recibe strings con todos los parámetros y retorna solo los valores
    public ArrayList<String> readValues(ArrayList<String> lista) {
        System.out.println("Estoy en readValues");
        ArrayList<String> valores = new ArrayList<String>();
        System.out.println("Size: " + lista.size());
        System.out.println("Lista: " + lista);

        for (int i = 0; i < lista.size(); i++) {
            switch (i) {
                case 0:
                    System.out.println("i: " + i);
                    System.out.println("ATI5");
                    break;
                case 1:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 2:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    //NET ID

                    break;
                case 3:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído 1: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).indexOf('=') + 3));
                    System.out.println("Numero leído 2: " + lista.get(i).substring(lista.get(i).length() - 2 , lista.get(i).length()));

                    //NODE ID

                    break;
                case 4:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 5:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));
                   //POWER
                    break;
                case 6:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 7:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 8:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 9:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 10:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 11:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 12:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 13:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 14:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 15:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));
                    //
                    break;
                case 16:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 17:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));

                    break;
                case 18:
                    System.out.println("i: " + i);
                    System.out.println(lista.get(i));
                    System.out.println("Numero leído: " + lista.get(i).substring(lista.get(i).indexOf('=') + 1, lista.get(i).length()));


                    break;
                default:
                    System.out.println("Error");
                    break;
            }

        }
        System.out.println(valores);
        return valores;

    }

    public static void readMessage(String frase)  {
        System.out.println("Estoy en el readMessage: ");
        System.out.println(frase);
        tokens = frase.split(",");
        System.out.println("Tokens: " + Arrays.toString(tokens));
        if (frase.contains("s") && tokens.length > 6) {
            //System.out.println("Contains Status : ");
            float voltaje1, voltaje2, voltaje3;
            voltaje1 = Float.parseFloat(tokens[1]);
            voltaje2 = Float.parseFloat(tokens[2]);
            voltaje3 = Float.parseFloat(tokens[3]);
            tvVoltaje1.setText(Float.toString(voltaje1) + " V");
            tvVoltaje2.setText(Float.toString(voltaje2) + " V");
            tvVoltaje3.setText(Float.toString(voltaje3) + " V");

            System.out.println("Se cambió los voltajes a: " + Float.toString(voltaje1) + " " + Float.toString(voltaje2) + " " + Float.toString(voltaje3) );

        }
    }

    public void desconectarBluetooth() throws IOException{
        //Desconectar bluetooth
        if(socketConectado){
            System.out.println("Socket Conectado");
            outputStream.close();
            outputStream = null;
            inputStream.close();
            inputStream = null;
            socket.close();
            socket = null;
        }
        resetFields();
        connected = false;
        btnConnect.setText("Conectar a módulo Bluetooth");
        device = null;
        stopThread = true;
        socketConectado = false;


    }

    public void resetFields(){
       tvVoltaje1.setText("0");
       tvVoltaje2.setText("0");
       tvVoltaje3.setText("0");

    }
    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void sendVoltaje(String voltaje) throws IOException{
        System.out.println("Estoy en sendVoltaje");
        String msg = "$AjGan," + voltaje + ",&";
        System.out.println("Este es el comando de Ganancia que mandé: " + msg);
        outputStream.write(msg.getBytes());
    }
    public void sendOffset() throws IOException{
        System.out.println("Estoy en sendOffset");
        String msg = "$AjOff&";
        outputStream.write(msg.getBytes());
    }

    void sendRadOn() throws IOException{

        Runnable r = new Runnable() {
            @Override
            public void run(){

                try
                {
                    System.out.println("Estoy en el RadOn de Register");
                    //Para evitar que siga mandando la cadena y poder entrar al radio
                    String msg1 = "$RadOn&";
                    outputStream.write(msg1.getBytes()); //<-- put your code in here.
                }
                catch (IOException ex) { }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConnect:
                if(!connected) {
                    if (BTinit()) {
                        BTconnect();
                    }
                }else{
                    try
                    {
                        desconectarBluetooth();
                    }
                    catch (IOException ex) { }
                }
                break;

            case R.id.btnOffset:
                if(connected) {
                    try
                    {
                        sendOffset();
                        showToast("Offset Configurado");
                    }
                    catch (IOException ex) { }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

            case R.id.btnGanancia:
                if(connected) {
                    if(!etVoltaje.getText().toString().matches("")){
                        try
                        {
                            sendVoltaje(etVoltaje.getText().toString());
                            showToast("Voltaje Configurado");
                        }
                        catch (IOException ex) { }
                    }else{
                        showToast("Campo vacío");
                    }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;

            case R.id.btnConfigurar:
                if(connected) {
                    //Intent a ConfigurarActivity
                    try
                    {
                        System.out.println("Boton Configurar");
                        sendRadOn();
                        sendPlus();
                        sendData();
                    }
                    catch (IOException ex) { }



                }else{
                    showToast("Bluetooth desconectado");
                }
                break;
        }
    }
}
