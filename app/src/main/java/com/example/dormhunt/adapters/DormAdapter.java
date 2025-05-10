package com.example.dormhunt.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormhunt.R;
import com.example.dormhunt.models.Dorm;
import com.bumptech.glide.Glide;
import com.example.dormhunt.utils.ImageUtils;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

public class DormAdapter extends RecyclerView.Adapter<DormAdapter.DormViewHolder> {
    private List<Dorm> dorms;
    private Context context;
    private OnDormClickListener listener;

    public interface OnDormClickListener {
        void onDormClick(Dorm dorm);
    }

    public DormAdapter(List<Dorm> dorms, Context context, OnDormClickListener listener) {
        this.dorms = dorms;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DormViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dorm_card, parent, false);
        return new DormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DormViewHolder holder, int position) {
        Dorm dorm = dorms.get(position);
        holder.dormName.setText(dorm.getName());
        holder.dormDescription.setText(dorm.getDescription());
        holder.dormPrice.setText(String.format(Locale.getDefault(), "₱%.2f", dorm.getPrice()));
        holder.availabilityChip.setText(dorm.isAvailable() ? "Available" : "Full");
        holder.availabilityChip.setChipBackgroundColorResource(
            dorm.isAvailable() ? R.color.available_color : R.color.unavailable_color
        );

        // Load image using Glide from Firebase Storage URL
        Glide.with(context)
             .load(dorm.getImageUrl())
             .placeholder(R.drawable.default_dorm_image) // Placeholder image
             .error(R.drawable.default_dorm_image) // Error image if loading fails
             .into(holder.dormImage);


 if (dorm.getImagePath() != null) {
 Bitmap bitmap = ImageUtils.loadImage(context, dorm.getImagePath());
 if (bitmap != null) {
 holder.dormImage.setImageBitmap(bitmap);
            } else {
 holder.dormImage.setImageResource(R.drawable.default_dorm_image);
            }
        } else if (dorm.getImageResourceName() != null) {
 int resourceId = context.getResources().getIdentifier(
 dorm.getImageResourceName(),
 "drawable",
 context.getPackageName()
 );
 holder.dormImage.setImageResource(resourceId > 0 ? resourceId : R.drawable.default_dorm_image);
        } else {
            holder.dormImage.setImageResource(R.drawable.default_dorm_image);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDormClick(dorm);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dorms.size();
    }

    public void updateDorms(List<Dorm> newDorms) {
        this.dorms = newDorms;
        notifyDataSetChanged();
    }

    static class DormViewHolder extends RecyclerView.ViewHolder {
        ImageView dormImage;
        TextView dormName, dormDescription, dormPrice;
        Chip availabilityChip;

        DormViewHolder(View itemView) {
            super(itemView);
            dormImage = itemView.findViewById(R.id.dormImage);
            dormName = itemView.findViewById(R.id.dormNameText);
            dormDescription = itemView.findViewById(R.id.dormDescriptionText);
            dormPrice = itemView.findViewById(R.id.dormPriceText);
            availabilityChip = itemView.findViewById(R.id.availabilityChip);
        }
    }
}