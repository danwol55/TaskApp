package com.example.taskapp;

import android.app.PendingIntent;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class IntentTypeAdapter extends TypeAdapter<PendingIntent> {

    @Override
    public void write(JsonWriter out, PendingIntent value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.toString());
    }

    @Override
    public PendingIntent read(JsonReader in) {
        return null;
    }
}
