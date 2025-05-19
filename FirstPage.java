package com.example.safetydrones.Home;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.safetydrones.Config.Config;
import com.example.safetydrones.Controller.FirstPageController;
import com.example.safetydrones.Model.Model_FirstPage;
import com.example.safetydrones.Drones.Drones;
import com.example.safetydrones.Maps.Maps;
import com.example.safetydrones.R;
import com.example.safetydrones.Tempo.Tempo;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FirstPage extends AppCompatActivity {

    private View firstPageContent;
    private View frameLayout;
    private FirstPageController controller;  // Agora o Controller
    private Model_FirstPage model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.firstpage);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        firstPageContent = findViewById(R.id.first_page_content);
        frameLayout = findViewById(R.id.frame_layout);


        model = new Model_FirstPage();
        controller = new FirstPageController(this, model);


        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.home);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            controller.handleNavigation(itemId);
            return true;
        });

    }

    public void updateViewBasedOnSelection(int itemId) {
        Fragment selectedFragment = null;

        if (itemId == R.id.tempo) {
            selectedFragment = new Tempo();
        } else if (itemId == R.id.maps) {
            selectedFragment = new Maps();
        } else if (itemId == R.id.config_user) {
            selectedFragment = new Config();
        } else if (itemId == R.id.drone) {
            selectedFragment = new Drones();
        } else if (itemId == R.id.home) {
            firstPageContent.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
            return;
        }

        if (selectedFragment != null) {
            firstPageContent.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);
            switchFragment(selectedFragment);
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}
