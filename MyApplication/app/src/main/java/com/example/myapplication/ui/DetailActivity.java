package com.example.myapplication.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.DatabaseHelper;
import com.example.myapplication.models.Item;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private long itemId = -1;
    private Item currentItem = null;

    private MaterialCardView statusCard;
    private TextView statusText;
    private ImageView itemImage;
    private TextView titleText;
    private TextView descriptionText;
    private TextView categoryText;
    private TextView typeTextDetail;
    private TextView locationText;
    private TextView dateText;
    private TextView contactText;
    private MaterialButton buttonContact;
    private MaterialButton buttonResolve;
    private MaterialCardView resolvedMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        databaseHelper = new DatabaseHelper(this);
        itemId = getIntent().getLongExtra("item_id", -1);

        if (itemId == -1L) {
            Toast.makeText(this, "Error: Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        loadItemDetails();
        setupClickListeners();
    }

    private void initializeViews() {
        statusCard = findViewById(R.id.statusCard);
        statusText = findViewById(R.id.statusText);
        itemImage = findViewById(R.id.itemImage);
        titleText = findViewById(R.id.titleText);
        descriptionText = findViewById(R.id.descriptionText);
        categoryText = findViewById(R.id.categoryText);
        typeTextDetail = findViewById(R.id.typeTextDetail);
        locationText = findViewById(R.id.locationText);
        dateText = findViewById(R.id.dateText);
        contactText = findViewById(R.id.contactText);
        buttonContact = findViewById(R.id.buttonContact);
        buttonResolve = findViewById(R.id.buttonResolve);
        resolvedMessage = findViewById(R.id.resolvedMessage);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadItemDetails() {
        currentItem = databaseHelper.getItemById(itemId);
        if (currentItem == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        displayItemDetails(currentItem);
    }

    private void displayItemDetails(Item item) {
        if (item.getType().equalsIgnoreCase("lost")) {
            statusText.setText("LOST ITEM - NEEDS ATTENTION");
            statusCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
        } else {
            statusText.setText("FOUND ITEM - CLAIM AVAILABLE");
            statusCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_dark, getTheme()));
        }

        String imagePath = item.getImagePath();
        Object imageSource = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("content://")) {
                imageSource = Uri.parse(imagePath);
            } else {
                imageSource = new java.io.File(imagePath);
            }
        }

        Glide.with(this)
                .load(imageSource)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(itemImage);

        titleText.setText(item.getTitle());
        descriptionText.setText(item.getDescription());
        categoryText.setText(item.getCategory());
        
        String type = item.getType();
        if (type != null && !type.isEmpty()) {
            typeTextDetail.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
        }

        locationText.setText(item.getLocation());
        contactText.setText(item.getContactInfo());

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        dateText.setText(dateFormat.format(new Date(item.getTimestamp())));

        if (item.isResolved()) {
            buttonResolve.setEnabled(false);
            buttonResolve.setText("RESOLVED");
            resolvedMessage.setVisibility(View.VISIBLE);
        } else {
            buttonResolve.setEnabled(true);
            buttonResolve.setText("MARK RESOLVED");
            resolvedMessage.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        buttonContact.setOnClickListener(v -> showContactOptions());
        buttonResolve.setOnClickListener(v -> showResolveConfirmationDialog());
        itemImage.setOnClickListener(v -> showFullScreenImage());
    }

    private void showContactOptions() {
        if (currentItem == null) return;
        String[] options = {"Send Email", "Make Phone Call", "Copy Contact Info"};
        new AlertDialog.Builder(this)
                .setTitle("Contact " + currentItem.getType() + " owner")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: sendEmail(currentItem.getContactInfo()); break;
                        case 1: makePhoneCall(currentItem.getContactInfo()); break;
                        case 2: copyToClipboard(currentItem.getContactInfo()); break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendEmail(String contactInfo) {
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = emailPattern.matcher(contactInfo);
        String email = matcher.find() ? matcher.group() : contactInfo;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Regarding your lost/found item");
        intent.putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI'm interested in the item you posted.\n\nBest regards,");
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void makePhoneCall(String contactInfo) {
        Pattern phonePattern = Pattern.compile("\\+?[0-9\\s\\-()]{10,}");
        Matcher matcher = phonePattern.matcher(contactInfo);
        String phoneNumber = matcher.find() ? matcher.group().trim() : contactInfo;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Contact Info", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Contact info copied", Toast.LENGTH_SHORT).show();
        }
    }

    private void showResolveConfirmationDialog() {
        if (currentItem == null) return;
        String message = currentItem.getType().equalsIgnoreCase("lost") ?
                "Has this lost item been returned?" : "Has this found item been claimed?";
        new AlertDialog.Builder(this)
                .setTitle("Mark as Resolved")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> resolveItem())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resolveItem() {
        if (currentItem == null) return;
        int rowsUpdated = databaseHelper.updateItemResolved(currentItem.getId(), true);
        if (rowsUpdated > 0) {
            Toast.makeText(this, "Item marked as resolved", Toast.LENGTH_SHORT).show();
            loadItemDetails();
        } else {
            Toast.makeText(this, "Error resolving item", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFullScreenImage() {
        if (currentItem == null) return;
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        String imagePath = currentItem.getImagePath();
        Object imageSource = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("content://")) {
                imageSource = Uri.parse(imagePath);
            } else {
                imageSource = new java.io.File(imagePath);
            }
        }
        Glide.with(this).load(imageSource).into(imageView);
        new AlertDialog.Builder(this).setView(imageView).setPositiveButton("Close", null).show();
    }
}
