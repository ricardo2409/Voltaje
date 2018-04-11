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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    static String atributo = "NetID";

    static boolean socketConectado;
    static int controlread = 1;
    String netIDValue, potenciaValue, nodeIDvalue;

    ArrayList<String> list = new ArrayList<String>();
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
                        final int byteCount = inputStream.available();
                        if(byteCount > 0) {
                            byte[] packetBytes = new byte[byteCount];
                            inputStream.read(packetBytes);
                            s = new String(packetBytes);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(s.contains("s,")){
                                        if(s.length() >= 26 && s.length() <= 34){
                                            a = s.substring(s.indexOf("s,"), s.length() - 1);
                                            readMessage(a);
                                        }
                                    }else{
                                        System.out.println("No es status: " + s + " length: " + s.length());

                                        if(control.matches("Config")){
                                            System.out.println("Config");
                                            System.out.println("Este es el atributo: " + atributo);

                                            switch (atributo){
                                                case "Power":
                                                    if(s.length() >= 8 && s.contains("]")){
                                                        readPower(s);
                                                    }else{
                                                        System.out.println("No es lo que quiero de Power" + s.length());
                                                    }
                                                    break;
                                                case "NetID":
                                                    if(s.length() >= 7 && s.contains("]")){
                                                        readNetID(s);
                                                    }else{
                                                        System.out.println("No es lo que quiero de NetID " + s.length());
                                                    }
                                                    break;
                                                case "NodeID":
                                                    if(s.length() >= 8 && s.contains("]")){
                                                        readNodeID(s);
                                                    }else{
                                                        System.out.println("No es lo que quiero de NodeID " + s.length());
                                                    }
                                                    break;
                                            }


                                        }
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

    void sendCommand() throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    System.out.println("Estoy en sendCommand");
                    String msg = "+++";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 100);
    }

    void sendNetID() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    //Control para saber que leer
                    control = "Config";
                    //Lo que lee es sobre el netid
                    atributo = "NetID";
                    System.out.println("Estoy en sendNetID");
                    String msg = "ATS3?\r";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 500);
    }

    void sendPower() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "Power";
                    System.out.println("Estoy en sendPower");
                    String msg = "ATS4?\r";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1000);
    }

    void sendNodeID() throws IOException
    {
        Runnable r = new Runnable() {
            @Override
            public void run(){
                try
                {
                    atributo = "NodeID";
                    System.out.println("Estoy en sendNodeID");
                    String msg = "ATS15?\r";
                    outputStream.write(msg.getBytes());
                }
                catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 2000);
    }

    void sendRadOn() throws IOException{

        System.out.println("Estoy en el RadOn");
        //Para evitar que siga mandando la cadena y poder entrar al radio
        String msg1 = "$RadOn,&";
        outputStream.write(msg1.getBytes()); //<-- put your code in here.
    }

    void sendRadOff() throws IOException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Estoy en el RadOff");
                    //Para evitar que siga mandando la cadena y poder entrar al radio
                    String msg1 = "$RadOff,&";
                    outputStream.write(msg1.getBytes());
                    changeStatus();
                } catch (IOException ex) {
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 1000);
    }


    void readPower(final String line){
        Runnable r = new Runnable() {
            @Override
            public void run(){

                atributo = "NodeID";
                System.out.println("Esta es la linea que lee Power: " + line + " Este es su tamaño: " + line.length());
                if(line.length() > 5){
                    potenciaValue = line.substring(line.lastIndexOf("]") + 2, line.length() - 1);
                    System.out.println("Esto tiene potenciaValue: " + potenciaValue);
                }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 10);

    }

    void readNetID(final String line){

        Runnable r = new Runnable() {
            @Override
            public void run(){
                atributo = "Power";
                System.out.println("Esta es la linea que lee NetID: " + line + " Este es su tamaño: " + line.length());
                if(line.length() > 5){
                    netIDValue = line.substring(line.lastIndexOf("]") + 2, line.length() - 1);
                    System.out.println("Esto tiene netIDvalue: " + netIDValue);
                }
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 30);
    }

    void readNodeID(final String line){
        Runnable r = new Runnable() {
            @Override
            public void run(){

                    System.out.println("NodeID: " + line);
                    System.out.println("Esta es la linea que lee NodeID: " + line + " Este es su tamaño: " + line.length());
                    if(line.length() > 5){
                        nodeIDvalue = line.substring(line.lastIndexOf("]") + 2, line.length() - 1);
                        System.out.println("Esto tiene nodeIDvalue: " + nodeIDvalue);
                    }
                    try
                    {
                        sendRadOff();

                    }
                    catch (IOException ex) { }

            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 20);



    }

    void changeStatus(){
        Runnable r = new Runnable() {
            @Override
            public void run() {

                control = "Status";
                System.out.println("Status changed");

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 400);
    }

    public static boolean isStringANumber(String str) {
        String regularExpression = "[-+]?[0-9]*\\.?[0-9]+$";
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();

    }

    public static void readMessage(String frase)  {

        tokens = frase.split(",");
        System.out.println("Tokens: " + Arrays.toString(tokens));
        if (frase.contains("s") && tokens.length > 6) {
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
            System.out.println("Socket Desconectado");
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

    public void executeIntent(){

        Runnable r = new Runnable() {
            @Override
            public void run(){

                //Intent a ConfigurarActivity
                Intent myIntent = new Intent(MainActivity.this, Configuracion.class);
                System.out.println("Esto es lo que pongo en el intent de netIDvalue: " + netIDValue);
                System.out.println("Esto es lo que pongo en el intent de nodeIDvalue: " + nodeIDvalue);
                System.out.println("Esto es lo que pongo en el intent de powerValue: " + potenciaValue);

                //Datos que se leen y mandan a la otra activity
                myIntent.putExtra("NetID", netIDValue);
                myIntent.putExtra("NodeID", nodeIDvalue);
                myIntent.putExtra("Potencia", potenciaValue);
                MainActivity.this.startActivityForResult(myIntent, 2);

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 3500);


    }

    public void writeNetID(final String netID) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteNetID");
                    String msg1 = "ATS3=" + netID + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 600);

    }

    public void writeNodeID(final String nodeID) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteNodeID");
                    String msg1 = "ATS15=" + nodeID + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 500);

    }

    public void writePower(final String power) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WritePower");
                    String msg1 = "ATS4=" + power + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 400);

    }

    public void writeDestination(final String destination) throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el WriteDestination");
                    String msg1 = "ATS16=" + destination + "\r";
                    System.out.println(msg1);
                    outputStream.write(msg1.getBytes());

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 700);

    }

    public void saveValues() throws IOException{
        Runnable r = new Runnable() {
            @Override
            public void run(){

                try {
                    System.out.println("Estoy en el SaveValues");
                    String msg1 = "AT&W\r";
                    outputStream.write(msg1.getBytes());
                    sendRadOff();
                    showToast("¡ Configuración Guardada !");

                } catch (IOException ex) {
                }

            }
        };

        Handler h = new Handler();
        h.postDelayed(r, 800);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("Estoy en el Acitivity Result");
        System.out.println("Result code: " + resultCode);
        System.out.println("Request code: " + requestCode);


        atributo = "NetID";
        if(resultCode==2)
        {
            //do the things u wanted
            String potencia=data.getExtras().getString("Potencia");
            String netID=data.getExtras().getString("NetID");
            String nodeID=data.getExtras().getString("NodeID");
            String destination=data.getExtras().getString("Destination");
            System.out.println("Estos son los valores que recibí: Potencia, netID, nodeID, destination " + potencia + " " + netID + " " + nodeID + " " + destination);
            try {
                sendRadOn();
                sendCommand();
                writePower(potencia);
                writeNodeID(nodeID);
                writeNetID(netID);
                writeDestination(destination);
                saveValues();


            } catch (IOException ex) {
            }

        }
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
                    try
                    {
                        System.out.println("Boton Configurar");
                        sendRadOn();
                        sendCommand();
                        //control = "Config";
                        sendNetID();
                        sendPower();
                        sendNodeID();
                        executeIntent();
                    }
                    catch (IOException ex) { }
                }else{
                    showToast("Bluetooth desconectado");
                }
                break;
        }
    }
}
