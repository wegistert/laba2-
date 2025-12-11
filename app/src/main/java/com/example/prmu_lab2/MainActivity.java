package com.example.prmu_lab2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prmu_lab2.InventoryAdapter;
import com.example.prmu_lab2.InventoryItem;
import com.example.prmu_lab2.network.SupabaseConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private InventoryAdapter adapter;
    private String accessToken;
    private ExecutorService networkExecutor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация (как в прошлой работе)
        networkExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Проверка авторизации
        checkAuthentication();

        initViews();
        setupRecyclerView();
        loadInventoryItems();
    }

    private void checkAuthentication() {
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);

        if (accessToken == null) {
            // Если нет токена, переходим на экран входа
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);
    }

    private void setupRecyclerView() {
        adapter = new InventoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadInventoryItems() {
        showLoading(true);

        networkExecutor.execute(() -> {
            try {
                // Создаем URL для получения данных
                URL url = new URL(SupabaseConfig.TABLE_URL + "?select=*");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Настраиваем соединение (GET запрос)
                connection.setRequestMethod("GET");
                connection.setRequestProperty(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);
                connection.setRequestProperty(SupabaseConfig.HEADER_CONTENT_TYPE, SupabaseConfig.CONTENT_TYPE_JSON);

                // Получаем ответ
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

                        showToast("Загружено: " + items.size() + " предметов");

                    } else {
                        showToast("Ошибка загрузки: " + responseCode);
                        showEmptyState(true);
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showToast("Сетевая ошибка: " + e.getMessage());
                    showEmptyState(true);
                });
            }
        });
    }

    private List<InventoryItem> parseInventoryItems(String jsonResponse) {
        List<InventoryItem> items = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                InventoryItem item = new InventoryItem();
                item.setId(jsonObject.optString("id"));
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

    private String readResponse(HttpURLConnection connection, int responseCode) {
        try {
            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
}