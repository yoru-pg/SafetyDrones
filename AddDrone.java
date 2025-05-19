package com.example.safetydrones.Drones;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetydrones.Model.Model_Drone;
import com.example.safetydrones.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddDrone extends AppCompatActivity {

    private EditText droneNome, droneModelo, dronePeso, droneAutonomia, droneVelocidade;
    private Button addDroneButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_adddrone);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Drones");

        droneNome = findViewById(R.id.drone_nome);
        droneModelo = findViewById(R.id.drone_modelo);
        dronePeso = findViewById(R.id.drone_peso);
        droneAutonomia = findViewById(R.id.drone_autonomia);
        droneVelocidade = findViewById(R.id.drone_velocidade);
        addDroneButton = findViewById(R.id.add_drone_button);

        addDroneButton.setOnClickListener(v -> addDrone());
    }

    private void addDrone() {
        String nome = droneNome.getText().toString().trim();
        String modelo = droneModelo.getText().toString().trim();
        String pesoStr = dronePeso.getText().toString().trim();
        String autonomiaStr = droneAutonomia.getText().toString().trim();
        String velocidadeStr = droneVelocidade.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(modelo) || TextUtils.isEmpty(pesoStr) ||
                TextUtils.isEmpty(autonomiaStr) || TextUtils.isEmpty(velocidadeStr)) {
            Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNumeric(pesoStr) || !isNumeric(velocidadeStr) || !isInteger(autonomiaStr)) {
            Toast.makeText(this, "Peso e velocidade devem ser números válidos!\nAutonomia deve ser um número inteiro.", Toast.LENGTH_LONG).show();
            return;
        }

        double peso = Double.parseDouble(pesoStr);
        double velocidade = Double.parseDouble(velocidadeStr);
        int autonomia = Integer.parseInt(autonomiaStr);

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Erro: Utilizador não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        Model_Drone.adicionaDrone(userId, nome, modelo, peso, autonomia, velocidade, mDatabase, new Model_Drone.FirebaseCallback() {
            @Override
            public void onSuccess(String droneId) {
                Toast.makeText(AddDrone.this, "Drone adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                getSharedPreferences("safetydrones_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("last_added_drone", nome + " - " + modelo)
                        .apply();

                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddDrone.this, "Erro ao adicionar drone: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Métodos de validação
    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private boolean isInteger(String str) {
        return str.matches("-?\\d+");
    }
}
