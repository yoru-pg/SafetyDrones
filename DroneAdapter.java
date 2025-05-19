package com.example.safetydrones.Drones;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safetydrones.Model.Model_Drone;
import com.example.safetydrones.R;

import java.util.ArrayList;
import java.util.List;

public class DroneAdapter extends RecyclerView.Adapter<DroneAdapter.DroneViewHolder> {

    private List<Model_Drone> droneList;
    private Context context;
    private OnDroneClickListener listener;

    public interface OnDroneClickListener {
        void onInfoClicked(Model_Drone drone);
        void onVerMapaClicked(Model_Drone drone);
    }

    public DroneAdapter(List<Model_Drone> droneList, Context context, OnDroneClickListener listener) {
        this.droneList = (droneList != null) ? droneList : new ArrayList<>();
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DroneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drone_item, parent, false);
        return new DroneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DroneViewHolder holder, int position) {
        Model_Drone drone = droneList.get(position);
        holder.droneNome.setText("Nome: " + drone.getNome());
        holder.droneModelo.setText("Modelo: " + drone.getModelo());
        holder.dronePeso.setText("Peso: " + drone.getPeso() + " Kg");
        holder.droneAutonomia.setText("Autonomia: " + drone.getAutonomia() + " minutos");
        holder.droneVelocidade.setText("Velocidade: " + drone.getVelocidade() + " m/s");
        holder.droneCategoria.setText("Categoria: " + drone.getCategoria());

        // Chama o listener para abrir mapa
        holder.btnVerMapa.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerMapaClicked(drone);
            }
        });

        // Chama o listener para abrir as informações do drone
        holder.btnInfo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onInfoClicked(drone);
            }
        });
    }

    @Override
    public int getItemCount() {
        return droneList.size();
    }

    public static class DroneViewHolder extends RecyclerView.ViewHolder {
        TextView droneNome, droneModelo, dronePeso, droneAutonomia, droneVelocidade, droneCategoria;
        Button btnVerMapa, btnInfo;

        public DroneViewHolder(@NonNull View itemView) {
            super(itemView);
            droneNome = itemView.findViewById(R.id.drone_nome);
            droneModelo = itemView.findViewById(R.id.drone_modelo);
            dronePeso = itemView.findViewById(R.id.drone_peso);
            droneAutonomia = itemView.findViewById(R.id.drone_autonomia);
            droneVelocidade = itemView.findViewById(R.id.drone_velocidade);
            droneCategoria = itemView.findViewById(R.id.drone_categoria);
            btnVerMapa = itemView.findViewById(R.id.btn_ver_mapa);
            btnInfo = itemView.findViewById(R.id.btn_info);
        }
    }
}
