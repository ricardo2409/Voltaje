package com.example.sergiotrevino.voltaje;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class Configuracion extends AppCompatActivity implements View.OnClickListener {

    EditText etNodeID, etNetID, etPotencia;
    Button btnConfigurar;
    private MainActivity mainActivity;
    private Configuracion(MainActivity activity) {
        mainActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        System.out.println("OnCreate");


        Intent intent = getIntent();
        String netID = intent.getStringExtra("NetID");
        String nodeID = intent.getStringExtra("NodeID");
        String potencia = intent.getStringExtra("Potencia");
        System.out.println("Esto tiene value: " + netID);


        etNodeID = (EditText) findViewById(R.id.etNodeID);
        etNetID = (EditText) findViewById(R.id.etNetID);
        etPotencia = (EditText) findViewById(R.id.etPotencia);

        btnConfigurar = (Button) findViewById(R.id.btnConfigurar);
        btnConfigurar.setOnClickListener(this);



    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("OnStart");
        Intent intent = getIntent();
        String netID = intent.getStringExtra("NetID");
        String nodeID = intent.getStringExtra("NodeID");
        String potencia = intent.getStringExtra("Potencia");
        etNetID.setText(netID);
        etNodeID.setText(nodeID);
        etPotencia.setText(potencia);
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(Configuracion.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfigurar:
                //Valida y Escribe los valores
                if (!etPotencia.getText().toString().matches("") && !etNodeID.getText().toString().matches("") && !etNetID.getText().toString().matches("")) {
                   if(Integer.parseInt(etPotencia.getText().toString()) > 0 && Integer.parseInt(etPotencia.getText().toString()) < 999 && Integer.parseInt(etNodeID.getText().toString()) > 0 && Integer.parseInt(etNodeID.getText().toString()) < 100000 && Integer.parseInt(etNetID.getText().toString()) > 0 && Integer.parseInt(etPotencia.getText().toString()) < 999){
                       //Escribe los valores
                       try{
                           mainActivity.writeNetID(etNetID.getText().toString());
                           mainActivity.writeNodeID(etNodeID.getText().toString());
                           mainActivity.writePower(etPotencia.getText().toString());
                           mainActivity.saveValues();

                       }catch (IOException e){}
                   }else{
                       showToast("Números fuera del límite");
                   }

                } else {
                    showToast("Favor de llenar todos los campos");
                }
                break;
        }
    }
}
