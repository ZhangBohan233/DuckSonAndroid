package com.trashsoftware.ducksontranslator.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.compose.material3.TextFieldKt;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.widgets.DictAdapter;
import com.trashsoftware.ducksontranslator.widgets.DictPlaceholderAdapter;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import trashsoftware.duckSonTranslator.words.WordResult;

public class MainDictionaryFragment extends Fragment {
    private static final String BUNDLE_KEY = "internalSavedViewMainDictionaryFragment";

    private MainViewModel viewModel;

    TextInputEditText searchBox;
    //    Spinner dictLangSpinner;
    AutoCompleteTextView dictLangDropdown;
    RecyclerView dictRecycler, dictPlaceholderRecycler;
    TextView noResultsPlaceholder, adPlaceholder;
    ShimmerFrameLayout dictRecyclerShimmer;

    private DictAdapter dictAdapter;
    private MainActivity parent;
    private String[][] langList;

    private ExecutorService executor;
    private Handler mainHandler;

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
        dictRecyclerShimmer = root.findViewById(R.id.dict_shimmer_layout);
        dictPlaceholderRecycler = root.findViewById(R.id.dict_placeholder_recycler);

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

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

        dictRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        dictRecycler.setAdapter(dictAdapter);

        dictPlaceholderRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        DictPlaceholderAdapter placeholderAdapter = new DictPlaceholderAdapter(3); // e.g., 5 dummy items
        dictPlaceholderRecycler.setAdapter(placeholderAdapter);
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

    private void showLoading() {
        dictRecycler.setVisibility(View.GONE);
        dictRecyclerShimmer.setVisibility(View.VISIBLE);
        dictPlaceholderRecycler.setVisibility(View.VISIBLE);

        dictRecyclerShimmer.startShimmer();
    }

    private void hideLoading() {
        dictRecyclerShimmer.stopShimmer();

        dictPlaceholderRecycler.setVisibility(View.GONE);
        dictRecyclerShimmer.setVisibility(View.GONE);
        dictRecycler.setVisibility(View.VISIBLE);
    }

    private void setSearched() {
        viewModel.dictSearched = true;
    }

    public void search() {
        setSearched();
        Editable editable = searchBox.getText();
        if (editable == null || editable.isEmpty()) {
            dictAdapter.refreshContent(List.of());
            return;
        }
        String text = editable.toString();
        if (text.length() > 6 && viewModel.getDictionary().getOptions().isUseSameSoundChar()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.dict_search_too_long)
                    .setMessage(R.string.dict_search_too_long_msg)
                    .setCancelable(true)
                    .setPositiveButton(R.string.yes, (dialog, which) -> searchEssential(text))
                    .setNegativeButton(R.string.no, (dialog, which) -> {
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            searchEssential(text);
        }
    }

    private void searchEssential(String srcText) {
        adPlaceholder.setVisibility(View.GONE);
        showLoading();

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
        Log.v("MainDictionaryFragment", "src: " + src + " dst: " + dst);

        Future<List<WordResult>> future = executor.submit(() ->
                viewModel.getDictionary().search(srcText, src, dst));

        executor.execute(() -> {
            try {
                // Wait up to 5 seconds for completion
                viewModel.wordResults = future.get(5, TimeUnit.SECONDS);

                // Update UI on main thread
                mainHandler.post(this::refreshByModel);
            } catch (TimeoutException e) {
                // Task took too long — cancel it
                future.cancel(true);
                viewModel.wordResults = null;
                mainHandler.post(() -> {
                            refreshByModel();
                            noResultsPlaceholder.setText(R.string.dict_timeout);
                        }
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainDictionaryFragment", "Error: " + e.getMessage());
                viewModel.wordResults = null;
                mainHandler.post(() -> {
                            noResultsPlaceholder.setText(requireContext().getString(R.string.error_with_message, e.getMessage()));
                            refreshByModel();
                        }
                );
            }
        });
    }

    private void refreshByModel() {
        hideLoading();

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
