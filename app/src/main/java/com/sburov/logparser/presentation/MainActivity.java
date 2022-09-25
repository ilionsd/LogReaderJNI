package com.sburov.logparser.presentation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.sburov.logparser.R;
import com.sburov.logparser.data.local.FilteredLinesLogger;
import com.sburov.logparser.data.local.logreader.LogReaderImpl;
import com.sburov.logparser.data.remote.downloadclient.StreamDownloadClientImpl;
import com.sburov.logparser.domain.downloadclient.StreamConsumer;
import com.sburov.logparser.domain.downloadclient.StreamDownloadClient;
import com.sburov.logparser.domain.logreader.LogReader;
import com.sburov.logparser.domain.logreader.LogReaderListener;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements LogReaderListener, SetupFragment.OnSetupReadyListener {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final StreamDownloadClient downloadClient = new StreamDownloadClientImpl();
    private final LogReader logReader = new LogReaderImpl();
    private final FilteredLinesLogger logger = new FilteredLinesLogger(getApplicationContext());
    private SetupFragment setupFragment;
    private OutputFragment outputFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logReader.addListener(this);
        logReader.addListener(logger);

        if (logReader instanceof StreamConsumer) {
            downloadClient.setStreamConsumer((StreamConsumer) logReader);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_setup, SetupFragment.class, savedInstanceState);
        transaction.add(R.id.fragment_output, OutputFragment.class, savedInstanceState);
        transaction.commit();

        setupFragment = (SetupFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_setup);
        outputFragment = (OutputFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_output);

        setupFragment.setOnSetupReadyListener(this);
    }

    @Override
    public void onDestroy() {
        setupFragment.setOnSetupReadyListener(null);

        downloadClient.setStreamConsumer(null);
        logReader.removeListener(logger);
        logReader.removeListener(this);

        super.onDestroy();
    }

    @Override
    public void onSetupReady(@NonNull URL url, @NonNull String filter) {
        logReader.setFilter(filter);
        executorService.submit(() -> downloadClient.download(url));
    }

    @Override
    public void onFiltered(@NonNull String line) {
        outputFragment.addLine(line);
    }
}
