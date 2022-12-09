package com.trashsoftware.ducksontranslator.fragments;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.widgets.ResultText;
import com.trashsoftware.ducksontranslator.widgets.TranslatorEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import trashsoftware.duckSonTranslator.result.TranslationResult;

public class MainTranslateFragment extends Fragment {
    public static final String TAG = "MainTranslateFragment";

    public static final String[] SRC_LANG_CODES = {"", "chs", "geg", "chi"};
    public static final String[] DST_LANG_CODES = {"chs", "geg", "chi"};

    MainActivity parent;
    MainViewModel viewModel;
    private HistoryAccess historyAccess;
    private ScrollView mainScrollView;
    private TranslatorEditText editTextUp;
    private ResultText textBoxDown;
    private View resultFocusIndicator;
    private Spinner lang1Spinner, lang2Spinner;
    private ArrayAdapter<LanguageItem> lang1Adapter, lang2Adapter;
    private ImageButton clearUpTextBtn;

    public static int srcLangIndex(String langCode) {
        for (int i = 0; i < SRC_LANG_CODES.length; i++) {
            if (SRC_LANG_CODES[i].equals(langCode)) return i;
        }
        System.err.println("Src lang '" + langCode + "' not found");
        return 0;
    }

    public static int dstLangIndex(String langCode) {
        for (int i = 0; i < DST_LANG_CODES.length; i++) {
            if (DST_LANG_CODES[i].equals(langCode)) return i;
        }
        System.err.println("Dst lang '" + langCode + "' not found");
        return 0;
    }

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
        clearUpTextBtn = root.findViewById(R.id.upTextClearBtn);
        clearUpTextBtn.setVisibility(View.GONE);

        editTextUp = root.findViewById(R.id.textBoxUp);
        textBoxDown = root.findViewById(R.id.textBoxDown);
        resultFocusIndicator = root.findViewById(R.id.textBoxDownFocusIndicator);
        clearUpTextBtn = root.findViewById(R.id.upTextClearBtn);
        clearUpTextBtn.setVisibility(View.GONE);

        // 关联两个东西
        textBoxDown.setSrcField(editTextUp);
        textBoxDown.setFocusIndicator(resultFocusIndicator);

        setScrollListeners();

        // 语言设置
        lang1Spinner = root.findViewById(R.id.lang1Spinner);
        lang2Spinner = root.findViewById(R.id.lang2Spinner);

        List<LanguageItem> lang1List = new ArrayList<>(List.of(
                new LanguageItem(getContext(), SRC_LANG_CODES[0], R.string.auto_detect, true),
                new LanguageItem(getContext(), SRC_LANG_CODES[1], R.string.chinese, false),
                new LanguageItem(getContext(), SRC_LANG_CODES[2], R.string.geglish, false),
                new LanguageItem(getContext(), SRC_LANG_CODES[3], R.string.chinglish, false)
        ));
        List<LanguageItem> lang2List = new ArrayList<>(List.of(
                new LanguageItem(getContext(), DST_LANG_CODES[0], R.string.chinese, false),
                new LanguageItem(getContext(), DST_LANG_CODES[1], R.string.geglish, false),
                new LanguageItem(getContext(), DST_LANG_CODES[2], R.string.chinglish, false)
        ));

        lang1Adapter = new ArrayAdapter<>(getContext(), R.layout.lang_spinner_item, lang1List);
        lang1Adapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        lang1Spinner.setAdapter(lang1Adapter);

        lang2Adapter = new ArrayAdapter<>(getContext(), R.layout.lang_spinner_item, lang2List);
        lang2Adapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        lang2Spinner.setAdapter(lang2Adapter);

        addSpinnerChangeListener();
        addTextChangeListener();

        resumeState();

