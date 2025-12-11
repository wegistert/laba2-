package com.example.prmu_lab2.network;

public class SupabaseConfig {

    public static final String SUPABASE_URL = "https://kupawyfoudcpeiktbqyi.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt1cGF3eWZvdWRjcGVpa3RicXlpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU0NjM5NzksImV4cCI6MjA4MTAzOTk3OX0.tk17bvwlvHJ4wSh1gzu7j7lwZYjM-aVl23h42K5pH5w";

    // Эндпоинты аутентификации
    public static final String AUTH_SIGNUP_URL = SUPABASE_URL + "/auth/v1/signup";
    public static final String AUTH_SIGNIN_URL = SUPABASE_URL + "/auth/v1/token?grant_type=password";

    // Эндпоинт для таблицы домашнего инвентаря
    public static final String TABLE_URL = SUPABASE_URL + "/rest/v1/wegistert";
}