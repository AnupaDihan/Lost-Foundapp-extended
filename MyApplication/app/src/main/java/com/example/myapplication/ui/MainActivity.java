package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.DatabaseHelper;
import com.example.myapplication.models.Item;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private ItemAdapter adapter;
    private RecyclerView recyclerView;
    private Spinner categorySpinner;
    private SearchView searchView;

    private List<Item> items = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentCategory = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        setupViews();
        setupRecyclerView();
        setupCategoryFilter();
        loadItems();

        findViewById(R.id.fabCreate).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recyclerView);
        categorySpinner = findViewById(R.id.categorySpinner);
        searchView = findViewById(R.id.searchView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = (newText != null) ? newText : "";
                applyFilters();
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ItemAdapter(
                items,
                item -> {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("item_id", item.getId());
                    startActivity(intent);
                },
                item -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Item")
                            .setMessage("Are you sure you want to delete this item?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                databaseHelper.deleteItem(item.getId());
                                loadItems();
                                Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupCategoryFilter() {
        String[] categories = {"All Categories", "Electronics", "Pets", "Wallets", "Documents", "Other"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = (position == 0) ? null : categories[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentCategory = null;
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        adapter.filterItems(currentSearchQuery, currentCategory);
    }

    private void loadItems() {
        items.clear();
        items.addAll(databaseHelper.getAllItems());
        adapter.updateItems(new ArrayList<>(items));
        applyFilters();
    }
}