        return root;
    }

    @Override
    public void onDestroyView() {
//        textBoxDown.clearSelection();
//        editTextUp.clearHighlights();  // 一清了事最简单

        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        resumeState();
    }

    private void resumeState() {
        lang1Spinner.setSelection(viewModel.transSrcLangSpinnerIndex);
        lang2Spinner.setSelection(viewModel.transDstLangSpinnerIndex);

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
        lang1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                LanguageItem sl = lang1Adapter.getItem(position);
                if (sl.isAutoDetect) {
                    updateLangSpinners(editTextUp.getText().toString());
                }
                viewModel.transSrcLangSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lang2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.transDstLangSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                if (charSequence.length() == 0) {
                    clearUpTextBtn.setVisibility(View.GONE);
                } else {
                    clearUpTextBtn.setVisibility(View.VISIBLE);
                }

                String s = charSequence.toString();
                updateLangSpinners(s);
                editTextUp.setToastReady();
                viewModel.plainOrigText = s;
//                if (viewModel.getTranslationResult() != null &&
//                        !Util.equals(charSequence, viewModel.getTranslationResult().getOriginalText())) {
//                    editTextUp.textContentChanged();
//                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void updateLangSpinners(String editorText) {
        if (editorText.length() > 0) {
            String lang = viewModel.getTranslator().autoDetectLanguage(editorText);
            autoChangeDstLang(lang);
            lang1Adapter.getItem(0).langId = lang;
            lang1Adapter.notifyDataSetChanged();
        }
    }

    private void autoChangeDstLang(String srcLangCode) {
        String curDstLangCode = ((LanguageItem) lang2Spinner.getSelectedItem()).langId;
        if ("chs".equals(srcLangCode)) {
            if ("chs".equals(curDstLangCode)) {
                selectByValue("geg", lang2Spinner);
            }
        } else if ("geg".equals(srcLangCode)) {
            if ("geg".equals(curDstLangCode)) {
                selectByValue("chs", lang2Spinner);
            }
        } else if ("chi".equals(srcLangCode)) {
            if ("chi".equals(curDstLangCode)) {
                selectByValue("chs", lang2Spinner);
            }
        }
    }

    public void translate() {
        editTextUp.clearHighlights();
//        editTextDown.clearHighlights();

        LanguageItem src = (LanguageItem) lang1Spinner.getSelectedItem();
        LanguageItem dst = (LanguageItem) lang2Spinner.getSelectedItem();

        String input = Objects.requireNonNull(editTextUp.getText()).toString();
        if (input.trim().isEmpty()) {
            textBoxDown.setText("");
            return;
        }

        String srcLang;
        if (src.isAutoDetect) {
            if (src.langId.length() > 0) {
                srcLang = src.langId;
            } else {
                srcLang = viewModel.getTranslator().autoDetectLanguage(input);
                src.langId = srcLang;  // 顺便给它设置了
            }
        } else {
            srcLang = src.langId;
        }
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
            getActivity().runOnUiThread(this::notifyTranslationChanged);
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
        LanguageItem src = (LanguageItem) lang1Spinner.getSelectedItem();
        LanguageItem dst = (LanguageItem) lang2Spinner.getSelectedItem();

        String srcLang = src.langId;
        if (srcLang.isEmpty()) {
            srcLang = dst.langId.equals("chs") ? "geg" : "chs";
        }

        editTextUp.setText(down);

        selectByValue(dst.langId, lang1Spinner);
        selectByValue(srcLang, lang2Spinner);
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

        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
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

    private void selectByValue(String langCode, Spinner spinner) {
        if (spinner == lang1Spinner) {
            int index = srcLangIndex(langCode);
            viewModel.transSrcLangSpinnerIndex = index;
            lang1Spinner.setSelection(index);
        } else if (spinner == lang2Spinner) {
            int index = dstLangIndex(langCode);
            viewModel.transDstLangSpinnerIndex = index;
            lang2Spinner.setSelection(index);
        } else {
            throw new RuntimeException("Unexpected spinner");
        }
    }

    public static class LanguageItem {
        public final int resId;
        public final boolean isAutoDetect;
        final Context context;
        String langId;

        LanguageItem(Context context, String langId, int resId, boolean isAutoDetect) {
            this.context = context;
            this.langId = langId;
            this.resId = resId;
            this.isAutoDetect = isAutoDetect;
        }

        @NonNull
        @Override
        public String toString() {
            String base = context.getString(resId);
            if (isAutoDetect) {
                String langName = MainActivity.getLangName(context, langId);
                if (langName.length() > 0) return base + "(" + langName + ")";
                else return base;
            } else {
                return base;
            }
        }
    }
}
