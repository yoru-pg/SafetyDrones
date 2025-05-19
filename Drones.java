package com.example.safetydrones.Drones;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safetydrones.Model.Model_Drone;
import com.example.safetydrones.Model.DroneRepository;
import com.example.safetydrones.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.List;

public class Drones extends Fragment {

    private LinearLayout noDronesLayout;
    private RecyclerView droneRecyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private com.airbnb.lottie.LottieAnimationView loadingAnimation;
    private ActivityResultLauncher<Intent> addDroneLauncher;
    private ActivityResultLauncher<Intent> infoDroneLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_drones, container, false);

        noDronesLayout = view.findViewById(R.id.no_drones_layout);
        Button addDroneButton = view.findViewById(R.id.add_drone_button);
        droneRecyclerView = view.findViewById(R.id.drone_recycler_view);
        loadingAnimation = view.findViewById(R.id.loading_animation);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Drones");

        droneRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        addDroneLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadDroneData();
                    }
                }
        );

        infoDroneLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadDroneData();
                    }
                }
        );

        loadDroneData();

        addDroneButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), AddDrone.class);
            addDroneLauncher.launch(intent);
        });

        return view;
    }

    public void loadDroneData() {
        loadingAnimation.setVisibility(View.VISIBLE);
        noDronesLayout.setVisibility(View.GONE);
        droneRecyclerView.setVisibility(View.GONE);

        DroneRepository droneRepository = new DroneRepository();

        droneRepository.getDrones(new DroneRepository.DataCallback() {
            @Override
            public void onSuccess(List<Model_Drone> droneList) {
                loadingAnimation.setVisibility(View.GONE);

                if (!droneList.isEmpty()) {
                    noDronesLayout.setVisibility(View.GONE);
                    droneRecyclerView.setVisibility(View.VISIBLE);
                    DroneAdapter adapter = new DroneAdapter(droneList, getContext(), new DroneAdapter.OnDroneClickListener() {
                        @Override
                        public void onInfoClicked(Model_Drone drone) {
                            // Passando os dados do drone para a tela de informações
                            Intent intent = new Intent(getContext(), InfoDrone.class);
                            intent.putExtra("nome", drone.getNome());
                            intent.putExtra("modelo", drone.getModelo());
                            intent.putExtra("peso", String.valueOf(drone.getPeso()));
                            intent.putExtra("autonomia", String.valueOf(drone.getAutonomia()));
                            intent.putExtra("velocidade", String.valueOf(drone.getVelocidade()));
                            intent.putExtra("categoria", drone.getCategoria());
                            intent.putExtra("id", drone.getId());
                            infoDroneLauncher.launch(intent);
                        }

                        @Override
                        public void onVerMapaClicked(Model_Drone drone) {
                            Intent intent = new Intent(getContext(), MapaDrone.class);
                            startActivity(intent);
                            Toast.makeText(getContext(), "Abrir mapa com drone: " + drone.getNome(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    droneRecyclerView.setAdapter(adapter);
                } else {
                    noDronesLayout.setVisibility(View.VISIBLE);
                    droneRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingAnimation.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

