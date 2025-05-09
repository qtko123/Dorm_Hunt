package com.example.dormhunt.adapters;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dormhunt.R;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {
    private final int[] imageResources;
    private final Context context;

    public CarouselAdapter(Context context, String baseName) {
        this.context = context;
        this.imageResources = new int[7];
        String[] suffixes = {"", "a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < suffixes.length; i++) {
            String imageName = baseName + suffixes[i];
            imageResources[i] = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
            if (imageResources[i] == 0) {
                imageResources[i] = R.drawable.placeholder_dorm;
            }
        }
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new CarouselViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        Glide.with(context)
            .load(imageResources[position])
            .placeholder(R.drawable.placeholder_dorm)
            .error(R.drawable.placeholder_dorm)
            .centerCrop()
            .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageResources.length;
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        CarouselViewHolder(@NonNull ImageView itemView) {
            super(itemView);
            this.imageView = itemView;
        }
    }
    public void updateImages(String newBaseName) {
        String[] suffixes = {"", "a", "b", "c", "d", "e", "f"};
        for (int i = 0; i < suffixes.length; i++) {
            String imageName = newBaseName + suffixes[i];
            int resourceId = context.getResources().getIdentifier(
                imageName, "drawable", context.getPackageName());
            imageResources[i] = resourceId != 0 ? resourceId : R.drawable.placeholder_dorm;
        }
        notifyDataSetChanged();
    }
}