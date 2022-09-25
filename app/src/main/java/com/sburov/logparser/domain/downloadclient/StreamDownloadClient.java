package com.sburov.logparser.domain.downloadclient;

import java.net.URL;

public interface StreamDownloadClient {
    void download(URL url);
    void setStreamConsumer(StreamConsumer consumer);
}
