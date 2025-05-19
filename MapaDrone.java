package com.example.safetydrones.Drones;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.fragment.app.FragmentActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.safetydrones.Model.Model_Zona;
import com.example.safetydrones.Controller.MapaDroneController;
import com.example.safetydrones.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MapaDrone extends FragmentActivity implements OnMapReadyCallback {

        private GoogleMap mMap;
        private LottieAnimationView loadingAnimation;
        private MapaDroneController controller;
        private Model_Zona zonaModel;

        private String filtroAtual = "TODAS"; // "TODAS", "PROHIBITED", "REQ_AUTHORISATION"
        private int totalZonas = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_mapadrone);

            loadingAnimation = findViewById(R.id.loading_animation);
            zonaModel = new Model_Zona();
            controller = new MapaDroneController(this, zonaModel);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }

            ImageButton btnVoltar = findViewById(R.id.btn_voltar);
            btnVoltar.setOnClickListener(v -> finish());

            Button btnFiltro = findViewById(R.id.btn_filtro);
            btnFiltro.setOnClickListener(v -> abrirFiltroZonas());
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            loadingAnimation.setVisibility(View.VISIBLE);

            LatLng portugal = new LatLng(39.5, -8.0);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(portugal, 6));

            // Carregar as zonas do JSON
            String json = carregarJSON();
            if (json != null) {
                controller.carregarZonasEAtualizarMapa(filtroAtual, json);
            } else {
                Log.e("MAPA", "JSON retornado nulo — verifica se o ficheiro está em /assets/");
            }

            mMap.setOnMapLoadedCallback(() -> loadingAnimation.setVisibility(View.GONE));

            mMap.setOnMarkerClickListener(marker -> {
                Model_Zona zona = (Model_Zona) marker.getTag();
                if (zona != null) {
                    exibirInformacoesZona(zona);

                    SharedPreferences prefs = getSharedPreferences("safetydrones_prefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    String zonaKey = "visitas_" + zona.getNome();
                    int visitas = prefs.getInt(zonaKey, 0);
                    visitas++;
                    UltimaVisitasZona(zona.getNome(), visitas);
                }
                return false;
            });
        }

        private void abrirFiltroZonas() {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottomsheet_filtro_zonas, null);

            sheetView.findViewById(R.id.btn_todas).setOnClickListener(v -> {
                filtroAtual = "TODAS";
                atualizarMapa(zonaModel.getZonasRestritas());
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_proibidas).setOnClickListener(v -> {
                filtroAtual = "PROHIBITED";
                atualizarMapa(zonaModel.getZonasRestritas());
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_autorizadas).setOnClickListener(v -> {
                filtroAtual = "REQ_AUTHORISATION";
                atualizarMapa(zonaModel.getZonasRestritas());
                bottomSheetDialog.dismiss();
            });

            sheetView.findViewById(R.id.btn_no_autorizadas).setOnClickListener(v -> {
                filtroAtual = "NO_RESTRICTION";
                atualizarMapa(zonaModel.getZonasRestritas());
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.show();
        }

        public void atualizarMapa(List<Model_Zona> zonasRestritas) {
            mMap.clear();
            totalZonas = 0;
            for (Model_Zona zona : zonasRestritas) {
                if (filtroAtual.equals("TODAS") || zona.getRestricao().equals(filtroAtual)) {
                    adicionarZonaNoMapa(zona);
                    totalZonas++;
                }
            }
            TotalZonas(totalZonas);
        }

        private void adicionarZonaNoMapa(Model_Zona zona) {
            if (zona.getTipoGeometria() == null) {
                Log.w("ZONA", "Zona sem tipo de geometria: " + zona.getNome());
                return;
            }

            float corMark = obterCorMarcador(zona.getRestricao());

            switch (zona.getTipoGeometria()) {
                case "Circle":
                    if (zona.getCentro() != null && zona.getRaio() > 0) {
                        adicionarCirculoZona(zona, corMark);
                    } else {
                        Log.w("ZONA", "Dados de círculo incompletos para: " + zona.getNome());
                    }
                    break;

                case "Polygon":
                    if (zona.getCoordenadasPoligono() != null && !zona.getCoordenadasPoligono().isEmpty()) {
                        adicionarPoligonoZona(zona, corMark);
                    } else {
                        Log.w("ZONA", "Coordenadas de polígono inválidas para: " + zona.getNome());
                    }
                    break;

                default:
                    Log.w("ZONA", "Tipo de geometria desconhecido: " + zona.getTipoGeometria());
                    break;
            }
        }

        private float obterCorMarcador(String restricao) {
            switch (restricao) {
                case "PROHIBITED":
                    return BitmapDescriptorFactory.HUE_RED;
                case "REQ_AUTHORISATION":
                    return BitmapDescriptorFactory.HUE_YELLOW;
                default:
                    return BitmapDescriptorFactory.HUE_ORANGE;
            }
        }

        private void adicionarCirculoZona(Model_Zona zona, float corMark) {
            mMap.addCircle(new CircleOptions()
                    .center(zona.getCentro())
                    .radius(zona.getRaio())
                    .fillColor(zona.getCorGoogleMaps())
                    .strokeColor(zona.getCorGoogleMaps())
                    .strokeWidth(5));

            Marker marcador = mMap.addMarker(new MarkerOptions()
                    .position(zona.getCentro())
                    .title(zona.getNome())
                    .icon(BitmapDescriptorFactory.defaultMarker(corMark)));
            if (marcador != null) marcador.setTag(zona);
        }

        private void adicionarPoligonoZona(Model_Zona zona, float corMark) {
            List<LatLng> pontos = zona.getCoordenadasPoligono();
            if (pontos == null || pontos.isEmpty()) return;

            mMap.addPolygon(new PolygonOptions()
                    .addAll(pontos)
                    .fillColor(zona.getCorGoogleMaps())
                    .strokeColor(zona.getCorGoogleMaps())
                    .strokeWidth(5));

            Marker marcador = mMap.addMarker(new MarkerOptions()
                    .position(pontos.get(0))
                    .title(zona.getNome())
                    .icon(BitmapDescriptorFactory.defaultMarker(corMark)));
            if (marcador != null) marcador.setTag(zona);
        }

        private void TotalZonas(int totalZonas) {
            SharedPreferences prefs = getSharedPreferences("safetydrones_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("total_zonas", totalZonas);
            editor.apply();
        }

        private void UltimaVisitasZona(String zonaNome, int visitas) {
            SharedPreferences prefs = getSharedPreferences("safetydrones_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            String zonaKey = "visitas_" + zonaNome;
            editor.putInt(zonaKey, visitas);
            editor.putString("ultima_zona", zonaNome);
            editor.apply();
        }

        private String carregarJSON() {
            try {
                InputStream is = getAssets().open("UASZoneVersion.json");
                int tamanho = is.available();
                byte[] buffer = new byte[tamanho];
                is.read(buffer);
                is.close();
                return new String(buffer, StandardCharsets.UTF_8);
            } catch (Exception e) {
                Log.e("MAPA", "Erro ao carregar JSON", e);
                return null;
            }
        }

        private void exibirInformacoesZona(Model_Zona zona) {
            try {
                StringBuilder info = new StringBuilder();
                info.append("Nome: ").append(zona.getNome()).append("\n");
                info.append("Restrição: ").append(zona.getRestricao()).append("\n\n");

                if (zona.getMotivos() != null && !zona.getMotivos().isEmpty()) {
                    info.append("Motivo(s): ");
                    for (String motivo : zona.getMotivos()) {
                        info.append(motivo).append(", ");
                    }
                    info.append("\n");
                }

                if (zona.getOutrasInformacoes() != null && !zona.getOutrasInformacoes().isEmpty()) {
                    info.append("Outras informações: ").append(zona.getOutrasInformacoes()).append("\n");
                }

                if (zona.getMensagem() != null && !zona.getMensagem().isEmpty()) {
                    info.append("Mensagem: ").append(zona.getMensagem()).append("\n");
                }

                if (zona.getAutoridades() != null && !zona.getAutoridades().isEmpty()) {
                    info.append("\nAutoridade(s): ");
                    for (Model_Zona.Autoridade autoridade : zona.getAutoridades()) {
                        info.append(autoridade.getNome()).append(", ");
                    }
                    info.append("\n");
                }

                new AlertDialog.Builder(this)
                        .setTitle("Informações da Zona")
                        .setMessage(info.toString())
                        .setPositiveButton("OK", null)
                        .show();
            } catch (Exception e) {
                Log.e("MAPA", "Erro ao exibir informações da zona", e);
            }
        }
}

