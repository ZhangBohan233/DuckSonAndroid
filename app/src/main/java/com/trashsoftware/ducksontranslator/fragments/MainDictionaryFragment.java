package com.trashsoftware.ducksontranslator.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.compose.material3.TextFieldKt;

import com.google.android.material.textfield.TextInputEditText;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.widgets.DictAdapter;

import java.util.List;

public class MainDictionaryFragment extends Fragment {
    private static final String BUNDLE_KEY = "internalSavedViewMainDictionaryFragment";

    private MainViewModel viewModel;

    TextInputEditText searchBox;
    //    Spinner dictLangSpinner;
    AutoCompleteTextView dictLangDropdown;
    RecyclerView dictRecycler;
    TextView noResultsPlaceholder, adPlaceholder;

    private DictAdapter dictAdapter;
    private MainActivity parent;
    private String[][] langList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_dict, container, false);

        viewModel = MainViewModel.getInstance();

        parent = (MainActivity) getActivity();
        assert parent != null;
        parent.setDictionaryFragment(this);

        searchBox = root.findViewById(R.id.search_box);
        dictLangDropdown = root.findViewById(R.id.dict_lang_dropdown);
        dictRecycler = root.findViewById(R.id.dict_result_recycler);
        noResultsPlaceholder = root.findViewById(R.id.dict_no_result_placeholder);
        adPlaceholder = root.findViewById(R.id.dict_ad_placeholder);

        bindRecyclerView();
        addInputListener();

        buildLangList();
        addSpinnerListener();

        resumeState();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        resumeState();
    }

    private void resumeState() {
//        dictLangSpinner.setSelection(viewModel.dictLangSpinnerIndex);
        String[] languages = getResources().getStringArray(R.array.dict_lang_list);
        dictLangDropdown.setText(languages[viewModel.dictLangSpinnerIndex], false);
        refreshByModel();
    }

    private void bindRecyclerView() {
        dictAdapter = new DictAdapter(this, noResultsPlaceholder);

        dictRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        dictRecycler.setAdapter(dictAdapter);
    }

    private void buildLangList() {
        String[] arrayValues = requireContext().getResources().getStringArray(R.array.dict_lang_list_values);
        langList = new String[arrayValues.length][];
        for (int i = 0; i < arrayValues.length; i++) {
            String[] split = arrayValues[i].split("-");
            assert split.length == 2;
            langList[i] = split;
        }
    }

    private void addSpinnerListener() {

        String[] languages = getResources().getStringArray(R.array.dict_lang_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.lang_spinner_dropdown_item,
                R.id.lang_spinner_dropdown_text,
                languages
        );

        dictLangDropdown.setAdapter(adapter);

        // selection listener
        dictLangDropdown.setOnItemClickListener((parent, view, position, id) ->
        {
            viewModel.dictLangSpinnerIndex = position;
            Log.d("MainDictionaryFragment", "Sel pos " + position);
        });

        dictLangDropdown.setText(languages[viewModel.dictLangSpinnerIndex], false);
    }

    private void setSearched() {
        viewModel.dictSearched = true;
    }

    public void search() {
        setSearched();
        Editable editable = searchBox.getText();
        if (editable == null) {
            dictAdapter.refreshContent(List.of());
            return;
        }
        String srcText = editable.toString();

        String[] langCodes = getSelectedLangCodes();

        String src;
        String dst;
        String detectedSrc = viewModel.getDictionary().inferSrcLang(srcText);
        if (detectedSrc.equals(langCodes[0])) {
            src = langCodes[0];
            dst = langCodes[1];
        } else if (detectedSrc.equals(langCodes[1])) {
            src = langCodes[1];
            dst = langCodes[0];
        } else {
            dictAdapter.refreshContent(List.of());
            return;
        }

        viewModel.wordResults = viewModel.getDictionary().search(srcText, src, dst);
        refreshByModel();
    }

    private void refreshByModel() {
        if (viewModel.wordResults == null || viewModel.wordResults.isEmpty()) {
            if (viewModel.dictSearched) {
                dictAdapter.refreshContent(List.of());
                adPlaceholder.setVisibility(View.GONE);
            }
        } else {
            dictAdapter.refreshContent(viewModel.wordResults);
            adPlaceholder.setVisibility(View.GONE);
        }
    }

    private String[] getSelectedLangCodes() {
//        int index = dictLangSpinner.getSelectedItemPosition();
        int index = viewModel.dictLangSpinnerIndex;
        return langList[index];
    }

    private void addInputListener() {
        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
            }
            return false;
        });
    }
}
