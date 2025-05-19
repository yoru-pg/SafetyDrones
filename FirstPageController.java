package com.example.safetydrones.Controller;
import com.example.safetydrones.Home.FirstPage;
import com.example.safetydrones.Model.Model_FirstPage;

public class FirstPageController {


        private FirstPage view;
        private Model_FirstPage model;

        public FirstPageController(FirstPage view, Model_FirstPage model) {
            this.view = view;
            this.model = model;
        }

        public void handleNavigation(int itemId) {
            model.setSelectedItemId(itemId);
            view.updateViewBasedOnSelection(itemId);
        }

}
