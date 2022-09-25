package com.sburov.logparser.data.local.matchinglines;

import androidx.annotation.NonNull;

import com.sburov.logparser.domain.logreader.LogReaderListener;
import com.sburov.logparser.domain.matchinglines.MatchingLines;

import java.util.LinkedList;
import java.util.List;

public class MatchingLinesImpl implements MatchingLines, LogReaderListener {

    private final List<String> matchingLines = new LinkedList<>();

    @Override
    public void onFiltered(@NonNull String line) {
        matchingLines.add(line);
    }
}
