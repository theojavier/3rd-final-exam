package com.example.a3rd.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.a3rd.R;

public class ForgotFragment extends AppCompatActivity {

    EditText emailField;
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget); // link with forget.xml

        emailField = findViewById(R.id.forgetEmail);
        sendButton = findViewById(R.id.btnSendReset);

        sendButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                // 🔴 Normally you check with your server if this email exists
                // Example: call API -> if exists, send email reset link
                Toast.makeText(this, "Reset link sent to " + email, Toast.LENGTH_LONG).show();

                // TODO: Replace this with server request to send reset link
            }
        });
    }
}
