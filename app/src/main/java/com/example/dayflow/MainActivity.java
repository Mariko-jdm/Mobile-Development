package com.example.dayflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnFavorites = findViewById(R.id.btnFavorites);
        ImageButton btnNotes = findViewById(R.id.btnNotes);
        ImageButton btnAddNote = findViewById(R.id.btnAddNote);
        ImageButton btnCalendar = findViewById(R.id.btnCalendar);
        ImageButton btnUser = findViewById(R.id.btnUser);

        btnNotes.setOnClickListener(v ->{
            Intent intent = new Intent(MainActivity.this, NotesActivity.class);
            startActivity(intent);
        });

        btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
            startActivity(intent); // Запуск для создания новой записи
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        });

        btnFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        btnCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }
}