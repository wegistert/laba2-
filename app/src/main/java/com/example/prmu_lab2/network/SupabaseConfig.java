package com.example.prmu_lab2.network;

public class SupabaseConfig {

    public static final String SUPABASE_URL = "https://kupawyfoudcpeiktbqyi.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt1cGF3eWZvdWRjcGVpa3RicXlpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0NjM5NzksImV4cCI6MjA4MTAzOTk3OX0.tk17bvwlvHJ4wSh1gzu7j7lwZYjM-aVl23h42K5pH5w";

    // Эндпоинты аутентификации
    public static final String AUTH_SIGNUP_URL = SUPABASE_URL + "/auth/v1/signup";
    public static final String AUTH_SIGNIN_URL = SUPABASE_URL + "/auth/v1/token?grant_type=password";

    // Эндпоинт для таблицы домашнего инвентаря
    public static final String TABLE_URL = SUPABASE_URL + "/rest/v1/home_inventory";

    // Заголовки для запросов
    public static final String HEADER_API_KEY = "apikey";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // Метод для получения URL с фильтром по ID
    public static String getItemUrl(String itemId) {
        return TABLE_URL + "?id=eq." + itemId;
    }
}