package com.example.safetydrones.Main;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetydrones.R;
import com.example.safetydrones.Model.Model_User;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerName, registerEmail, registerPassword, registerPassword2;
    private Button registerUserButton;
    private Model_User userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);

        userModel = new Model_User();

        registerName = findViewById(R.id.register_name);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerPassword2 = findViewById(R.id.register_password2);
        registerUserButton = findViewById(R.id.btn_register);

        registerUserButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = registerName.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString().trim();
        String confirm_password = registerPassword2.getText().toString().trim();


        if (userModel.validarDados(name, email, password, confirm_password, this)) {

            userModel.registerUser(name, email, password, new Model_User.RegisterCallback() {
                @Override
                public void onRegisterSuccess() {
                    Toast.makeText(RegisterActivity.this, "Conta criada com sucesso! Verifique seu e-mail para confirmar.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onRegisterFailure(String error) {
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
