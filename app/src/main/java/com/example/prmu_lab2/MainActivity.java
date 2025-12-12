package com.example.prmu_lab2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prmu_lab2.InventoryAdapter;
import com.example.prmu_lab2.InventoryItem;
import com.example.prmu_lab2.network.SupabaseConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements InventoryAdapter.OnItemClickListener,
        InventoryAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private FloatingActionButton fabAdd;
    private InventoryAdapter adapter;

    private String accessToken;
    private String userId;

    private ExecutorService networkExecutor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация ExecutorService и Handler
        networkExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Загрузка данных сессии ОДИН РАЗ
        loadSessionData();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadInventoryItems();
    }

    private void loadSessionData() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);
        userId = prefs.getString("user_id", null);

        if (accessToken == null || userId == null) {
            // Если нет данных сессии, переходим на экран входа
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        adapter = new InventoryAdapter();
        adapter.setOnItemClickListener(this);
        adapter.setOnDeleteClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddDialog());
    }

    // === МЕТОД ДЛЯ ДОБАВЛЕНИЯ ПРЕДМЕТА ===
    private void showAddDialog() {
        // Создаем поля ввода
        final EditText etItemName = new EditText(this);
        etItemName.setHint("Название предмета");

        final EditText etCost = new EditText(this);
        etCost.setHint("Ориентировочная стоимость");
        etCost.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final EditText etDate = new EditText(this);
        etDate.setHint("Дата покупки (ГГГГ-ММ-ДД)");
        etDate.setText("2023-12-01");  // Пример для удобства

        // Создаем контейнер для полей
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        container.addView(etItemName);
        container.addView(etCost);
        container.addView(etDate);

        // Создаем диалог
        new AlertDialog.Builder(this)
                .setTitle("Добавить предмет в инвентарь")
                .setView(container)
                .setPositiveButton("Добавить", (dialog, which) -> {
                    String itemName = etItemName.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (itemName.isEmpty() || costStr.isEmpty() || date.isEmpty()) {
                        Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double cost = Double.parseDouble(costStr);
                        addInventoryItem(itemName, cost, date);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите корректную стоимость", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void addInventoryItem(String itemName, double cost, String date) {
        showLoading(true);

        networkExecutor.execute(() -> {
            try {
                URL url = new URL(SupabaseConfig.TABLE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("item_name", itemName);
                jsonBody.put("estimated_cost", cost);
                jsonBody.put("purchase_date", date);
                jsonBody.put("user_id", userId);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonBody.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();

                mainHandler.post(() -> {
                    showLoading(false);

                    if (responseCode == 201) {
                        // Просто перезагружаем список вместо парсинга ответа
                        loadInventoryItems();
                        Toast.makeText(this, "Предмет добавлен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // === МЕТОД ДЛЯ РЕДАКТИРОВАНИЯ ПРЕДМЕТА ===
    @Override
    public void onItemClick(InventoryItem item) {
        showEditDialog(item);
    }

    private void showEditDialog(InventoryItem item) {
        // Создаем поля ввода с предзаполненными значениями
        final EditText etItemName = new EditText(this);
        etItemName.setHint("Название предмета");
        etItemName.setText(item.getItemName());

        final EditText etCost = new EditText(this);
        etCost.setHint("Ориентировочная стоимость");
        etCost.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etCost.setText(String.valueOf(item.getEstimatedCost()));

        final EditText etDate = new EditText(this);
        etDate.setHint("Дата покупки (ГГГГ-ММ-ДД)");
        etDate.setText(item.getPurchaseDate());

        // Создаем контейнер для полей
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        container.addView(etItemName);
        container.addView(etCost);
        container.addView(etDate);

        // Создаем диалог
        new AlertDialog.Builder(this)
                .setTitle("Редактировать предмет")
                .setView(container)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String itemName = etItemName.getText().toString().trim();
                    String costStr = etCost.getText().toString().trim();
                    String date = etDate.getText().toString().trim();

                    if (itemName.isEmpty() || costStr.isEmpty() || date.isEmpty()) {
                        Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double cost = Double.parseDouble(costStr);
                        updateInventoryItem(item.getId(), itemName, cost, date);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Введите корректную стоимость", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void updateInventoryItem(String itemId, String itemName, double cost, String date) {
        showLoading(true);

        networkExecutor.execute(() -> {
            try {
                URL url = new URL(SupabaseConfig.TABLE_URL + "?id=eq." + itemId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Используем PATCH для частичного обновления
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);
                connection.setRequestProperty(SupabaseConfig.HEADER_CONTENT_TYPE, SupabaseConfig.CONTENT_TYPE_JSON);
                connection.setDoOutput(true);

                // Создаем JSON только с измененными полями
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("item_name", itemName);
                jsonBody.put("estimated_cost", cost);
                jsonBody.put("purchase_date", date);

                // Отправляем данные
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(jsonBody.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();

                mainHandler.post(() -> {
                    showLoading(false);

                    if (responseCode == 204) {  // 204 No Content
                        // Обновляем элемент в адаптере
                        InventoryItem updatedItem = new InventoryItem();
                        updatedItem.setId(itemId);
                        updatedItem.setUserId(userId);
                        updatedItem.setItemName(itemName);
                        updatedItem.setEstimatedCost(cost);
                        updatedItem.setPurchaseDate(date);

                        adapter.updateItem(updatedItem);

                        Toast.makeText(this, "Предмет обновлен", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка обновления: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Сетевая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // === МЕТОД ДЛЯ УДАЛЕНИЯ ПРЕДМЕТА ===
    @Override
    public void onDeleteClick(String itemId, String itemName) {
        showDeleteConfirmationDialog(itemId, itemName);
    }

    private void showDeleteConfirmationDialog(String itemId, String itemName) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить предмет?")
                .setMessage("Вы уверены, что хотите удалить \"" + itemName + "\"?")
                .setPositiveButton("Да", (dialog, which) -> deleteInventoryItem(itemId))
                .setNegativeButton("Нет", null)
                .show();
    }

    private void deleteInventoryItem(String itemId) {
        showLoading(true);

        networkExecutor.execute(() -> {
            try {
                URL url = new URL(SupabaseConfig.TABLE_URL + "?id=eq." + itemId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("DELETE");
                connection.setRequestProperty(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);

                int responseCode = connection.getResponseCode();

                mainHandler.post(() -> {
                    showLoading(false);

                    if (responseCode == 204) {  // 204 No Content
                        // Удаляем элемент из адаптера
                        adapter.removeItem(itemId);

                        if (adapter.getItemCount() == 0) {
                            showEmptyState(true);
                        }

                        Toast.makeText(this, "Предмет удален", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ошибка удаления: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Сетевая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // === ОСТАЛЬНЫЕ МЕТОДЫ (оставляем из предыдущей работы) ===

    private void loadInventoryItems() {
        showLoading(true);

        networkExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SupabaseConfig.TABLE_URL + "?select=*");
                connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);
                connection.setRequestProperty(SupabaseConfig.HEADER_CONTENT_TYPE, SupabaseConfig.CONTENT_TYPE_JSON);

                int responseCode = connection.getResponseCode();
                String response = readResponse(connection, responseCode);

                mainHandler.post(() -> {
                    showLoading(false);

                    if (responseCode == 200) {
                        List<InventoryItem> items = parseInventoryItems(response);
                        adapter.setInventoryItems(items);

                        if (items.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                        }

                    } else {
                        Toast.makeText(this, "Ошибка загрузки: " + responseCode, Toast.LENGTH_SHORT).show();
                        showEmptyState(true);
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Сетевая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(HttpURLConnection connection, int responseCode) {
        try {
            java.io.InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            java.util.Scanner scanner = new java.util.Scanner(inputStream).useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } catch (Exception e) {
            return "Ошибка чтения: " + e.getMessage();
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        textViewEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    private List<InventoryItem> parseInventoryItems(String jsonResponse) {
        List<InventoryItem> items = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                InventoryItem item = new InventoryItem();
                item.setId(jsonObject.optString("id"));
                item.setUserId(jsonObject.optString("user_id"));  // Добавляем user_id
                item.setItemName(jsonObject.optString("item_name", "Без названия"));
                item.setEstimatedCost(jsonObject.optDouble("estimated_cost", 0));
                item.setPurchaseDate(jsonObject.optString("purchase_date", ""));

                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
}