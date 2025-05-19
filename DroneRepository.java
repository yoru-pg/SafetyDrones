package com.example.safetydrones.Model;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class DroneRepository {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public DroneRepository() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Drones");
    }

    public void getDrones(DataCallback callback) {
        if (mAuth.getCurrentUser() == null) {
            callback.onError("Utilizador não autenticado.");
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Model_Drone> droneList = new ArrayList<>();
                for (DataSnapshot droneSnapshot : dataSnapshot.getChildren()) {
                    String nome = droneSnapshot.child("Nome").getValue(String.class);
                    String modelo = droneSnapshot.child("Modelo").getValue(String.class);
                    Double peso = droneSnapshot.child("Peso").getValue(Double.class);
                    Integer autonomia = droneSnapshot.child("Autonomia").getValue(Integer.class);
                    Double velocidade = droneSnapshot.child("Velocidade").getValue(Double.class);

                    if (peso == null) peso = 0.0;
                    if (velocidade == null) velocidade = 0.0;

                    Model_Drone drone = new Model_Drone(nome, modelo, peso, autonomia, velocidade);
                    drone.setId(droneSnapshot.getKey());

                    if (droneSnapshot.hasChild("Categoria")) {
                        drone.setCategoria(droneSnapshot.child("Categoria").getValue(String.class));
                    }

                    droneList.add(drone);
                }
                callback.onSuccess(droneList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError("Erro ao carregar dados do drone: " + databaseError.getMessage());
            }
        });
    }

    public interface DataCallback {
        void onSuccess(List<Model_Drone> droneList);
        void onError(String errorMessage);
    }
}
