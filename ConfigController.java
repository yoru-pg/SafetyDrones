package com.example.safetydrones.Controller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.safetydrones.Main.LoginActivity;
import com.example.safetydrones.Model.Model_User;
import com.example.safetydrones.Model.Model_Zona;
import com.example.safetydrones.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

public class ConfigController {

    private final Fragment view;
    private final Model_User userModel;
    private final SharedPreferences prefs;
    private final Model_Zona zonaModel;

    public ConfigController(Fragment view,
                            Model_User userModel,
                            SharedPreferences prefs,
                            Model_Zona zonaModel) {
        this.view = view;
        this.userModel = userModel;
        this.prefs = prefs;
        this.zonaModel = zonaModel;
    }

    // Carrega e mostra dados do utilizador
    public void carregarDadosUtilizador(FirebaseUser currentUser,
                                        TextView userName,
                                        TextView userEmail,
                                        TextView userCreationDate,
                                        ImageView profileImage) {
        if (currentUser == null) return;

        // Email
        userEmail.setText(currentUser.getEmail());

        // Data de criação
        long creationTimestamp = currentUser.getMetadata().getCreationTimestamp();
        String formattedDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date(creationTimestamp));
        userCreationDate.setText("Criado em: " + formattedDate);

        // Referência ao utilizador na base de dados
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utilizadores").child(currentUser.getUid());

        // Imagem de perfil (Base64 ou imagem externa)
        userRef.child("fotoPerfil").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fotoPerfil = snapshot.getValue(String.class);

                if (fotoPerfil == null || fotoPerfil.isEmpty()) {
                    profileImage.setImageResource(R.drawable.icon_perfil);
                } else if (fotoPerfil.startsWith("http")) {
                    // É uma URL
                    Picasso.get().load(fotoPerfil).into(profileImage);
                } else {
                    // É uma string Base64
                    try {
                        byte[] imageBytes = Base64.decode(fotoPerfil, Base64.DEFAULT);
                        android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        profileImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        profileImage.setImageResource(R.drawable.icon_perfil);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profileImage.setImageResource(R.drawable.icon_perfil);
            }
        });

        // Nome (Google ou manual)
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
            userName.setText(currentUser.getDisplayName());
        } else {
            userRef.child("Nome").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nome = snapshot.getValue(String.class);
                    userName.setText(nome != null ? nome : "Utilizador");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    userName.setText("Utilizador");
                }
            });
        }
    }

    // Abre picker e envia callback para a View
    public void onProfileImageClicked(int requestCode, int pickRequestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        view.startActivityForResult(intent, pickRequestCode);
    }

    // Mostrar diálogo de edição de nome
    public void onEditNameClicked(FirebaseUser currentUser,
                                  String novoNome,
                                  Runnable onSuccess,
                                  Runnable onFailure) {
        atualizarNome(
                currentUser.getUid(), novoNome, onSuccess, onFailure
        );
    }

    public static void atualizarNome(String userId, String novoNome, Runnable onSuccess, Runnable onFailure) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Utilizadores").child(userId).child("Nome");

        userRef.setValue(novoNome).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccess.run();
            } else {
                onFailure.run();
            }
        });
    }

    // Logout
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(view.getActivity(), LoginActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        view.getActivity().startActivity(intent);
    }

    // Redefinir password
    public void resetPassword(Context context, String email,
                              Runnable onSuccess,
                              java.util.function.Consumer<String> onFailure) {
        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }

    // Carrega estatísticas de drones
    public void carregarStatsDrones(FirebaseUser currentUser,
                                    TextView totalDrones,
                                    TextView lastAdded,
                                    TextView lastEdited) {
        userModel.carregarStatsDrones(
                currentUser.getUid(), prefs,
                totalDrones, lastAdded, lastEdited
        );
    }

    // Carrega estatísticas de zonas
    public void carregarStatsZonas(TextView totalZonas,
                                   TextView mostVisited,
                                   TextView lastVisited) {
        int total = prefs.getInt("total_zonas", 0);
        totalZonas.setText("Total de zonas: " + total);

        String last = prefs.getString("ultima_zona", "N/A");
        lastVisited.setText("Última zona visitada: " + last);

        String best = "N/A";
        int max = 0;
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            if (e.getKey().startsWith("visitas_")) {
                int v = prefs.getInt(e.getKey(), 0);
                if (v > max) {
                    max = v;
                    best = e.getKey().substring(8);
                }
            }
        }
        mostVisited.setText("Zona mais visitada: " + best + " (" + max + ")");
    }

    public void updateProfileImage(Uri uri, FirebaseUser currentUser, Context context) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            byte[] imageBytes = getBytes(inputStream);
            String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            FirebaseDatabase.getInstance()
                    .getReference("Utilizadores")
                    .child(currentUser.getUid())
                    .child("fotoPerfil")
                    .setValue(imageBase64);

            Toast.makeText(context, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao atualizar imagem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public void mostrarDialogoConfirmacaoEliminacao(FirebaseUser user, String email) {
        if (user == null) return;

        boolean isGoogleLogin = user.getProviderData().stream()
                .anyMatch(profile -> "google.com".equals(profile.getProviderId()));

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("Confirmação");

        if (isGoogleLogin) {
            builder.setMessage("Tem a certeza que deseja eliminar a sua conta?\n\nEsta ação é irreversível.");
            builder.setPositiveButton("Eliminar", (dialog, which) -> {
                delete_account_google_ou_email(email, null);  // Não há password
            });
        } else {
            final android.widget.EditText inputPassword = new android.widget.EditText(view.getContext());
            inputPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            inputPassword.setHint("Insira a sua palavra-passe");

            builder.setView(inputPassword);
            builder.setMessage("Para segurança, insira a sua palavra-passe para eliminar a conta.");
            builder.setPositiveButton("Confirmar", (dialog, which) -> {
                String password = inputPassword.getText().toString().trim();
                if (password.isEmpty()) {
                    Toast.makeText(view.getContext(), "Insira a palavra-passe.", Toast.LENGTH_SHORT).show();
                    return;
                }
                delete_account_google_ou_email(email, password);
            });
        }
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void delete_account_google_ou_email(String email, String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if (password != null) {
            // --- Email e Password ---
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                if (reauthTask.isSuccessful()) {
                    eliminarDadosConta(user);
                } else {
                    Toast.makeText(view.getContext(), "Falha na reautenticação: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // --- Login via Google ---
            GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(view.getContext());
            if (googleAccount != null && googleAccount.getIdToken() != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);

                user.reauthenticate(credential).addOnCompleteListener(reauthTask -> {
                    if (reauthTask.isSuccessful()) {
                        eliminarDadosConta(user);
                    } else {
                        Toast.makeText(view.getContext(), "Falha na reautenticação Google: " + reauthTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(view.getContext(), "Sessão Google inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void eliminarDadosConta(FirebaseUser user) {
        String userId = user.getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("Utilizadores").child(userId).removeValue();
        database.child("Drones").child(userId).removeValue();

        // Limpar prefs
        prefs.edit().clear().apply();

        user.delete().addOnCompleteListener(deleteTask -> {
            if (deleteTask.isSuccessful()) {
                Toast.makeText(view.getContext(), "Conta eliminada com sucesso!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(view.getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                view.getActivity().startActivity(intent);
            } else {
                Toast.makeText(view.getContext(), "Erro ao eliminar conta: " + deleteTask.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
