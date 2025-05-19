package com.example.safetydrones.Drones;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safetydrones.Model.Model_Drone;
import com.example.safetydrones.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDrone extends AppCompatActivity {

    private TextView drone_titulo;
    private EditText droneNome, droneModelo, dronePeso, droneAutonomia, droneVelocidade;
    private Button editDroneButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private String droneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_editdrone);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Drones");

        drone_titulo = findViewById(R.id.app_name);
        droneNome = findViewById(R.id.drone_nome);
        droneModelo = findViewById(R.id.drone_modelo);
        dronePeso = findViewById(R.id.drone_peso);
        droneAutonomia = findViewById(R.id.drone_autonomia);
        droneVelocidade = findViewById(R.id.drone_velocidade);
        editDroneButton = findViewById(R.id.add_drone_button);
        editDroneButton.setText("Editar");

        // Receber dados da Intent
        Intent intent = getIntent();
        droneId = intent.getStringExtra("droneId");
        String nome = intent.getStringExtra("nome");
        String modelo = intent.getStringExtra("modelo");
        String peso = intent.getStringExtra("peso");
        String autonomia = intent.getStringExtra("autonomia");
        String velocidade = intent.getStringExtra("velocidade");

        if (nome != null) {
            drone_titulo.setText(nome);
        }

        // Preencher os campos com dados
        droneNome.setText(nome);
        droneModelo.setText(modelo);
        dronePeso.setText(formatarNumero(peso));
        droneAutonomia.setText(autonomia);
        droneVelocidade.setText(formatarNumero(velocidade));

        editDroneButton.setOnClickListener(v -> updateDrone());
    }


    private String formatarNumero(String numero) {
        if (numero == null || numero.isEmpty()) return "";
        try {
            double valor = Double.parseDouble(numero);
            if (valor == (long) valor) {
                return String.format("%d", (long) valor);
            } else {
                return String.format("%s", valor);
            }
        } catch (NumberFormatException e) {
            return numero; // Retorna como está se não for número válido
        }
    }

    // Atualiza os dados do drone
    private void updateDrone() {
        String nome = droneNome.getText().toString().trim();
        String modelo = droneModelo.getText().toString().trim();
        String pesoStr = dronePeso.getText().toString().trim();
        String autonomiaStr = droneAutonomia.getText().toString().trim();
        String velocidadeStr = droneVelocidade.getText().toString().trim();

        String validatorError = Model_Drone.validarDrone(nome, modelo, pesoStr, autonomiaStr, velocidadeStr);
        if (validatorError != null) {
            Toast.makeText(this, validatorError, Toast.LENGTH_SHORT).show();
            return;
        }

        double peso = Double.parseDouble(pesoStr);
        double velocidade = Double.parseDouble(velocidadeStr);
        int autonomia = Integer.parseInt(autonomiaStr);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Utilizador não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Model_Drone.atualizarDrone(userId, droneId, nome, modelo, peso, autonomia, velocidade, mDatabase, new Model_Drone.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(EditDrone.this, message, Toast.LENGTH_SHORT).show();
                getSharedPreferences("safetydrones_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("ultimoDroneEditadoId", droneId)
                        .apply();
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditDrone.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
