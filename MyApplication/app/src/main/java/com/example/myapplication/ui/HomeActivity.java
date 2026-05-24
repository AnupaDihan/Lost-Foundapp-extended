package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.buttonCreateAdvert).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, PostActivity.class));
        });

        findViewById(R.id.buttonShowItems).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });

        findViewById(R.id.buttonShowOnMap).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MapActivity.class));
        });
    }
}
