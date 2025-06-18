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

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private NotesAdapter adapter;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> noteDetailLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация
        rvFavorites = findViewById(R.id.rvNotes);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        dbHelper = new DatabaseHelper(this);

        // Добавление разделителей
        DividerItemDecoration divider = new DividerItemDecoration(
                rvFavorites.getContext(),
                DividerItemDecoration.VERTICAL
        );
        rvFavorites.addItemDecoration(divider);

        // Настройка ActivityResultLauncher
        noteDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            loadFavorites(); // Обновление списка после редактирования
                        }
                    }
                }
        );

        // Загрузка избранных записей
        loadFavorites();

        btnBack.setOnClickListener(v -> {
            String source = getIntent().getStringExtra("source");
            Intent intent = new Intent(FavoritesActivity.this, source != null && source.equals("NotesActivity") ? NotesActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnSearch.setOnClickListener(v -> {
            showSearchDialog();
        });
    }

    private void loadFavorites() {
        List<Note> favorites = dbHelper.getFavoriteNotes();
        adapter = new NotesAdapter(favorites, this);
        rvFavorites.setAdapter(adapter);

        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(FavoritesActivity.this, NoteDetailActivity.class);
            intent.putExtra("note_id", note.getId());
            noteDetailLauncher.launch(intent);
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поиск избранных");

        final EditText input = new EditText(this);
        input.setHint("Введите заголовок...");
        input.setSingleLine();
        builder.setView(input);

        builder.setPositiveButton("Поиск", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String query = input.getText().toString().trim();
                if (!query.isEmpty()) {
                    List<Note> filteredFavorites = dbHelper.getFavoriteNotes().stream()
                            .filter(note -> note.getTitle().toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList());
                    adapter = new NotesAdapter(filteredFavorites, FavoritesActivity.this);
                    rvFavorites.setAdapter(adapter);
                    adapter.setOnItemClickListener(note -> {
                        Intent intent = new Intent(FavoritesActivity.this, NoteDetailActivity.class);
                        intent.putExtra("note_id", note.getId());
                        noteDetailLauncher.launch(intent);
                    });
                } else {
                    loadFavorites(); // Показать все избранные при пустом запросе
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
        loadFavorites();
    }
}