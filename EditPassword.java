package com.example.safetydrones.Config;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safetydrones.Controller.ConfigController;
import com.example.safetydrones.Main.LoginActivity;
import com.example.safetydrones.Model.Model_User;
import com.example.safetydrones.Model.Model_Zona;
import com.example.safetydrones.R;

public class EditPassword extends AppCompatActivity {

    private EditText editEmail;
    private Button btnRedefinir;
    private ProgressBar progressBar;
    private ConfigController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_editpassword);

        editEmail = findViewById(R.id.edit_email);
        btnRedefinir = findViewById(R.id.btn_redefinir_senha);
        progressBar = findViewById(R.id.progressBar);

        // Instanciar controller com objetos fictícios
        controller = new ConfigController(null, new Model_User(), null, new Model_Zona());

        String emailIntent = getIntent().getStringExtra("email");
        if (emailIntent != null) {
            editEmail.setText(emailIntent);
        }

        btnRedefinir.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Insere um email válido", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnRedefinir.setEnabled(false);

            controller.resetPassword(
                    this,
                    email,
                    () -> {
                        Toast.makeText(this, "Email de redefinição enviado!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        btnRedefinir.setEnabled(true);

                        // Redirecionar para login
                        Intent intent = new Intent(EditPassword.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    },
                    erro -> {
                        Toast.makeText(this, "Erro: " + erro, Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        btnRedefinir.setEnabled(true);
                    }
            );
        });

        Button btnCancelar = findViewById(R.id.btn_cancelar);
        btnCancelar.setOnClickListener(v -> finish());
    }
}
