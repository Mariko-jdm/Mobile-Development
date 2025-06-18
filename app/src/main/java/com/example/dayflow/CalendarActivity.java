package com.example.dayflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayflow.database.DatabaseHelper;
import com.example.dayflow.models.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {
    private CalendarView calendarView;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> noteDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация
        calendarView = findViewById(R.id.calendarView);
        ImageButton btnBack = findViewById(R.id.btnBack);
        dbHelper = new DatabaseHelper(this);

        // Настройка ActivityResultLauncher (если нужно для перехода к записям)
        noteDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            highlightDates(); // Обновляем даты после редактирования
                        }
                    }
                }
        );

        // Подсветка дней с записями
        highlightDates();

        btnBack.setOnClickListener(v -> {
            String source = getIntent().getStringExtra("source");
            Intent intent = new Intent(CalendarActivity.this, source != null && source.equals("NotesActivity") ? NotesActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void highlightDates() {
        List<Note> notes = dbHelper.getAllNotes();
        Set<Long> datesWithNotes = new HashSet<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (Note note : notes) {
            try {
                Date date = dateFormat.parse(note.getDate());
                if (date != null) {
                    datesWithNotes.add(date.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Установка дат для подсветки (на Android 12+ можно использовать setDateTextAppearance)
        Calendar calendar = Calendar.getInstance();
        for (Long date : datesWithNotes) {
            calendar.setTimeInMillis(date);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // Простая подсветка через слушатель (ограничено на старых версиях)
            calendarView.setOnDateChangeListener((view, year1, month1, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year1, month1, dayOfMonth);
                if (datesWithNotes.contains(selected.getTimeInMillis())) {
                    // Можно добавить визуальную индикацию (например, цвет)
                    view.getDateTextAppearance(); // Для Android 12+ можно кастомизировать
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        highlightDates();
    }
}