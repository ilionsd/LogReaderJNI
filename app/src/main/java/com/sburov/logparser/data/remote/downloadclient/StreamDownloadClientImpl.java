package com.sburov.logparser.data.remote.downloadclient;

import com.sburov.logparser.domain.downloadclient.StreamDownloadListener;
import com.sburov.logparser.domain.downloadclient.StreamConsumer;
import com.sburov.logparser.domain.downloadclient.StreamDownloadClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class StreamDownloadClientImpl implements StreamDownloadClient {

    public static final int DEFAULT_BUFFER_LENGTH = 1024 * 1024; // 1Mi

    private final byte[] buffer;

    private StreamConsumer consumer;
    private StreamDownloadListener listener;

    public StreamDownloadClientImpl() {
        this(DEFAULT_BUFFER_LENGTH);
    }
    public StreamDownloadClientImpl(final int bufferLength) {
        buffer = new byte[bufferLength];
    }

    @Override
    public void setStreamConsumer(StreamConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void setStreamDownloadListener(StreamDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public void download(URL url) {
        InputStream is = null;
        int offset = 0;
        int length = buffer.length;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000); // 5 seconds connectTimeout
            connection.setReadTimeout(5000 ); // 5 seconds socketTimeout

            // Connect
            connection.connect(); // Without this line, method readLine() stucks!!!
            // because it reads incorrect data, possibly from another memory area

            is = url.openStream();
            while (true) {
                assert length <= buffer.length;
                length = is.read(buffer, offset, length);
                if (length == -1) {
                    break;
                }
                length += offset;

                int blockLength = findLastLineEnd(buffer, length) + 1;
                if (blockLength != 0 && consumer != null) {
                    consumer.receiveData(buffer, blockLength);
                }

                int tail = length - blockLength;
                for (int k = 0; k < tail; k++) {
                    buffer[k] = buffer[blockLength + k];
                }
                offset = tail;
                length = buffer.length - tail;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (listener != null) {
                listener.onDownloadComplete();
            }
        }
    }

    private int findLastLineEnd(@NotNull byte[] buffer, int length) {
        int k = length - 1;
        while (k >= 0 && buffer[k] != '\n') {
            k--;
        }
        return k;
    }
}
