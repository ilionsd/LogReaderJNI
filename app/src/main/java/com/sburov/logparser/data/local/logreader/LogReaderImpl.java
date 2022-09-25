package com.sburov.logparser.data.local.logreader;

import androidx.annotation.NonNull;

import com.sburov.logparser.domain.downloadclient.StreamConsumer;
import com.sburov.logparser.domain.logreader.LogReader;
import com.sburov.logparser.domain.logreader.LogReaderListener;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LogReaderImpl implements LogReader, StreamConsumer {

    private boolean isRunning = false;
    private long nativeLogReaderPtr = 0;
    private Set<LogReaderListener> listeners = new HashSet<>();

    public LogReaderImpl() {
        nativeCreateInstance();
    }

    @Override
    public void receiveData(@NonNull byte[] buffer, int length) {
        addSourceBlock(buffer, length);
    }

    @Override
    public boolean addListener(@NotNull final LogReaderListener listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeListener(@NotNull final LogReaderListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public boolean setFilter(@NotNull final String filter) {
        return nativeSetFilter(filter.getBytes(StandardCharsets.US_ASCII));
    }

    @Override
    public boolean addSourceBlock(@NotNull final byte[] block, final int length) {
        return nativeAddSourceBlock(block, length);
    }

    @Override
    protected void finalize() throws Throwable {
        nativeReleaseInstance();
        super.finalize();
    }

    private void foundLine(byte[] line) {
        String lineStr = new String(line, StandardCharsets.US_ASCII);
        for (LogReaderListener listener : listeners) {
            listener.onFiltered(lineStr);
        }
    }

    private native void nativeGetMatches();

    private synchronized void onProcessingStarted() {
        isRunning = true;
    }

    private synchronized void onProcessingEnded() {
        isRunning = false;
    }

    private native void nativeCreateInstance();

    private native void nativeReleaseInstance();

    private native boolean nativeSetFilter(byte[] filter);

    private native boolean nativeAddSourceBlock(byte[] block, int length);

    private native void nativeProcess();



}
