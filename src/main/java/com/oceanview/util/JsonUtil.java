package com.oceanview.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JsonUtil — Gson singleton with Java-time serializers registered.
 * Provides a standard ApiResponse wrapper for all servlet responses.
 */
public final class JsonUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** Shared, thread-safe Gson instance with time adapters. */
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, t, ctx) -> new JsonPrimitive(src.format(DATE_FMT)))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, t, ctx) -> LocalDate.parse(json.getAsString(), DATE_FMT))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, t, ctx) -> new JsonPrimitive(src.format(DATETIME_FMT)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, t, ctx) -> LocalDateTime.parse(json.getAsString(),
                            DATETIME_FMT))
            .create();

    private JsonUtil() {
    }

    // ─── Standard API response wrapper ────────────────────────────────────────

    public static class ApiResponse {
        public final boolean success;
        public final String message;
        public final Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }

    public static String ok(String message, Object data) {
        return GSON.toJson(new ApiResponse(true, message, data));
    }

    public static String ok(Object data) {
        return ok("OK", data);
    }

    public static String error(String message) {
        return GSON.toJson(new ApiResponse(false, message, null));
    }
}
