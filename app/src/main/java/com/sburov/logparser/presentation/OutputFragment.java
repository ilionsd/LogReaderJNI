package com.sburov.logparser.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.sburov.logparser.R;

import org.jetbrains.annotations.NotNull;

public class OutputFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_output, container, false);

        adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.item_filtered_lines);

        listView = view.findViewById(R.id.filtered_lines_list);
        listView.setAdapter(adapter);

        return view;
    }

    public void addLine(@NotNull String line) {
        adapter.add(line);
    }
}
