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
import com.example.dormhunt.utils.ImageUtils;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentDormAdapter extends RecyclerView.Adapter<StudentDormAdapter.DormViewHolder> {
    private List<Dorm> dorms;
    private Context context;
    private OnDormClickListener listener;

    public interface OnDormClickListener {
        void onDormClick(Dorm dorm);
    }

    public StudentDormAdapter(Context context, OnDormClickListener listener) {
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
            R.layout.item_student_dorm_card, parent, false);
        return new DormViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DormViewHolder holder, int position) {
        Dorm dorm = dorms.get(position);
        holder.dormName.setText(dorm.getName());
        holder.dormPrice.setText(String.format(Locale.getDefault(), "₱%.2f", dorm.getPrice()));
        holder.statusChip.setText(dorm.getAvailabilityStatus());
        holder.statusChip.setChipBackgroundColorResource(
            dorm.isAvailable() ? R.color.available_color : R.color.unavailable_color);
        holder.statusChip.setTextColor(context.getColor(android.R.color.white));
        if (dorm.getImagePath() != null) {
            Bitmap bitmap = ImageUtils.loadImage(context, dorm.getImagePath());
            if (bitmap != null) {
                holder.dormImage.setImageBitmap(bitmap);
                holder.noImageText.setVisibility(View.GONE);
            } else {
                setupNoImage(holder);
            }
        } else {
            setupNoImage(holder);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDormClick(dorm);
            }
        });
    }

    private void setupNoImage(DormViewHolder holder) {
        holder.dormImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.dormImage.setBackgroundColor(context.getColor(R.color.grey));
        holder.dormImage.setImageResource(android.R.drawable.ic_menu_gallery);
        holder.noImageText.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return dorms.size();
    }

    static class DormViewHolder extends RecyclerView.ViewHolder {
        ImageView dormImage;
        TextView dormName, dormPrice, noImageText;
        Chip statusChip;

        DormViewHolder(View itemView) {
            super(itemView);
            dormImage = itemView.findViewById(R.id.dormImage);
            dormName = itemView.findViewById(R.id.dormName);
            dormPrice = itemView.findViewById(R.id.dormPrice);
            statusChip = itemView.findViewById(R.id.statusChip);
            noImageText = itemView.findViewById(R.id.noImageText);
        }
    }
}