package com.example.safetydrones.Maps;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.example.safetydrones.Controller.MapsController;
import com.example.safetydrones.Model.Model_Zona;
import com.example.safetydrones.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


public class Maps extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LottieAnimationView loadingAnimation;
    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // MVC
    private MapsController controller;
    private Model_Zona zonaModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        // view binding
        loadingAnimation = view.findViewById(R.id.loading_animation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // controller + model
        zonaModel = new Model_Zona();
        controller = new MapsController(zonaModel);


        // inicializar o mapa
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        }

        // Carrega o JSON ANTES de o mapa ficar pronto
        carregarZonasDoJSON();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadingAnimation.setVisibility(View.VISIBLE);

        // centraliza em Portugal
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.5, -8.0), 6));
        mMap.setOnMapLoadedCallback(() -> loadingAnimation.setVisibility(View.GONE));

        mMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Model_Zona) {
                Model_Zona zona = (Model_Zona) tag;
                mostrarDialogZona(zona);
            }
            return false;
        });

        // localização do utilizador
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void ativarLocalizacao() {
        try {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), loc -> {
                if (loc == null) return;

                LatLng atual = new LatLng(loc.getLatitude(), loc.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(atual)
                        .title("Está aqui!")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(atual, 14));

                // desenhar a zona mais próxima
                Model_Zona prox = controller.getZonaMaisProxima(atual);
                if (prox != null) desenharZonaMaisProxima(prox, atual);
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void desenharZonaMaisProxima(Model_Zona zona, LatLng atual) {
        // marca no mapa
        LatLng centro = "Circle".equals(zona.getTipoGeometria())
                ? zona.getCentro()
                : zona.getCoordenadasPoligono().get(0);

        double distKm = zona.getDistancia();

        mMap.addMarker(new MarkerOptions()
                        .position(centro)
                        .title(zona.getNome())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                .setTag(zona);

        mMap.addCircle(new CircleOptions()
                .center(centro)
                .radius(zona.getRaio())
                .strokeWidth(4)
                .strokeColor(0xAAFF0000)
                .fillColor(0x22FF0000));

        new AlertDialog.Builder(requireContext())
                .setTitle("Zona mais próxima")
                .setMessage(
                                "Nome: " + zona.getNome() + "\n" +
                                "Tipo: " + zona.getRestricao() + "\n" +
                                "Distância: " + distKm + " km"
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void mostrarDialogZona(Model_Zona zona) {
        StringBuilder sb = new StringBuilder();
        sb.append("Nome: ").append(zona.getNome()).append("\n")
                .append("Motivo: ").append(zona.getMotivos()).append("\n")
                .append("Restrição: ").append(zona.getRestricao()).append("\n")
                .append("Raio: ").append((int)zona.getRaio()).append(" m\n")
                .append("Distância: ").append(zona.getDistancia()).append(" Km\n");


        new AlertDialog.Builder(requireContext())
                .setTitle("Informações da Zona")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void carregarZonasDoJSON() {
        try (InputStream is = requireActivity().getAssets().open("UASZoneVersion.json")) {
            byte[] buf = new byte[is.available()];
            is.read(buf);
            String json = new String(buf, StandardCharsets.UTF_8);
            // carrega no controller
            controller.carregarZonasNoMapa(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perm, @NonNull int[] grant) {
        if (req == LOCATION_PERMISSION_REQUEST_CODE && grant.length > 0 && grant[0] == PackageManager.PERMISSION_GRANTED) {
            ativarLocalizacao();
        }
    }
}
