package com.example.safetydrones.Main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safetydrones.R;
import com.example.safetydrones.Model.Model_User;
import com.example.safetydrones.Home.FirstPage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.auth.AuthCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button enterButton, GoogleLogin;
    private TextView registeBtn;

    private Model_User userModel;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);

        userModel = new Model_User();
        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.email_login);
        loginPassword = findViewById(R.id.email_password);
        enterButton = findViewById(R.id.enter_button);
        registeBtn = findViewById(R.id.registebtn);
        GoogleLogin = findViewById(R.id.google_button);

        enterButton.setOnClickListener(v -> loginUser());

        registeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        GoogleLogin.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Insira um email válido!", Toast.LENGTH_SHORT).show();
            return;
        }

        userModel.loginWithEmailAndPassword(email, password, this, new Model_User.LoginCallback() {
            @Override
            public void onLoginSuccess(FirebaseUser user) {
                Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, FirstPage.class));
                finish();
            }

            @Override
            public void onLoginFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = GoogleSignIn.getClient(this, userModel.getGoogleSignInOptions()).getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult();

                    userModel.loginWithGoogle(account, this, new Model_User.LoginCallback() {
                        @Override
                        public void onLoginSuccess(FirebaseUser user) {
                            Toast.makeText(LoginActivity.this, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, FirstPage.class));
                            finish();
                        }

                        @Override
                        public void onLoginFailure(String error) {
                            Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
}