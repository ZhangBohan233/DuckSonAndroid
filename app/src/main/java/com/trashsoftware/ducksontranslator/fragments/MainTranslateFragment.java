package com.trashsoftware.ducksontranslator.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.util.Util;
import com.trashsoftware.ducksontranslator.widgets.ResultText;
import com.trashsoftware.ducksontranslator.widgets.TranslatorEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import trashsoftware.duckSonTranslator.result.TranslationResult;

public class MainTranslateFragment extends Fragment {
    public static final String TAG = "MainTranslateFragment";

    public static final String[] LANG_CODES = {"chs", "geg", "chi"};

    MainActivity parent;
    MainViewModel viewModel;
    MaterialButton copyButton;
    private HistoryAccess historyAccess;
    private ScrollView mainScrollView;
    private TranslatorEditText editTextUp;
    private ResultText textBoxDown;
    private MaterialAutoCompleteTextView lang1dropdown, lang2dropdown;
    private ArrayAdapter<LanguageItem> lang1Adapter, lang2Adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        viewModel = MainViewModel.getInstance();

        View root = inflater.inflate(R.layout.fragment_main_translate, container, false);

        parent = (MainActivity) getActivity();
        assert parent != null;
        parent.setTranslateFragment(this);

        historyAccess = HistoryAccess.getInstance(getContext());
        mainScrollView = root.findViewById(R.id.main_scroller);
        copyButton = root.findViewById(R.id.downTextCopyBtn);

        editTextUp = root.findViewById(R.id.textBoxUp);
        textBoxDown = root.findViewById(R.id.textBoxDown);

        // 关联两个东西
        textBoxDown.setSrcField(editTextUp);

        setScrollListeners();

        // 语言设置
        lang1dropdown = root.findViewById(R.id.lang1_dropdown);
        lang2dropdown = root.findViewById(R.id.lang2_dropdown);

        List<LanguageItem> lang1List = new ArrayList<>(List.of(
                new LanguageItem(getContext(), LANG_CODES[0], R.string.chinese),
                new LanguageItem(getContext(), LANG_CODES[1], R.string.geglish),
                new LanguageItem(getContext(), LANG_CODES[2], R.string.chinglish)
        ));
        List<LanguageItem> lang2List = new ArrayList<>(List.of(
                new LanguageItem(getContext(), LANG_CODES[0], R.string.chinese),
                new LanguageItem(getContext(), LANG_CODES[1], R.string.geglish),
                new LanguageItem(getContext(), LANG_CODES[2], R.string.chinglish)
        ));

        lang1Adapter = new NoFilterAdapter(requireContext(),
                R.layout.lang_spinner_dropdown_item,
                R.id.lang_spinner_dropdown_text,
                lang1List);
        lang1dropdown.setAdapter(lang1Adapter);

        lang2Adapter = new NoFilterAdapter(requireContext(),
                R.layout.lang_spinner_dropdown_item,
                R.id.lang_spinner_dropdown_text,
                lang2List);
        lang2dropdown.setAdapter(lang2Adapter);

        addSpinnerChangeListener();
        addTextChangeListener();

