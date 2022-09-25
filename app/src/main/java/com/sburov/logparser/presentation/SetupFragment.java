package com.sburov.logparser.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sburov.logparser.R;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

public class SetupFragment extends Fragment {

    public interface OnSetupReadyListener {
        void onSetupReady(@NonNull URL url, @NotNull String filter);
    }

    private OnSetupReadyListener listener;

    private EditText textEditUrl;
    private EditText textEditFilter;
    private TextView textViewErrorMessage;
    private Button buttonOk;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        textEditUrl = view.findViewById(R.id.editTextUrl);
        textEditFilter = view.findViewById(R.id.editTextFilter);
        textViewErrorMessage = view.findViewById(R.id.errorMessage);
        buttonOk = view.findViewById(R.id.buttonOk);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setOnSetupReadyListener(OnSetupReadyListener listener) {
        this.listener = listener;
    }

    public void disableOkButton() {
        buttonOk.setEnabled(false);
    }

    public void enableOkButton() {
        buttonOk.setEnabled(true);
    }

    private void addButtonOkOnClickListener() {
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewErrorMessage.clearComposingText();
                lockText();
                if (textEditUrl.length() == 0) {
                    textViewErrorMessage.setText(R.string.error_url_empty);
                    unlockText();
                    return;
                }
                if (textEditFilter.length() == 0) {
                    textViewErrorMessage.setText(R.string.error_filter_empty);
                    unlockText();
                    return;
                }

                URL url;
                try {
                    url = new URL(textEditUrl.getText().toString());
                } catch (MalformedURLException e) {
                    textViewErrorMessage.setText(R.string.error_url_malformed);
                    unlockText();
                    return;
                }

                if (listener != null) {
                    listener.onSetupReady(url, textEditFilter.getText().toString());
                }
            }
        });
    }

    private void lockText() {
        textEditUrl.setEnabled(false);
        textEditFilter.setEnabled(false);
    }

    private void unlockText() {
        textEditUrl.setEnabled(true);
        textEditFilter.setEnabled(true);
    }
}
