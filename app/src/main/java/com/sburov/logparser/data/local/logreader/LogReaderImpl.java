package com.sburov.logparser.data.local.logreader;

import androidx.annotation.NonNull;

import com.sburov.logparser.domain.downloadclient.StreamConsumer;
import com.sburov.logparser.domain.logreader.LogReader;
import com.sburov.logparser.domain.logreader.LogReaderListener;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class LogReaderImpl implements LogReader, StreamConsumer {

    private static final int SLEEP_WAITING_MS = 100;

    private boolean isPullingEnabled = false;
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private long nativeLogReaderPtr = 0;
    private final Set<LogReaderListener> listeners = new HashSet<>();

    static {
        System.loadLibrary("logreader");
    }

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
        byte[] bytesASCII = filter.getBytes(StandardCharsets.US_ASCII);
        return nativeSetFilter(bytesASCII, bytesASCII.length);
    }

    @Override
    public boolean addSourceBlock(@NotNull final byte[] block, final int length) {
        return nativeAddSourceBlock(block, length);
    }

    @Override
    public synchronized boolean isPullingEnabled() {
        return isPullingEnabled;
    }

    @Override
    public synchronized void enablePulling(ExecutorService executorService) {
        if (isPullingEnabled) {
            return;
        }
        isPullingEnabled = true;
        executorService.submit(this::pulling);
    }

    @Override
    public synchronized void disablePulling() {
        if (!isPullingEnabled) {
            return;
        }
        isPullingEnabled = false;
    }

    @Override
    protected void finalize() throws Throwable {
        nativeReleaseInstance();
        super.finalize();
    }

    private void pulling() {
        while (true) {
            byte[][] matchingLines = nativeGetMatches();
            if (matchingLines != null) {
                for (byte[] line : matchingLines) {
                    foundLine(line);
                }
            }
            else if (isPullingEnabled() || nativeIsRunning()) {
                try {
                    Thread.sleep(SLEEP_WAITING_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                break;
            }
        }
    }

    private void foundLine(byte[] line) {
        String lineStr = new String(line, StandardCharsets.US_ASCII);
        for (LogReaderListener listener : listeners) {
            listener.onFiltered(lineStr);
        }
    }

    private native void nativeCreateInstance();

    private native void nativeReleaseInstance();

    private native boolean nativeIsRunning();

    private native boolean nativeSetFilter(byte[] filter, int length);

    private native boolean nativeAddSourceBlock(byte[] block, int length);

    private native byte[][] nativeGetMatches();

}
