package com.example.a3rd.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.a3rd.R;

public class LoginFragment extends Fragment {

    private EditText etUsername, etPassword;
    private Button btnlogin;
    private TextView tvRegister, tvForgot;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.login, container, false);

        etUsername = root.findViewById(R.id.Email);
        etPassword = root.findViewById(R.id.Password);
        btnlogin = root.findViewById(R.id.btnLogin);
        tvRegister = root.findViewById(R.id.btnRegister);
        tvForgot = root.findViewById(R.id.forgotPassword);

        btnlogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (user.equals("admin") && pass.equals("1234")) {
                Navigation.findNavController(v).navigate(R.id.nav_home);
            }
        });

        tvRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_register);
        });

        tvForgot.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "javiertheo96@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Forgot Password");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        });

        return root;
    }
}