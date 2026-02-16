package com.yoav_s.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.helper.PasswordUtil;
import com.yoav_s.helper.inputValidators.EmailRule;
import com.yoav_s.helper.inputValidators.EntryValidation;
import com.yoav_s.helper.inputValidators.PasswordRule;
import com.yoav_s.helper.inputValidators.Rule;
import com.yoav_s.helper.inputValidators.RuleOperation;
import com.yoav_s.helper.inputValidators.Validator;
import com.yoav_s.model.User;
import com.yoav_s.tashtit.ACTIVITIES.BASE.BaseActivity;
import com.yoav_s.tashtit.R;
import com.yoav_s.viewmodel.UsersViewModel;

public class RegisterActivity extends BaseActivity implements EntryValidation {
    private UsersViewModel usersViewModel;
    private EditText etUserName, etEmail, etPassword, etRePassword;
    private Spinner spRole;
    private MaterialButton btnRegister, btnCancel;

    private boolean waitingForEmailCheck = false;
    private boolean waitingForCreateUser = false;
    private User pendingUser = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setLayout(R.layout.activity_register);
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
        etUserName = findViewById(R.id.etUserName);
        spRole = findViewById(R.id.spRole);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRePassword = findViewById(R.id.etRePassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnCancel = findViewById(R.id.btnCancel);

        setupRoleSpinner();

        // clear errors when user focuses again
        etUserName.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) etUserName.setError(null); });
        etEmail.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) etEmail.setError(null); });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) etPassword.setError(null); });
        etRePassword.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) etRePassword.setError(null); });

        setListeners();
    }

    private void setupRoleSpinner() {
        String[] roles = new String[]{
                "Select role",
                "User",
                "Content creator"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);

        spRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // clear spinner error (Validator sets error on selected view)
                if (view instanceof TextView) {
                    ((TextView) view).setError(null);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    @Override
    protected void setListeners() {
        btnRegister.setOnClickListener(v -> doRegister());

        btnCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void setViewModel() {
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        // 1) observe email exists result
        usersViewModel.getEmailExists().observe(this, exists -> {
            if (!waitingForEmailCheck) return;
            waitingForEmailCheck = false;

            if (exists == null) {
                hideProgressDialog();
                Toast.makeText(this, "Error checking email. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (exists) {
                hideProgressDialog();
                etEmail.setError("Email already exists");
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // email not exists => create user
            if (pendingUser == null) {
                hideProgressDialog();
                Toast.makeText(this, "Unexpected error. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            showProgressDialog(null, "Creating account...");
            waitingForCreateUser = true;
            usersViewModel.add(pendingUser);
        });

        // 2) observe create user success
        usersViewModel.getSuccess().observe(this, success -> {
            if (!waitingForCreateUser) return;
            waitingForCreateUser = false;

            hideProgressDialog();

            if (Boolean.TRUE.equals(success)) {
                currentUser = pendingUser;
                Toast.makeText(this, "Welcome " + pendingUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void doRegister() {
        if (!validate()) return;

        String userName = etUserName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();

        if (!password.equals(rePassword)) {
            etRePassword.setError("Passwords do not match");
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        // Safety: PasswordUtil uses java.util.Base64 (API 26+). On API < 26 it returns "".
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            Toast.makeText(this, "Password hashing not supported on this Android version.", Toast.LENGTH_SHORT).show();
            return;
        }

        // build user
        User.Role role = getSelectedRole();
        if (role == null) {
            // should be caught by Validator, but just in case
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingUser = new User(userName, role, email, hashedPassword);

        // check email exists first
        showProgressDialog(null, "Checking email...");
        waitingForEmailCheck = true;
        usersViewModel.checkEmailExists(email);
    }

    private User.Role getSelectedRole() {
        int pos = spRole.getSelectedItemPosition();
        if (pos == 1) return User.Role.USER;
        if (pos == 2) return User.Role.CONTENT_CREATOR;
        return null;
    }

    private void goToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void setValidation() {
        Validator.clear();

        Validator.add(new Rule(etUserName, RuleOperation.REQUIRED, "Please enter user name"));

        Validator.add(new Rule(spRole, RuleOperation.REQUIRED, "Please select role"));

        Validator.add(new Rule(etEmail, RuleOperation.REQUIRED, "Please enter email"));
        Validator.add(new EmailRule(etEmail, RuleOperation.TEXT, "Email is not valid"));

        Validator.add(new Rule(etPassword, RuleOperation.REQUIRED, "Please enter password"));
        Validator.add(new PasswordRule(etPassword, RuleOperation.PASSWORD, "Password must be 4–8 chars and include: lowercase, uppercase, number, and one of !@#$%^&*()_+"));
        Validator.add(new Rule(etRePassword, RuleOperation.REQUIRED, "Please retype password"));
    }

    @Override
    public boolean validate() {
        setValidation();
        return Validator.validate();
    }
}