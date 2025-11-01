package com.trashsoftware.ducksontranslator.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.trashsoftware.ducksontranslator.BuildConfig;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.util.AssetsReader;

import java.io.IOException;

public class MainAboutFragment extends Fragment {

    public static final String TAG = "MainAboutFragment";
    MainViewModel viewModel;

    TextView emailView, closedBetaUsersView, appVersionView, coreVersionView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_about, container, false);

        viewModel = MainViewModel.getInstance();

        emailView = root.findViewById(R.id.email_content);
        closedBetaUsersView = root.findViewById(R.id.cb_users);
        appVersionView = root.findViewById(R.id.app_version_content);
        coreVersionView = root.findViewById(R.id.core_version_content);

        appVersionView.setText(BuildConfig.VERSION_NAME);
        coreVersionView.setText(viewModel.getShownCoreVersion());

        fillTexts();

        return root;
    }

    private void fillTexts() {
        String[] emails = getResources().getStringArray(R.array.contact_emails);
        String emailText = String.join("\n", emails);
        emailView.setText(emailText);

        try {
            String usersList = AssetsReader.readAssetsLinesAsString(requireContext(),
                    "closed_beta_user_list.txt");
            closedBetaUsersView.setText(usersList);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }
}
