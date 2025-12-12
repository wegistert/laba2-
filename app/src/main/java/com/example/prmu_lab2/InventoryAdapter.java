package com.example.prmu_lab2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prmu_lab2.R;
import com.example.prmu_lab2.InventoryItem;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> inventoryItems = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String itemId, String itemName);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

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
        holder.currentItemId = item.getId();

        // Обработчик клика на весь элемент (для редактирования)
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        });

        // Обработчик долгого нажатия (альтернатива для редактирования)
        holder.itemView.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
                return true;
            }
            return false;
        });

        // Обработчик кнопки удаления
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(item.getId(), item.getItemName());
            }
        });
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

    public void addItem(InventoryItem item) {
        this.inventoryItems.add(item);
        notifyItemInserted(this.inventoryItems.size() - 1);
    }

    public void updateItem(InventoryItem updatedItem) {
        for (int i = 0; i < inventoryItems.size(); i++) {
            if (inventoryItems.get(i).getId().equals(updatedItem.getId())) {
                inventoryItems.set(i, updatedItem);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(String itemId) {
        for (int i = 0; i < inventoryItems.size(); i++) {
            if (inventoryItems.get(i).getId().equals(itemId)) {
                inventoryItems.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void clearItems() {
        this.inventoryItems.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewItemName, textViewCost, textViewDate;
        ImageButton buttonDelete;
        String currentItemId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItemName = itemView.findViewById(R.id.textViewItemName);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}