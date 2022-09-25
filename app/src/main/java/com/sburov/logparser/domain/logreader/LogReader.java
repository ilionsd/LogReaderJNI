package com.sburov.logparser.domain.logreader;

import org.jetbrains.annotations.NotNull;

public interface LogReader {
    boolean addListener(@NotNull final LogReaderListener listener);
    boolean removeListener(@NotNull final LogReaderListener listener);
    boolean setFilter(@NotNull final String filter);
    boolean addSourceBlock(@NotNull final byte[] block, final int length);
}
