package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yoav_s.helper.StringUtil;
import com.yoav_s.helper.inputValidators.EmailRule;
import com.yoav_s.helper.inputValidators.EntryValidation;
import com.yoav_s.helper.inputValidators.Rule;
import com.yoav_s.helper.inputValidators.RuleOperation;
import com.yoav_s.helper.inputValidators.Validator;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.UsersViewModel;

public class SignInActivity extends BaseActivity implements EntryValidation {
    private UsersViewModel usersViewModel;
    private EditText etEmail, etPassword;
    private MaterialButton btnSignIn, btnRegister, btnGuestMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_sign_in);
        setBottomNavigationVisibility(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeActivity();
    }
    @Override
    protected void initializeActivity() {
        initializeViews();
        setViewModel();
    }
    @Override
    protected void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);
        btnGuestMode = findViewById(R.id.btnGuestMode);

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etEmail.setError(null);
        });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) etPassword.setError(null);
        });

        setListeners();
    }
    @Override
    protected void setListeners() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) return;

                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString();

                showProgressDialog(null, "Signing in...");
                usersViewModel.logIn(email, password);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, RegisterActivity.class));
            }
        });

        btnGuestMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUser = null;
                goToMain();
            }
        });
    }
    @Override
    protected void setViewModel() {
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        usersViewModel.getEntity().observe(this, user -> {
            hideProgressDialog();

            if (user != null) {
                currentUser = user;
                Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "Email doesn't exist or password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void setValidation() {
        Validator.clear();

        Validator.add(new Rule(etEmail, RuleOperation.REQUIRED, "Please enter email"));
        Validator.add(new EmailRule(etEmail, RuleOperation.TEXT, "Email is not valid"));

        Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, "Please enter password"));
    }

    @Override
    public boolean validate() {
        setValidation();
        return Validator.validate();
    }
}