        resumeState();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        resumeState();
    }

    private static LanguageItem getItemByLangId(ArrayAdapter<LanguageItem> adapter, String langId) {
        Log.i("MainTranslateFragment", "getItemByLangId: " + adapter.getCount() + " items");
        for (int i = 0; i < adapter.getCount(); i++) {
            LanguageItem item = adapter.getItem(i);
            assert item != null;
            if (item.langId.equals(langId)) {
                return item;
            }
        }
        throw new RuntimeException("Lang id '" + langId + "' not found.");
    }

    private void selectDropdownNoChange(AutoCompleteTextView dropdown,
                                        ArrayAdapter<LanguageItem> adapter,
                                        String langId) {
        selectDropdown(dropdown, adapter, langId, false);
    }

    private void selectDropdown(AutoCompleteTextView dropdown,
                                ArrayAdapter<LanguageItem> adapter,
                                String langId,
                                boolean changeViewModel) {
        LanguageItem item = getItemByLangId(adapter, langId);
        dropdown.setText(item.toString());
        if (changeViewModel) {
            if (dropdown == lang1dropdown) viewModel.transSrcLangId = langId;
            else if (dropdown == lang2dropdown) viewModel.transDstLangId = langId;
            else throw new RuntimeException("Unrecognized dropdown");
        }
    }

    private void resumeState() {
        selectDropdownNoChange(lang1dropdown, lang1Adapter, viewModel.transSrcLangId);
        selectDropdownNoChange(lang2dropdown, lang2Adapter, viewModel.transDstLangId);

        Log.i("MainTranslateFragment", "Dropdown1 items: " + lang1Adapter.getCount());

        if (viewModel.plainOrigText != null) {
            editTextUp.setText(viewModel.plainOrigText);
        }

        notifyTranslationChanged();
    }

    public void reTranslate() {
        resumeState();

        translate();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScrollListeners() {
        // 让焦点在text时scrollview不滑动，焦点不在时才滑动
        mainScrollView.setOnTouchListener((v, event) -> {
            editTextUp.getParent().requestDisallowInterceptTouchEvent(false);
            textBoxDown.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

        editTextUp.setOnTouchListener((v, event) -> {
            editTextUp.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        textBoxDown.setOnTouchListener((v, event) -> {
            textBoxDown.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private void addSpinnerChangeListener() {
        lang1dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String curSrc = viewModel.transSrcLangId;
            String curDst = viewModel.transDstLangId;

            LanguageItem sl = lang1Adapter.getItem(position);
            if (sl == null) {
                Log.i("MainTranslateFragment", "sl is null");
                return;
            }

            viewModel.transSrcLangId = sl.langId;
            if (sl.langId.equals(curDst)) {
                selectDropdown(lang2dropdown, lang2Adapter, curSrc, true);
            }
        });

        lang2dropdown.setOnItemClickListener((parent, view, position, id) -> {
            String curSrc = viewModel.transSrcLangId;
            String curDst = viewModel.transDstLangId;

            LanguageItem sl = lang2Adapter.getItem(position);
            if (sl == null) {
                Log.i("MainTranslateFragment", "sl is null");
                return;
            }

            viewModel.transDstLangId = sl.langId;
            if (sl.langId.equals(curSrc)) {
                selectDropdown(lang1dropdown, lang1Adapter, curDst, true);
            }
        });
    }

    private void addTextChangeListener() {
        editTextUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String s = charSequence.toString();
                updateLangSpinners(s);
                editTextUp.setToastReady();
                viewModel.plainOrigText = s;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        textBoxDown.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    copyButton.setVisibility(VISIBLE);
                } else {
                    copyButton.setVisibility(INVISIBLE);
                }
            }
        });
    }

    private void updateLangSpinners(String editorText) {
        if (!editorText.isEmpty()) {
            String lang = viewModel.getTranslator().autoDetectLanguage(editorText);
            if (Util.arrayContains(LANG_CODES, lang)) {
                viewModel.transSrcLangId = lang;
                autoChangeDstLang(lang);
                selectDropdownNoChange(lang1dropdown, lang1Adapter, lang);
            }
        }
    }

    private void autoChangeDstLang(String srcLangCode) {
        String curDstLangCode = viewModel.transDstLangId;
        if ("chs".equals(srcLangCode)) {
            if ("chs".equals(curDstLangCode)) {
                selectDropdown(lang2dropdown, lang2Adapter, "geg", true);
            }
        } else if ("geg".equals(srcLangCode)) {
            if ("geg".equals(curDstLangCode)) {
                selectDropdown(lang2dropdown, lang2Adapter, "chs", true);
            }
        } else if ("chi".equals(srcLangCode)) {
            if ("chi".equals(curDstLangCode)) {
                selectDropdown(lang2dropdown, lang2Adapter, "chs", true);
            }
        }
    }

    public void translate() {
        editTextUp.clearHighlights();
//        editTextDown.clearHighlights();

        LanguageItem src = getItemByLangId(lang1Adapter, viewModel.transSrcLangId);
        LanguageItem dst = getItemByLangId(lang2Adapter, viewModel.transDstLangId);

        String input = Objects.requireNonNull(editTextUp.getText()).toString();
        if (input.trim().isEmpty()) {
            textBoxDown.setText("");
            return;
        }

        String srcLang = src.langId;
        String dstLang = dst.langId;
        textBoxDown.setTextColor(Color.GRAY);
        textBoxDown.setText(R.string.translating);

        Thread thread = new Thread(() -> {
            long t0 = System.currentTimeMillis();
            TranslationResult translationResult = viewModel.getTranslator().translateByLangCode(
                    input, srcLang, dstLang
            );
            Log.v(TAG, "Translation time: " + (System.currentTimeMillis() - t0));

            String dstText;
            if (translationResult == null) {
                dstText = input;
            } else {
                dstText = translationResult.toString();
            }
            viewModel.setTranslationResult(translationResult);

            historyAccess.insert(HistoryItem.createFromTranslator(
                    srcLang,
                    input,
                    dstLang,
                    dstText,
                    viewModel.getTranslator()
            ));
            requireActivity().runOnUiThread(this::notifyTranslationChanged);
        });
        thread.start();
    }

    public void swapLanguage() {
        CharSequence downCs = textBoxDown.getText();
        if (downCs == null) {
            Toast.makeText(getContext(), R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }
        String down = downCs.toString();
        String curSrc = viewModel.transSrcLangId;
        String curDst = viewModel.transDstLangId;

        editTextUp.setText(down);

        selectDropdown(lang1dropdown, lang1Adapter, curDst, true);
        selectDropdown(lang2dropdown, lang2Adapter, curSrc, true);
        translate();
    }

    public void clearUpText() {
        editTextUp.setText("");
    }

    public void copyDownText() {
        CharSequence downCs = textBoxDown.getText();
        if (downCs == null) {
            Toast.makeText(getContext(), R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }
        String down = downCs.toString();
        if (down.trim().isEmpty()) {
            Toast.makeText(getContext(), R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(down, down);
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    private void notifyTranslationChanged() {
        textBoxDown.setTextColor(Color.BLACK);
        textBoxDown.notifyTranslationChanged();
        editTextUp.setToastReady();
//        editTextUp.enableHighlighting();
    }

    public static class NoFilterAdapter extends ArrayAdapter<LanguageItem> {

        private final LanguageItem[] languages;

        public NoFilterAdapter(@NonNull Context context,
                               int resource,
                               int textViewResourceId,
                               @NonNull List<LanguageItem> objects) {
            super(context, resource, textViewResourceId, objects);

            this.languages = objects.toArray(new LanguageItem[0]);
        }

        @NonNull
        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    results.values = languages; // full list always
                    results.count = languages.length;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    addAll((LanguageItem[]) results.values);
                    notifyDataSetChanged();
                }
            };
        }
    }


    public static class LanguageItem {
        public final int resId;
        final Context context;
        final String langId;

        LanguageItem(Context context, String langId, int resId) {
            this.context = context;
            this.langId = langId;
            this.resId = resId;
        }

        @NonNull
        @Override
        public String toString() {
            return context.getString(resId);
        }
    }
}
