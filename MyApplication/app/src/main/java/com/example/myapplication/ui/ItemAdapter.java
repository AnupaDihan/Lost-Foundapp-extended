package com.example.myapplication.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private List<Item> filteredItems;
    private final OnItemClickListener onItemClick;
    private final OnItemDeleteListener onDeleteClick;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(Item item);
    }

    public ItemAdapter(List<Item> items, OnItemClickListener onItemClick, OnItemDeleteListener onDeleteClick) {
        this.items = items;
        this.filteredItems = new ArrayList<>(items);
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView itemImage;
        TextView titleText;
        TextView descriptionText;
        TextView categoryText;
        TextView typeText;
        TextView locationText;
        TextView timestampText;
        ImageView deleteButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            itemImage = itemView.findViewById(R.id.itemImage);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            categoryText = itemView.findViewById(R.id.categoryText);
            typeText = itemView.findViewById(R.id.typeText);
            locationText = itemView.findViewById(R.id.locationText);
            timestampText = itemView.findViewById(R.id.timestampText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = filteredItems.get(position);

        holder.titleText.setText(item.getTitle());
        holder.descriptionText.setText(item.getDescription());
        holder.categoryText.setText("📁 " + item.getCategory());
        holder.locationText.setText("📍 " + item.getLocation());

        String type = item.getType().toLowerCase();
        if (type.equals("lost")) {
            holder.typeText.setText("LOST");
            holder.typeText.setBackgroundResource(R.drawable.rounded_background_lost);
            holder.typeText.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else if (type.equals("found")) {
            holder.typeText.setText("FOUND");
            holder.typeText.setBackgroundResource(R.drawable.rounded_background_found);
            holder.typeText.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(item.getTimestamp()));
        holder.timestampText.setText("🕐 " + formattedDate);

        String imagePath = item.getImagePath();
        Object imageSource = null;
        if (imagePath != null && !imagePath.isEmpty()) {
            if (imagePath.startsWith("content://")) {
                imageSource = android.net.Uri.parse(imagePath);
            } else {
                imageSource = new java.io.File(imagePath);
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(imageSource)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(holder.itemImage);

        holder.cardView.setOnClickListener(v -> onItemClick.onItemClick(item));
        holder.deleteButton.setOnClickListener(v -> onDeleteClick.onItemDelete(item));
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public void filterItems(String query, String category) {
        filteredItems = new ArrayList<>();
        for (Item item : items) {
            boolean matchesQuery = query == null || query.isEmpty() ||
                    item.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                    item.getLocation().toLowerCase().contains(query.toLowerCase());

            boolean matchesCategory = (category == null || category.isEmpty() || category.equals("All Categories")) ||
                    item.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                filteredItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void updateItems(List<Item> newItems) {
        this.items = newItems;
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public Item getItemAtPosition(int position) {
        return filteredItems.get(position);
    }
}
