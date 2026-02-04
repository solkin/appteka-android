package com.tomclaw.appsend.colorpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tomclaw.appsend.R;

import java.util.List;

public class PaletteHistoryAdapter extends RecyclerView.Adapter<PaletteHistoryAdapter.ViewHolder> {

    private final List<Integer> history;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int position, int color);
    }

    public PaletteHistoryAdapter(List<Integer> history, OnItemClickListener listener) {
        this.history = history;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_palette, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color = history.get(position);
        holder.paletteView.setSeedColor(color);
        holder.paletteView.setChecked(position == selectedPosition, true); // Enable animation
        holder.itemView.setOnClickListener(v -> listener.onItemClick(position, color));
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        PaletteItemView paletteView;

        ViewHolder(View itemView) {
            super(itemView);
            paletteView = itemView.findViewById(R.id.palette_view);
        }
    }
}