package com.sburov.logparser.presentation;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.sburov.logparser.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class OutputFragment extends Fragment {

    private ArrayAdapter<String> adapter;
    private ListView listView;

    private AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            listView.getAdapter();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_output, container, false);

        adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.item_filtered_lines);

        listView = view.findViewById(R.id.filtered_lines_list);
        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setMultiChoiceModeListener(multiChoiceModeListener);

        return view;
    }

    public void addLine(@NotNull String line) {
        adapter.add(line);
    }
}
