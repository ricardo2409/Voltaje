package com.example.sergiotrevino.voltaje;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Configuracion extends AppCompatActivity implements View.OnClickListener {

    EditText etNodeID, etNetID, etPotencia;
    Button btnConfigurar;

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

    @Override
    public void onClick(View v) {

    }
}
