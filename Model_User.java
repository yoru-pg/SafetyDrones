package com.example.safetydrones.Model;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class Model_User {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public Model_User() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Utilizadores");
    }

    public boolean validarDados(String name, String email, String password, String confirmPassword, Context context) {
        if (TextUtils.isEmpty(name) && !name.isEmpty()) {
            Toast.makeText(context, "O nome não pode estar vazio!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(email) || !email.contains("@") || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            Toast.makeText(context, "Insira um email válido!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(context, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isEmpty(confirmPassword) && !password.equals(confirmPassword)) {
            Toast.makeText(context, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void registerUser(String name, String email, String password, final RegisterCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Recupera o id do utilizador criado
                        String userId = mAuth.getCurrentUser().getUid();

                        // Cria um mapa de dados para guardar no Firebase
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("Nome", name);
                        userData.put("Email", email);
                        userData.put("DataRegisto", System.currentTimeMillis());
                        userData.put("fotoPerfil", "default");

                        // Armazena os dados do usuário no Firebase Realtime Database
                        mDatabase.child(userId).setValue(userData)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Envia o link de verificação para o email
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            callback.onRegisterSuccess();
                                                        } else {
                                                            callback.onRegisterFailure("Erro ao enviar e-mail de verificação!");
                                                        }
                                                    });
                                        }
                                    } else {
                                        callback.onRegisterFailure("Erro ao guardar dados: " + task1.getException().getMessage());
                                    }
                                });
                    } else {
                        callback.onRegisterFailure("Erro ao registrar o usuário: " + task.getException().getMessage());
                    }
                });
    }

    // Autenticação com email e senha
    public void loginWithEmailAndPassword(String email, String password, Context context, LoginCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    callback.onLoginSuccess(user);
                } else {
                    callback.onLoginFailure("Verifique seu e-mail antes de entrar!");
                }
            } else {
                callback.onLoginFailure("Falha no login. Verifique suas credenciais!");
            }
        });
    }

    // Autenticação com Google
    public void loginWithGoogle(GoogleSignInAccount account, Context context, LoginCallback callback) {
        if (account != null) {
            String idToken = account.getIdToken();
            if (idToken != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        callback.onLoginSuccess(user);
                    } else {
                        callback.onLoginFailure("Erro ao autenticar com Google.");
                    }
                });
            }
        }
    }

    public GoogleSignInOptions getGoogleSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("280045476415-aa54s9tf7hmjtu2ca099j0p987961ck3.apps.googleusercontent.com")
                .requestEmail()
                .build();
    }

    public void carregarStatsDrones(
            String uid,
            SharedPreferences prefs,
            TextView totalDrones,
            TextView lastAdded,
            TextView lastEdited
    ) {
        DatabaseReference dronesRef =
                FirebaseDatabase.getInstance()
                        .getReference("Drones")
                        .child(uid);

        dronesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = snapshot.getChildrenCount();
                totalDrones.setText("Total: " + total);

                String lastKey = null;
                DataSnapshot lastSnap = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    lastKey = ds.getKey();
                    lastSnap = ds;
                }

                if (lastSnap != null) {
                    String nome = lastSnap.child("Nome").getValue(String.class);
                    String modelo = lastSnap.child("Modelo").getValue(String.class);
                    lastAdded.setText("Último adicionado: " + nome + " - " + modelo);
                } else {
                    lastAdded.setText("Último adicionado: N/A");
                }

                String editedId = prefs.getString("ultimoDroneEditadoId", null);
                if (editedId != null && snapshot.hasChild(editedId)) {
                    DataSnapshot ed = snapshot.child(editedId);
                    String nomeE = ed.child("Nome").getValue(String.class);
                    String modE = ed.child("Modelo").getValue(String.class);
                    lastEdited.setText("Último editado: " + nomeE + " - " + modE);
                } else {
                    lastEdited.setText("Último editado: N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                totalDrones.setText("Erro ao carregar");
                lastAdded.setText("Erro ao carregar");
                lastEdited.setText("Erro ao carregar");
            }
        });
    }


    // Callback para login
    public interface LoginCallback {
        void onLoginSuccess(FirebaseUser user);
        void onLoginFailure(String error);
    }
    public interface RegisterCallback {
        void onRegisterSuccess();
        void onRegisterFailure(String error);
    }

}
