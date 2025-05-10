package com.example.dormhunt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.dormhunt.R;
import com.example.dormhunt.models.Dorm;
import android.view.View;
import com.example.dormhunt.OwnerDormDetailsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwnerDormAdapter extends RecyclerView.Adapter<OwnerDormAdapter.DormViewHolder> {
    private List<Dorm> dorms;
    private Context context;
    private OnDormClickListener listener;

    public interface OnDormClickListener {
        void onDormClick(Dorm dorm);
        void onEditClick(Dorm dorm);
        void onDeleteClick(Dorm dorm);
    }

    public OwnerDormAdapter(Context context, OnDormClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.dorms = new ArrayList<>();
    }

    public void updateDorms(List<Dorm> newDorms) {
        this.dorms = newDorms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
            R.layout.item_owner_dorm_card, parent, false);
        return new DormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DormViewHolder holder, int position) {
        Dorm dorm = dorms.get(position);
        
        holder.dormName.setText(dorm.getName());
        holder.dormPrice.setText(String.format(Locale.getDefault(), "₱%.2f", dorm.getPrice()));
        holder.viewCount.setText(String.valueOf(dorm.getViewCount()));
        holder.occupancyText.setText(String.format(Locale.getDefault(), 
            "%d/%d occupied", dorm.getCurrentOccupants(), dorm.getMaxOccupants()));
        holder.statusChip.setText(dorm.getAvailabilityStatus());
        holder.statusChip.setChipBackgroundColorResource(
 dorm.isAvailable() ? R.color.available_color : R.color.unavailable_color);
        holder.statusChip.setTextColor(context.getColor(android.R.color.white));
 if (dorm.getImageUrl() != null && !dorm.getImageUrl().isEmpty()) {
            holder.noImageText.setVisibility(View.GONE); // Hide the "no image" text
            Glide.with(context)
 .load(dorm.getImageUrl())
 .placeholder(R.drawable.default_dorm_image)
 .error(R.drawable.default_dorm_image)
 .listener(new RequestListener<android.graphics.drawable.Drawable>() {
 @Override
 public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
 Log.e("GlideError", "Image loading failed: " + e.getMessage(), e);
 return false; // Allow the error placeholder to be shown
 }

 @Override
 public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
 // Image loaded successfully
 return false;
 }
 })
 .into(holder.dormImage);
        }
        holder.itemView.setOnClickListener(v -> {
            // Changed to open OwnerDormDetailsActivity
            android.content.Intent intent = new android.content.Intent(context, OwnerDormDetailsActivity.class);
            intent.putExtra("dormId", dorm.getId());
            context.startActivity(intent);

            // Original onDormClick listener - keep if you still need a custom click handler in the activity
 if (listener != null) {
                listener.onDormClick(dorm);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(dorm);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(dorm);
            }
        });
        holder.analyticsContainer.setVisibility(View.VISIBLE);
        holder.viewCountContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return dorms.size();
    }

    static class DormViewHolder extends RecyclerView.ViewHolder {
        ImageView dormImage;
        TextView dormName, dormPrice, viewCount, occupancyText;
        Chip statusChip;
        View analyticsContainer, viewCountContainer;
        ImageView editButton, deleteButton;
        DormViewHolder(View itemView) {
            super(itemView);
            dormImage = itemView.findViewById(R.id.dormImage);
            dormName = itemView.findViewById(R.id.dormName);
            dormPrice = itemView.findViewById(R.id.dormPrice);
            viewCount = itemView.findViewById(R.id.viewCount);
            occupancyText = itemView.findViewById(R.id.occupancyText);
            statusChip = itemView.findViewById(R.id.statusChip);
            analyticsContainer = itemView.findViewById(R.id.analyticsContainer);
            viewCountContainer = itemView.findViewById(R.id.viewCountContainer);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            noImageText = itemView.findViewById(R.id.noImageText);
        }
    }
}