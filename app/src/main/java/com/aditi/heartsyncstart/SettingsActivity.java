package com.aditi.heartsyncstart;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar seekBarMinAge, seekBarMaxAge;
    private TextView tvMinAge, tvMaxAge;
    private RadioGroup rgGenderPreference;
    private RadioButton rbMale, rbFemale, rbEveryone;
    private Spinner spinnerDistance;
    private Switch switchInvisibleMode;
    private Button btnSaveSettings, btnChangePassword, btnDeleteAccount, btnBack;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, settingsRef;
    private String currentUserId;

    private int minAge = 18;
    private int maxAge = 35;
    private String genderPreference = "Everyone";
    private int distanceFilter = 50; // km
    private boolean invisibleMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);
        settingsRef = userRef.child("settings");

        // Initialize Views
        seekBarMinAge = findViewById(R.id.seekBarMinAge);
        seekBarMaxAge = findViewById(R.id.seekBarMaxAge);
        tvMinAge = findViewById(R.id.tvMinAge);
        tvMaxAge = findViewById(R.id.tvMaxAge);
        rgGenderPreference = findViewById(R.id.rgGenderPreference);
        rbMale = findViewById(R.id.rbPrefMale);
        rbFemale = findViewById(R.id.rbPrefFemale);
        rbEveryone = findViewById(R.id.rbPrefEveryone);
        spinnerDistance = findViewById(R.id.spinnerDistance);
        switchInvisibleMode = findViewById(R.id.switchInvisibleMode);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnBack = findViewById(R.id.btnBackSettings);

        // Load current settings
        loadSettings();

        // Setup SeekBars
        setupAgeSeekBars();

        // Invisible Mode Switch
        switchInvisibleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            invisibleMode = isChecked;
        });

        // Save Settings Button
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        // Change Password Button
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Delete Account Button
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Back Button
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    minAge = snapshot.child("minAge").getValue(Integer.class) != null ?
                            snapshot.child("minAge").getValue(Integer.class) : 18;
                    maxAge = snapshot.child("maxAge").getValue(Integer.class) != null ?
                            snapshot.child("maxAge").getValue(Integer.class) : 35;
                    genderPreference = snapshot.child("genderPreference").getValue(String.class) != null ?
                            snapshot.child("genderPreference").getValue(String.class) : "Everyone";
                    distanceFilter = snapshot.child("distanceFilter").getValue(Integer.class) != null ?
                            snapshot.child("distanceFilter").getValue(Integer.class) : 50;
                    invisibleMode = snapshot.child("invisibleMode").getValue(Boolean.class) != null ?
                            snapshot.child("invisibleMode").getValue(Boolean.class) : false;

                    // Update UI
                    seekBarMinAge.setProgress(minAge - 18);
                    seekBarMaxAge.setProgress(maxAge - 18);
                    tvMinAge.setText(minAge + " years");
                    tvMaxAge.setText(maxAge + " years");

                    // Set gender preference
                    // Set gender preference - convert database value to UI text
                    switch (genderPreference) {
                        case "Male":
                            rbMale.setChecked(true);
                            break;
                        case "Female":
                            rbFemale.setChecked(true);
                            break;
                        default:
                            rbEveryone.setChecked(true);
                            break;
                    }

                    // Set distance
                    setDistanceSpinner(distanceFilter);

                    // Set invisible mode
                    switchInvisibleMode.setChecked(invisibleMode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SettingsActivity.this, "Failed to load settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAgeSeekBars() {
        seekBarMinAge.setMax(42); // 18 to 60
        seekBarMaxAge.setMax(42);

        seekBarMinAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minAge = progress + 18;
                tvMinAge.setText(minAge + " years");

                // Ensure min age is not greater than max age
                if (minAge > maxAge) {
                    maxAge = minAge;
                    seekBarMaxAge.setProgress(maxAge - 18);
                    tvMaxAge.setText(maxAge + " years");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarMaxAge.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                maxAge = progress + 18;
                tvMaxAge.setText(maxAge + " years");

                // Ensure max age is not less than min age
                if (maxAge < minAge) {
                    minAge = maxAge;
                    seekBarMinAge.setProgress(minAge - 18);
                    tvMinAge.setText(minAge + " years");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setDistanceSpinner(int distance) {
        int position = 0;
        switch (distance) {
            case 5: position = 0; break;
            case 10: position = 1; break;
            case 25: position = 2; break;
            case 50: position = 3; break;
            case 100: position = 4; break;
        }
        spinnerDistance.setSelection(position);
    }

    private void saveSettings() {
        // Get gender preference
        int selectedGenderId = rgGenderPreference.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String selectedText = selectedGender.getText().toString();

        // Convert UI text to database values
        if (selectedText.equals("Men")) {
            genderPreference = "Male";
        } else if (selectedText.equals("Women")) {
            genderPreference = "Female";
        } else {
            genderPreference = "Everyone";
        }

        // Get distance from spinner
        String distanceStr = spinnerDistance.getSelectedItem().toString();
        distanceFilter = Integer.parseInt(distanceStr.replace(" km", ""));

        // Save to Firebase with completion listener
        settingsRef.child("minAge").setValue(minAge);
        settingsRef.child("maxAge").setValue(maxAge);
        settingsRef.child("genderPreference").setValue(genderPreference);
        settingsRef.child("distanceFilter").setValue(distanceFilter);
        settingsRef.child("invisibleMode").setValue(invisibleMode)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Settings saved successfully! ✅", Toast.LENGTH_SHORT).show();
                    // Close activity to go back and trigger reload
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String currentPassword = etCurrentPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    changePassword(currentPassword, newPassword);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate user
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Change password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    Toast.makeText(this, "Password changed successfully! 🔒", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("⚠️ Are you sure you want to delete your account?\n\nThis action cannot be undone and will:\n" +
                        "• Delete all your data\n" +
                        "• Remove all matches\n" +
                        "• Delete all messages\n" +
                        "• Remove your profile permanently")
                .setPositiveButton("Delete", (dialog, which) -> confirmDeleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteAccount() {
        // Ask for password confirmation
        EditText etPassword = new EditText(this);
        etPassword.setHint("Enter your password");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Enter your password to confirm account deletion:")
                .setView(etPassword)
                .setPositiveButton("Confirm Delete", (dialog, which) -> {
                    String password = etPassword.getText().toString().trim();
                    if (!TextUtils.isEmpty(password)) {
                        deleteAccount(password);
                    } else {
                        Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // Delete user data from database
                        userRef.removeValue()
                                .addOnSuccessListener(aVoid1 -> {
                                    // Delete authentication account
                                    user.delete()
                                            .addOnSuccessListener(aVoid2 -> {
                                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                // Navigate to login
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}