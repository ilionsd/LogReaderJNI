package com.sburov.logparser.domain.logreader;

import org.jetbrains.annotations.NotNull;

public interface LogReaderListener {
    void onFiltered(@NotNull final String line);
}
