package com.example.prmu_lab2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prmu_lab2.R;
import com.example.prmu_lab2.InventoryItem;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> inventoryItems = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);

        holder.textViewItemName.setText(item.getItemName());
        holder.textViewCost.setText(item.getFormattedCost());
        holder.textViewDate.setText(item.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    public void setInventoryItems(List<InventoryItem> items) {
        this.inventoryItems.clear();
        this.inventoryItems.addAll(items);
        notifyDataSetChanged();
    }

    public void clearItems() {
        this.inventoryItems.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName, textViewCost, textViewDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}