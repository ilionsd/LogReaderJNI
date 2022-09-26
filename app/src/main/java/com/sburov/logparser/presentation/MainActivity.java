package com.sburov.logparser.presentation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.sburov.logparser.R;
import com.sburov.logparser.data.local.FilteredLinesLogger;
import com.sburov.logparser.data.local.logreader.LogReaderImpl;
import com.sburov.logparser.data.remote.downloadclient.StreamDownloadClientImpl;
import com.sburov.logparser.domain.downloadclient.StreamConsumer;
import com.sburov.logparser.domain.downloadclient.StreamDownloadClient;
import com.sburov.logparser.domain.downloadclient.StreamDownloadListener;
import com.sburov.logparser.domain.logreader.LogReader;
import com.sburov.logparser.domain.logreader.LogReaderListener;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements LogReaderListener, StreamDownloadListener, SetupFragment.OnSetupReadyListener {

    private static final int PERMISSION_REQUEST_CODE = 1234597;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final StreamDownloadClient downloadClient = new StreamDownloadClientImpl();
    private final LogReader logReader = new LogReaderImpl();
    private FilteredLinesLogger logger = null;
    private SetupFragment setupFragment;
    private OutputFragment outputFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = new FilteredLinesLogger(getApplicationContext());

        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        logReader.addListener(this);
        logReader.addListener(logger);

        if (logReader instanceof StreamConsumer) {
            downloadClient.setStreamConsumer((StreamConsumer) logReader);
        }
        downloadClient.setStreamDownloadListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_setup, SetupFragment.class, savedInstanceState);
        transaction.add(R.id.fragment_output, OutputFragment.class, savedInstanceState);
        transaction.commitNow();

        setupFragment = (SetupFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_setup);
        outputFragment = (OutputFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_output);

        setupFragment.setOnSetupReadyListener(this);
    }

    @Override
    public void onDestroy() {
        setupFragment.setOnSetupReadyListener(null);

        downloadClient.setStreamDownloadListener(null);
        downloadClient.setStreamConsumer(null);
        logReader.removeListener(logger);
        logReader.removeListener(this);

        super.onDestroy();
    }

    @Override
    public void onSetupReady(@NotNull URL url, @NotNull String filter) {
        logReader.setFilter(filter);
        logReader.enablePulling(executorService);
        executorService.submit(() -> downloadClient.download(url));
    }

    @Override
    public void onDownloadComplete() {
        logReader.disablePulling();
    }

    @Override
    public void onFiltered(@NotNull String line) {
        runOnUiThread(() -> outputFragment.addLine(line));
    }

    public boolean checkPermission(@NotNull String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }


}
