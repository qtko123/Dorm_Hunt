package com.example.dormhunt.interfaces;

import com.example.dormhunt.models.Dorm;

public interface OnDormClickListener {
    void onDormClick(Dorm dorm);
    void onEditClick(Dorm dorm);
    void onDeleteClick(Dorm dorm);
}