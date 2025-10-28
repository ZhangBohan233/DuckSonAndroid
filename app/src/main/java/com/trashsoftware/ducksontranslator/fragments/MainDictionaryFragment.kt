package com.trashsoftware.ducksontranslator.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.material3.TextField
import com.google.android.material.textfield.TextInputEditText
import com.trashsoftware.ducksontranslator.MainActivity
import com.trashsoftware.ducksontranslator.R
import com.trashsoftware.ducksontranslator.model.MainViewModel
import com.trashsoftware.ducksontranslator.widgets.DictAdapter
import trashsoftware.duckSonTranslator.words.WordResult

class MainDictionaryFragment : Fragment() {
    private var viewModel: MainViewModel? = null

    var searchBox: TextInputEditText? = null
    var dictLangSpinner: Spinner? = null
    var dictRecycler: RecyclerView? = null
    var noResultsPlaceholder: TextView? = null
    var adPlaceholder: TextView? = null

    private var dictAdapter: DictAdapter? = null
    private var parent: MainActivity? = null
    private var langList: Array<Array<String?>?> = arrayOfNulls<Array<String?>>(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main_dict, container, false)

        viewModel = MainViewModel.getInstance()

        parent = getActivity() as MainActivity?
        checkNotNull(parent)
        parent!!.setDictionaryFragment(this)

        searchBox = root.findViewById<TextInputEditText>(R.id.search_box)
        dictLangSpinner = root.findViewById<Spinner>(R.id.dict_lang_spinner)
        dictRecycler = root.findViewById<RecyclerView>(R.id.dict_result_recycler)
        noResultsPlaceholder = root.findViewById<TextView>(R.id.dict_no_result_placeholder)
        adPlaceholder = root.findViewById<TextView>(R.id.dict_ad_placeholder)

        bindRecyclerView()
        addInputListener()

        buildLangList()
        addSpinnerListener()

        resumeState()

        return root
    }

    override fun onResume() {
        super.onResume()

        resumeState()
    }

    private fun resumeState() {
        dictLangSpinner!!.setSelection(viewModel!!.dictLangSpinnerIndex)
        refreshByModel()
    }

    private fun bindRecyclerView() {
        dictAdapter = DictAdapter(this, noResultsPlaceholder)
        dictRecycler!!.addItemDecoration(
            DividerItemDecoration(
                dictRecycler!!.getContext(),
                DividerItemDecoration.VERTICAL
            )
        )

        dictRecycler!!.setLayoutManager(LinearLayoutManager(getContext()))
        dictRecycler!!.setAdapter(dictAdapter)
    }

    private fun buildLangList() {
        val arrayValues =
            requireContext().getResources().getStringArray(R.array.dict_lang_list_values)
        langList = arrayOfNulls<Array<String?>>(arrayValues.size)
        for (i in arrayValues.indices) {
            val split: Array<String?> =
                arrayValues[i]!!.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            assert(split.size == 2)
            langList[i] = split
        }
    }

    private fun addSpinnerListener() {
        dictLangSpinner!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel!!.dictLangSpinnerIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })
    }

    private fun setSearched() {
        viewModel!!.dictSearched = true
    }

    fun search() {
        setSearched()
        val editable = searchBox!!.getText()
        if (editable == null) {
            dictAdapter!!.refreshContent(mutableListOf<WordResult?>())
            return
        }
        val srcText = editable.toString()

        val langCodes = this.selectedLangCodes

        val src: String?
        val dst: String?
        val detectedSrc = viewModel!!.getDictionary().inferSrcLang(srcText)
        if (detectedSrc == langCodes[0]) {
            src = langCodes[0]
            dst = langCodes[1]
        } else if (detectedSrc == langCodes[1]) {
            src = langCodes[1]
            dst = langCodes[0]
        } else {
            dictAdapter!!.refreshContent(mutableListOf<WordResult?>())
            return
        }

        viewModel!!.wordResults = viewModel!!.getDictionary().search(srcText, src, dst)
        refreshByModel()
    }

    private fun refreshByModel() {
        if (viewModel!!.wordResults == null || viewModel!!.wordResults.isEmpty()) {
            if (viewModel!!.dictSearched) {
                dictAdapter!!.refreshContent(mutableListOf<WordResult?>())
                adPlaceholder!!.setVisibility(View.GONE)
            }
        } else {
            dictAdapter!!.refreshContent(viewModel!!.wordResults)
            adPlaceholder!!.setVisibility(View.GONE)
        }
    }

    private val selectedLangCodes: Array<String?>
        get() {
            val index = dictLangSpinner!!.getSelectedItemPosition()
            return langList[index]!!
        }

    private fun addInputListener() {
        searchBox!!.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search()
            }
            false
        })
    }

    companion object {
        private const val BUNDLE_KEY = "internalSavedViewMainDictionaryFragment"
    }
}
