package com.example.dayflow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayflow.adapters.NotesAdapter;
import com.example.dayflow.database.DatabaseHelper;
import com.example.dayflow.models.Note;

import java.util.List;
import java.util.stream.Collectors;

public class NotesActivity extends AppCompatActivity {
    private RecyclerView rvNotes;
    private NotesAdapter adapter;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> noteDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация
        rvNotes = findViewById(R.id.rvNotes);
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        dbHelper = new DatabaseHelper(this);

        // Добавление разделителей
        DividerItemDecoration divider = new DividerItemDecoration(
                rvNotes.getContext(),
                DividerItemDecoration.VERTICAL
        );
        rvNotes.addItemDecoration(divider);

        // Настройка ActivityResultLauncher
        noteDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            loadNotes(); // Обновление списка после сохранения
                        }
                    }
                }
        );

        // Загрузка записей
        loadNotes();

        btnBack.setOnClickListener(v -> {
            String source = getIntent().getStringExtra("source");
            Intent intent = new Intent(NotesActivity.this, source != null && source.equals("FavoritesActivity") ? FavoritesActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            showSearchDialog();
        });
    }

    private void loadNotes() {
        List<Note> notes = dbHelper.getAllNotes();
        adapter = new NotesAdapter(notes, this);
        rvNotes.setAdapter(adapter);

        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(NotesActivity.this, NoteDetailActivity.class);
            intent.putExtra("note_id", note.getId());
            noteDetailLauncher.launch(intent);
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поиск записей");

        final EditText input = new EditText(this);
        input.setHint("Введите заголовок...");
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("Поиск", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = input.getText().toString().trim();
                if (!query.isEmpty()) {
                    List<Note> filteredNotes = dbHelper.getAllNotes().stream()
                            .filter(note -> note.getTitle().toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList());
                    adapter = new NotesAdapter(filteredNotes, NotesActivity.this);
                    rvNotes.setAdapter(adapter);
                    adapter.setOnItemClickListener(note -> {
                        Intent intent = new Intent(NotesActivity.this, NoteDetailActivity.class);
                        intent.putExtra("note_id", note.getId());
                        noteDetailLauncher.launch(intent);
                    });
                } else {
                    loadNotes(); // Показать все записи при пустом запросе
                }
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
}