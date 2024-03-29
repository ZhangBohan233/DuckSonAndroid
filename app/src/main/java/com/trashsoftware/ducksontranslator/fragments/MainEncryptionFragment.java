package com.trashsoftware.ducksontranslator.fragments;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.model.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import trashsoftware.duckSonTranslator.encrypt.InvalidLiteralException;
import trashsoftware.duckSonTranslator.encrypt.LiteralRSADecoder;
import trashsoftware.duckSonTranslator.encrypt.LiteralRSAEncoder;
import trashsoftware.duckSonTranslator.encrypt.LiteralRSAUtil;
import trashsoftware.duckSonTranslator.encrypt.PrivateKey;
import trashsoftware.duckSonTranslator.encrypt.PublicKey;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.ChineseLitConverter;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.EngLitConverter;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.HexLiteralConverter;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.LiteralConverter;
import trashsoftware.duckSonTranslator.encrypt.literalConverter.ThinkCleanConverter;

public class MainEncryptionFragment extends Fragment {

    private static LiteralConverter[] LITERAL_CONVERTERS = {
            ThinkCleanConverter.getInstance(),
            ChineseLitConverter.getInstance(),
            EngLitConverter.getInstance(),
            HexLiteralConverter.getInstance()
    };

    private static int[] LITERAL_CONVERTER_NAMES = {
            R.string.converter_think_clean,
            R.string.converter_chn,
            R.string.converter_eng,
            R.string.converter_hex
    };

    ScrollView mainScrollView;
    TextView publicKeyContent, privateKeyContent;
    ImageView keysArrow, encryptionArrow;
    ConstraintLayout keysContainer, encryptionContainer;

    Spinner keyBitsSpinner;
    Spinner literalTypeSpinner;

    TextInputEditText encryptInput;
    TextInputEditText keyInput;
    TextView encryptOutput;

    Button encDecBtn;

    MaterialButtonToggleGroup encryptToggleGroup;
    MaterialButton encryptToggle, decryptToggle;

