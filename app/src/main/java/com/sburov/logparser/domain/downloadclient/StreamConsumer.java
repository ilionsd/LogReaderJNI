package com.sburov.logparser.domain.downloadclient;

import androidx.annotation.NonNull;

public interface StreamConsumer {
    void receiveData(@NonNull byte[] buffer, int length);
}
