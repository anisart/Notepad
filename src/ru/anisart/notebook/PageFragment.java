package ru.anisart.notebook;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public class PageFragment extends Fragment {

    private static final String ARGUMENT_ID = "arg_id";
    private static final String ARGUMENT_TEXT_CONTENT = "arg_text_content";
    private static final String TAG = PageFragment.class.getSimpleName();

    private long noteId;
    private String noteTextContent;
    private EditText textField;

    public static PageFragment newInstance(long id, String textContent) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_TEXT_CONTENT, textContent);
        arguments.putLong(ARGUMENT_ID, id);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteTextContent = getArguments().getString(ARGUMENT_TEXT_CONTENT);
        noteId = getArguments().getLong(ARGUMENT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View pageView = inflater.inflate(R.layout.fragment, null);

        textField = (EditText) pageView.findViewById(R.id.textField);
        textField.setText(noteTextContent);
        textField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textField.setCursorVisible(true);
//                textField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            }
        });
        textField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    textField.setCursorVisible(false);
//                    textField.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    if (!textField.getText().toString().equals(noteTextContent)) {
                        noteTextContent = textField.getText().toString();
                        ContentValues cv = new ContentValues();
                        cv.put(NotesProvider.TEXT_CONTENT, noteTextContent);
                        Uri uri = ContentUris.withAppendedId(NotesProvider.CONTENT_URI, noteId);
                        getActivity().getContentResolver().update(uri, cv, null, null);
                    }
                }
            }
        });

        return pageView;
    }

}