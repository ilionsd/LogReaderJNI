package com.sburov.logparser.data.local;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sburov.logparser.domain.logreader.LogReaderListener;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FilteredLinesLogger implements LogReaderListener {

    private static final String DEFAULT_FILE_NAME = "results.log";

    private FileOutputStream fileOutputStream = null;

    public FilteredLinesLogger(@NotNull Context context) {
        try {
            this.fileOutputStream = context.openFileOutput(DEFAULT_FILE_NAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFiltered(@NonNull String line) {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.write(line.getBytes(StandardCharsets.US_ASCII));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.finalize();
    }
}
