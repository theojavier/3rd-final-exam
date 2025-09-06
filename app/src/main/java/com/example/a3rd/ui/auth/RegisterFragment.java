package com.example.a3rd.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a3rd.R;

public class RegisterFragment extends AppCompatActivity {  // ✅ extend AppCompatActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // ✅ this links to register.xml
    }
}