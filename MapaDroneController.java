package com.example.safetydrones.Controller;
import android.util.Log;
import com.example.safetydrones.Drones.MapaDrone;
import com.example.safetydrones.Model.Model_Zona;


public class MapaDroneController {

    private final MapaDrone view;
    private final Model_Zona zonaModel;

    public MapaDroneController(MapaDrone view, Model_Zona zonaModel) {
        this.view = view;
        this.zonaModel = zonaModel;
    }

    // Aplica um filtro às zonas já carregadas
    public void aplicarFiltro(String filtro) {
        zonaModel.aplicarFiltro(filtro);
        view.atualizarMapa(zonaModel.getZonasRestritas());
    }

    // Carrega zonas do JSON e aplica o filtro atual
    public void carregarZonasEAtualizarMapa(String filtroAtual, String json) {
        if (json == null || json.trim().isEmpty()) {
            Log.e("CONTROLLER", "JSON inválido ao tentar carregar e filtrar zonas.");
            return;
        }

        zonaModel.carregarZonas(json);
        aplicarFiltro(filtroAtual);
    }
}
