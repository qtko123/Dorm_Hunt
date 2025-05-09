package com.example.dormhunt.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dormhunt.R;
import com.example.dormhunt.models.Inquiry;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.InquiryViewHolder> {
    private List<Inquiry> inquiries = new ArrayList<>();
    private OnInquiryActionListener listener;

    public interface OnInquiryActionListener {
        void onAcceptClick(Inquiry inquiry);
        void onDeclineClick(Inquiry inquiry);
    }

    public InquiryAdapter(OnInquiryActionListener listener) {
        this.listener = listener;
    }

    @Override
    public InquiryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inquiry, parent, false);
        return new InquiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InquiryViewHolder holder, int position) {
        Inquiry inquiry = inquiries.get(position);
        holder.dormNameText.setText(inquiry.getDormName());
        holder.priceText.setText(String.format(Locale.getDefault(), "₱%.2f", inquiry.getPrice()));
        holder.userNameText.setText("From: " + inquiry.getUserName());
        holder.paymentMethodText.setText("Payment: " + inquiry.getPaymentMethod());
        holder.messageText.setText(inquiry.getMessage());
        holder.statusText.setText("Status: " + inquiry.getStatus());
        boolean isPending = "pending".equals(inquiry.getStatus().toLowerCase());
        holder.acceptButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.declineButton.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) listener.onAcceptClick(inquiry);
        });

        holder.declineButton.setOnClickListener(v -> {
            if (listener != null) listener.onDeclineClick(inquiry);
        });
    }

    @Override
    public int getItemCount() {
        return inquiries.size();
    }

    public void updateInquiries(List<Inquiry> newInquiries) {
        this.inquiries = newInquiries;
        notifyDataSetChanged();
    }

    static class InquiryViewHolder extends RecyclerView.ViewHolder {
        TextView dormNameText, priceText, userNameText, paymentMethodText, 
                messageText, statusText;
        Button acceptButton, declineButton;

        InquiryViewHolder(View itemView) {
            super(itemView);
            dormNameText = itemView.findViewById(R.id.dormNameText);
            priceText = itemView.findViewById(R.id.priceText);
            userNameText = itemView.findViewById(R.id.userNameText);
            paymentMethodText = itemView.findViewById(R.id.paymentMethodText);
            messageText = itemView.findViewById(R.id.messageText);
            statusText = itemView.findViewById(R.id.statusText);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}