    private MainViewModel viewModel;
    private MainActivity parent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_encrypt, container, false);

        viewModel = MainViewModel.getInstance();

        parent = (MainActivity) getActivity();
        assert parent != null;
        parent.setEncryptionFragment(this);

        mainScrollView = root.findViewById(R.id.encrypt_main_scroll);

        publicKeyContent = root.findViewById(R.id.public_key_content);
        privateKeyContent = root.findViewById(R.id.private_key_content);
        keyBitsSpinner = root.findViewById(R.id.key_bits_spinner);

        literalTypeSpinner = root.findViewById(R.id.encrypt_literal_type_spinner);

        keysArrow = root.findViewById(R.id.keys_expand_arrow);
        encryptionArrow = root.findViewById(R.id.encrypt_expand_arrow);
        keysContainer = root.findViewById(R.id.keys_expand_container);
        encryptionContainer = root.findViewById(R.id.encrypt_expand_container);

        encryptInput = root.findViewById(R.id.encrypt_input);
        keyInput = root.findViewById(R.id.encrypt_key_input);
        encryptOutput = root.findViewById(R.id.encrypt_output);

        encDecBtn = root.findViewById(R.id.encrypt_btn);

        encryptToggleGroup = root.findViewById(R.id.encrypt_decrypt_toggle);
        encryptToggle = root.findViewById(R.id.toggle_encrypt);
        decryptToggle = root.findViewById(R.id.toggle_decrypt);

        updateKeysFields();
        setupSpinner();
        setupToggle();
        expandByModel();

        refreshOutText();
        setScrollListeners();

        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScrollListeners() {
        // 让焦点在text时scrollview不滑动，焦点不在时才滑动
        mainScrollView.setOnTouchListener((v, event) -> {
            encryptInput.getParent().requestDisallowInterceptTouchEvent(false);
            encryptOutput.getParent().requestDisallowInterceptTouchEvent(false);
            keyInput.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

        encryptInput.setOnTouchListener((v, event) -> {
            encryptInput.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        encryptOutput.setOnTouchListener((v, event) -> {
            encryptOutput.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        keyInput.setOnTouchListener((v, event) -> {
            keyInput.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private void setupSpinner() {
        keyBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.keyBitsSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        literalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.literalConverterSpinnerIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        List<String> names = new ArrayList<>();
        for (int stringId : LITERAL_CONVERTER_NAMES) {
            names.add(getString(stringId));
        }
        ArrayAdapter<String> litTypeAdapter = new ArrayAdapter<>(getContext(),
                R.layout.lang_spinner_item,
                names);
        litTypeAdapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        literalTypeSpinner.setAdapter(litTypeAdapter);

        keyBitsSpinner.setSelection(viewModel.keyBitsSpinnerIndex);
        literalTypeSpinner.setSelection(viewModel.literalConverterSpinnerIndex);
    }

    private void updateKeysFields() {
        if (viewModel.getKeyPair() == null) {
            publicKeyContent.setText("");
            privateKeyContent.setText("");
        } else {
            publicKeyContent.setText(LiteralRSAUtil.keyToLiteral(viewModel.getKeyPair().getPublicKey(), true));
            privateKeyContent.setText(LiteralRSAUtil.keyToLiteral(viewModel.getKeyPair().getPrivateKey(), true));
        }
    }

    private void setupToggle() {
        encryptToggle.setOnClickListener(e -> {
            viewModel.encrypting = true;
            refreshHintsByEncDec();
        });

        decryptToggle.setOnClickListener(e -> {
            viewModel.encrypting = false;
            refreshHintsByEncDec();
        });

        encryptToggleGroup.check(viewModel.encrypting ? R.id.toggle_encrypt : R.id.toggle_decrypt);
    }

    private void refreshHintsByEncDec() {
        keyInput.setHint(viewModel.encrypting ?
                R.string.public_key_input_prompt : R.string.private_key_input_prompt);
        encDecBtn.setText(viewModel.encrypting ?
                R.string.encrypt : R.string.decrypt);
        encryptInput.setHint(viewModel.encrypting ?
                R.string.encrypt_field_prompt : R.string.decrypt_field_prompt);
    }

    private void expandByModel() {
        expandByModel(keysArrow, keysContainer, viewModel.keyFieldExpanded);
        expandByModel(encryptionArrow, encryptionContainer, viewModel.encryptFieldExpanded);
    }

    private void expandByModel(ImageView arrowView, ConstraintLayout container, boolean expanded) {
        arrowView.setImageResource(expanded ?
                R.drawable.ic_chevron_down_24 : R.drawable.ic_chevron_right_24);
        container.setVisibility(expanded ? View.VISIBLE : View.GONE);
    }

    private void refreshOutText() {
        encryptOutput.setText(Objects.requireNonNullElse(viewModel.encryptOutputText, ""));
    }

    public void expandCollapseKeyField() {
        viewModel.keyFieldExpanded = !viewModel.keyFieldExpanded;
        expandByModel();
    }

    public void expandCollapseEncryptField() {
        viewModel.encryptFieldExpanded = !viewModel.encryptFieldExpanded;
        expandByModel();
    }

    public void generateRSAKeys() {
        int bits = Integer.parseInt((String) keyBitsSpinner.getSelectedItem());
        viewModel.generateKeyPair(bits);

        updateKeysFields();
    }

    public void encryptDecrypt() {
        Editable text = encryptInput.getText();
        if (text == null || text.length() == 0) {
            Toast.makeText(getContext(), R.string.fill_encrypt_content, Toast.LENGTH_SHORT).show();
            return;
        }
        String input = text.toString();

        Editable keyText = keyInput.getText();
        if (keyText == null || text.length() == 0) {
            Toast.makeText(getContext(), R.string.fill_rsa_key, Toast.LENGTH_SHORT).show();
            return;
        }
        String keyInput = keyText.toString();

        LiteralConverter converter = LITERAL_CONVERTERS[literalTypeSpinner.getSelectedItemPosition()];

        String out;
        if (viewModel.encrypting) {
            out = encrypt(input, keyInput, converter);
        } else {
            out = decrypt(input, keyInput, converter);
        }

        viewModel.encryptOutputText = out;
        refreshOutText();
    }

    private String encrypt(String input, String key, LiteralConverter converter) {
        PublicKey publicKey;
        try {
            publicKey = LiteralRSAUtil.literalToPublicKey(key);
        } catch (InvalidLiteralException ile) {
            Toast.makeText(getContext(),
                    R.string.invalid_key_text,
                    Toast.LENGTH_SHORT).show();

            return "";
        }

        LiteralRSAEncoder encoder = new LiteralRSAEncoder(publicKey, converter);

        return encoder.encode(input);
    }

    private String decrypt(String input, String key, LiteralConverter converter) {
        PrivateKey privateKey;
        try {
            privateKey = LiteralRSAUtil.literalToPrivateKey(key);
        } catch (InvalidLiteralException ile) {
            Toast.makeText(getContext(),
                    R.string.invalid_key_text,
                    Toast.LENGTH_SHORT).show();

            return "";
        }

        LiteralRSADecoder decoder = new LiteralRSADecoder(privateKey, converter);

        try {
            return decoder.decode(input);
        } catch (InvalidLiteralException ile) {
            Toast.makeText(getContext(),
                    getString(R.string.invalid_encrypted_text, ile.getUnkLiteral()),
                    Toast.LENGTH_SHORT).show();

            return "";
        }
    }

    public void copyPublicKey() {
        copyToClipboard(publicKeyContent);
    }

    public void copyPrivateKey() {
        copyToClipboard(privateKeyContent);
    }

    public void clearEncryptInput() {
        encryptInput.setText("");
    }

    public void clearKeyInput() {
        keyInput.setText("");
    }

    public void copyEncryptOutput() {
        copyToClipboard(encryptOutput);
    }

    private void copyToClipboard(TextView textView) {
        CharSequence downCs = textView.getText();
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
}
