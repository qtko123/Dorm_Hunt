package com.example.dormhunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.content.Intent;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private MaterialButton btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        viewPager = findViewById(R.id.carouselViewPager);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        // Setup carousel images
        List<Integer> slideImages = new ArrayList<>();
        slideImages.add(R.drawable.slideshow_1);
        slideImages.add(R.drawable.slideshow_2);
        slideImages.add(R.drawable.slideshow_3);
        slideImages.add(R.drawable.slideshow_4);

        // Set adapter for ViewPager2
        viewPager.setAdapter(new SlideAdapter(slideImages));

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AuthSelectionActivity.class);
                startActivity(intent);
            }
        });
    }

    private class SlideAdapter extends RecyclerView.Adapter<SlideViewHolder> {
        private List<Integer> slides;

        SlideAdapter(List<Integer> slides) {
            this.slides = slides;
        }

        @Override
        public SlideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new SlideViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(SlideViewHolder holder, int position) {
            holder.imageView.setImageResource(slides.get(position));
        }

        @Override
        public int getItemCount() {
            return slides.size();
        }
    }

    private static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SlideViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}