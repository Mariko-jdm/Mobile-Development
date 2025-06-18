package com.example.dayflow;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class UserActivity extends AppCompatActivity {
    private EditText userName, userAbout;
    private ImageButton btnBack;
    private ShapeableImageView userAvatar;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "UserPrefs";
    private static final int REQUEST_CODE_READ_MEDIA = 100;

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Обработка системных отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация элементов
        userName = findViewById(R.id.userName);
        userAbout = findViewById(R.id.userAbout);
        btnBack = findViewById(R.id.btnBack);
        userAvatar = findViewById(R.id.userAvatar);

        // Инициализация SharedPreferences
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Загрузка сохранённых данных
        loadSavedData();

        // Настройка ActivityResultLauncher для галереи
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                userAvatar.setImageBitmap(bitmap);
                                saveAvatar(bitmap);
                                Toast.makeText(UserActivity.this, "Аватарка обновлена", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(UserActivity.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        // Сохранение данных при изменении
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                saveData();
            }
        });

        userAbout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                saveData();
            }
        });

        btnBack.setOnClickListener(v -> {
            saveData(); // Сохранение перед выходом
            Intent intent = new Intent(UserActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Обработчик клика по аватарке
        userAvatar.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 14+
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                            REQUEST_CODE_READ_MEDIA);
                } else {
                    openGallery();
                }
            } else { // Для Android < 14
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_READ_MEDIA);
                } else {
                    openGallery();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Разрешение на доступ к изображениям отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void loadSavedData() {
        String savedName = prefs.getString("userName", "");
        String savedAbout = prefs.getString("userAbout", "");
        String savedAvatar = prefs.getString("userAvatar", null);
        userName.setText(savedName);
        userAbout.setText(savedAbout);
        if (savedAvatar != null) {
            byte[] decodedString = Base64.getDecoder().decode(savedAvatar);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            userAvatar.setImageBitmap(decodedByte);
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userName", userName.getText().toString().trim());
        editor.putString("userAbout", userAbout.getText().toString().trim());
        editor.apply();
    }

    private void saveAvatar(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        String encoded = Base64.getEncoder().encodeToString(byteArray);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userAvatar", encoded);
        editor.apply();
    }
}