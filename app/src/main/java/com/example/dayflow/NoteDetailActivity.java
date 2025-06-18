package com.example.dayflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayflow.database.DatabaseHelper;
import com.example.dayflow.models.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    private EditText etTitle, etMainText;
    private ImageButton btnBack;
    private Button btnSave;
    private TextView tvDate;
    private DatabaseHelper dbHelper;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация элементов
        etTitle = findViewById(R.id.etTitle);
        etMainText = findViewById(R.id.etMainText);
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        tvDate = findViewById(R.id.tvDate);
        dbHelper = new DatabaseHelper(this);

        // Получение записи для редактирования (если передана)
        long noteId = getIntent().getLongExtra("note_id", -1);
        if (noteId != -1) {
            currentNote = dbHelper.getNoteById(noteId);
            if (currentNote != null) {
                etTitle.setText(currentNote.getTitle());
                etMainText.setText(currentNote.getText() != null ? currentNote.getText() : "");
                tvDate.setText(currentNote.getDate());
            }
        } else {
            tvDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        }

        // Обработчики
        btnBack.setOnClickListener(v -> {
            finish(); // Возвращает на предыдущую активность в стеке
        });

        tvDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String content = etMainText.getText().toString().trim();
            String date = tvDate.getText().toString().trim();

            if (!title.isEmpty() && !content.isEmpty() && !date.isEmpty()) {
                long id = (currentNote != null) ? currentNote.getId() : System.currentTimeMillis();
                Note note = new Note(title, content, date, id);
                if (currentNote == null) {
                    dbHelper.addNote(note);
                } else {
                    dbHelper.updateNote(note);
                }
                Toast.makeText(this, "Запись сохранена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(this,
                (view, yearSelected, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%d-%02d-%02d", yearSelected, monthOfYear + 1, dayOfMonth);
                    tvDate.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }
}