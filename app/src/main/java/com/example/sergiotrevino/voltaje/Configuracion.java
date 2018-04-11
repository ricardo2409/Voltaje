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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        System.out.println("OnCreate");


        /*
        Intent intent = getIntent();
        String netID = intent.getStringExtra("NetID");
        String nodeID = intent.getStringExtra("NodeID");
        String potencia = intent.getStringExtra("Potencia");
        System.out.println("Esto tiene value: " + netID);
        */


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
        String netID, nodeID, potencia;
        netID = intent.getStringExtra("NetID");
        nodeID = intent.getStringExtra("NodeID");
        potencia = intent.getStringExtra("Potencia");
        System.out.println("Esto tiene netID: " + netID);
        System.out.println("Esto tiene nodeID: " + nodeID);
        System.out.println("Esto tiene Potencia: " + potencia);

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
                System.out.println("Botón Configurar Valores");
                //Valida y Escribe los valores
                String stringPotencia, stringNetID, stringNodeID, stringDestination;
                if (!etPotencia.getText().toString().matches("") && !etNodeID.getText().toString().matches("") && !etNetID.getText().toString().matches("")) {
                    stringPotencia = etPotencia.getText().toString();
                    stringNetID = etNetID.getText().toString();
                    stringNodeID = etNodeID.getText().toString();

                    if(Integer.parseInt(stringPotencia.trim()) >= 0 && Integer.parseInt(stringPotencia.trim()) <= 30 && Integer.parseInt(stringNetID.trim()) >= 0 && Integer.parseInt(stringNetID.trim()) <= 65535 && Integer.parseInt(stringNodeID.trim()) >= 0 && Integer.parseInt(stringNodeID.trim()) <= 30){

                        if(Integer.parseInt(stringNodeID.trim()) == 0){
                            System.out.println("NodeID igual a 0");
                            stringDestination = "65535";
                        }else{
                            System.out.println("NodeID diferente a 0");
                            stringDestination = "0";
                        }

                       Intent intent=new Intent();
                       intent.putExtra("Potencia",stringPotencia);
                       intent.putExtra("NetID",stringNetID);
                       intent.putExtra("NodeID",stringNodeID);
                       intent.putExtra("Destination",stringDestination);


                       setResult(2,intent);
                       finish();//finishing activity


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
