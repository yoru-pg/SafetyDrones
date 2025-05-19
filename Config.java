package com.example.safetydrones.Config;
import static android.app.Activity.RESULT_OK;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.safetydrones.Controller.ConfigController;
import com.example.safetydrones.Model.Model_User;
import com.example.safetydrones.Model.Model_Zona;
import com.example.safetydrones.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Config extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private ConfigController controller;

    private ImageView profileImage, editar_nome;
    private TextView userName, userEmail, userCreationDate;
    private TextView totalDrones, lastAddedDrone, lastEditedDrone;
    private TextView totalZonas, zonaMaisVisitada, zonaUltimaVisitada;
    private TextView eliminar_conta, editar_password, logout, btn_editar_nome;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_config, container, false);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        prefs = requireContext()
                .getSharedPreferences("safetydrones_prefs", Context.MODE_PRIVATE);

        // instancia controller
        controller = new ConfigController(
                this,
                new Model_User(),
                prefs,
                new Model_Zona()
        );

        profileImage = view.findViewById(R.id.user_profile_image);
        editar_nome = view.findViewById(R.id.icon_edit_name);

        userName = view.findViewById(R.id.user_name);
        userEmail  = view.findViewById(R.id.user_email);
        userCreationDate  = view.findViewById(R.id.user_creation_date);

        totalDrones = view.findViewById(R.id.drones_total);
        lastAddedDrone = view.findViewById(R.id.drones_last_added);
        lastEditedDrone = view.findViewById(R.id.drones_last_edited);

        totalZonas = view.findViewById(R.id.zonas_total);
        zonaMaisVisitada = view.findViewById(R.id.zonas_mais_visitada);
        zonaUltimaVisitada = view.findViewById(R.id.zonas_ultima_visitada);

        eliminar_conta = view.findViewById(R.id.btn_eliminar_conta);
        editar_password = view.findViewById(R.id.btn_editar_password);
        btn_editar_nome = view.findViewById(R.id.btn_editar_nome);
        logout = view.findViewById(R.id.btnlogout);

        // Carregar dados
        controller.carregarDadosUtilizador(
                currentUser, userName, userEmail, userCreationDate, profileImage
        );
        controller.carregarStatsDrones(
                currentUser, totalDrones, lastAddedDrone, lastEditedDrone
        );
        controller.carregarStatsZonas(
                totalZonas, zonaMaisVisitada, zonaUltimaVisitada
        );

        // editar foto
        profileImage.setOnClickListener(v -> {
            controller.onProfileImageClicked(RESULT_OK, PICK_IMAGE_REQUEST);
        });

        // botao editar nome
        btn_editar_nome.setOnClickListener(v-> {
            if (currentUser.getProviderData().stream()
                    .anyMatch(u->"google.com".equals(u.getProviderId()))) {
                Toast.makeText(getContext(),
                        "Não pode alterar nome de utilizador Google",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            editar_nome.setVisibility(VISIBLE);
        });

        // editar nome (ao clicar no icon)
        editar_nome.setOnClickListener(v -> {
            EditarNome(currentUser);
        });

        // editar password
        editar_password.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditPassword.class)
                        .putExtra("email", currentUser.getEmail()));
        });

        // logout
        logout.setOnClickListener(v -> {
            controller.logout();
        });

        //eliminar conta
        eliminar_conta.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String email = user != null ? user.getEmail() : "";
            controller.mostrarDialogoConfirmacaoEliminacao(user, email);

        });

        return view;
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE_REQUEST && res == RESULT_OK && data!=null) {
            Uri uri = data.getData();
            Glide.with(this).load(uri).into(profileImage);
            controller.updateProfileImage(uri, mAuth.getCurrentUser(), requireContext());

        }
    }

    private void EditarNome(FirebaseUser user) {
        editar_nome.setVisibility(INVISIBLE);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Editar Nome");

        final EditText inputNome = new EditText(getContext());
        inputNome.setInputType(InputType.TYPE_CLASS_TEXT);
        inputNome.setHint("Novo nome");
        dialogBuilder.setView(inputNome);

        dialogBuilder.setPositiveButton("Guardar", (dialog, which) -> {
            String novoNome = inputNome.getText().toString().trim();
            if (TextUtils.isEmpty(novoNome) || novoNome.length() < 5) {
                Toast.makeText(getContext(),
                        "O nome deve ter pelo menos 5 caracteres!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            controller.onEditNameClicked(
                    user,
                    novoNome,
                    () -> userName.setText(novoNome),
                    () -> Toast.makeText(getContext(),
                            "Erro ao atualizar nome", Toast.LENGTH_SHORT).show()
            );
        });

        dialogBuilder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        dialogBuilder.show();
    }
}
