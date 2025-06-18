package com.example.dayflow.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.dayflow.models.Note;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DiaryDB";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_NAME = "notes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_TEXT = "text";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_IS_LIKED = "isLiked";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_IS_LIKED + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IS_LIKED + " INTEGER DEFAULT 0");
        }
    }

    public void addNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, note.getId());
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_TEXT, note.getText());
        values.put(COLUMN_DATE, note.getDate()); // Убедимся, что дата в формате yyyy-MM-dd
        values.put(COLUMN_IS_LIKED, note.isLiked() ? 1 : 0);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_TEXT, note.getText());
        values.put(COLUMN_DATE, note.getDate());
        values.put(COLUMN_IS_LIKED, note.isLiked() ? 1 : 0);
        db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public Note getNoteById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Note note = null;
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            note = new Note(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            );
            note.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LIKED)) == 1);
        }
        cursor.close();
        db.close();
        return note;
    }

    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_DATE + " DESC"; // Убывание
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                );
                note.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LIKED)) == 1);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    public List<Note> getFavoriteNotes() {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_IS_LIKED + "=1 ORDER BY " + COLUMN_DATE + " DESC"; // Убывание
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
                );
                note.setLiked(true);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return notes;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}