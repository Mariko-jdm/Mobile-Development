package com.example.dayflow.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayflow.R;
import com.example.dayflow.database.DatabaseHelper;
import com.example.dayflow.models.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private OnItemClickListener listener;
    private Context context;
    private DatabaseHelper dbHelper;

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public NotesAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvNoteText.setText(note.getTitle());
        holder.btnHeart.setSelected(note.isLiked());
        holder.btnHeart.setImageResource(
                note.isLiked() ? R.drawable.heart_filled : R.drawable.heart
        );

        holder.btnHeart.setOnClickListener(v -> {
            note.setLiked(!note.isLiked());
            holder.btnHeart.setSelected(note.isLiked());
            holder.btnHeart.setImageResource(
                    note.isLiked() ? R.drawable.heart_filled : R.drawable.heart
            );
            dbHelper.updateNote(note); // Сохраняем новое состояние в базе
            notifyItemChanged(position);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(note);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(note, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    private void showDeleteDialog(Note note, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Удалить запись")
                .setMessage("Вы уверены, что хотите удалить запись \"" + note.getTitle() + "\"?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteNote(note.getId());
                        notes.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteText;
        ImageButton btnHeart;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteText = itemView.findViewById(R.id.tvNoteText);
            btnHeart = itemView.findViewById(R.id.btnHeart);
        }
    }
}