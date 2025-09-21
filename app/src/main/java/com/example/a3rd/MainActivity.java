package com.example.a3rd;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.a3rd.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private String profileImageUrl;

    // Header views (cached)
    private ImageView headerImage;
    private TextView headerName, headerSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Top-level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_myschedule,
                R.id.nav_exam_item_page,
                R.id.nav_profile,
                R.id.nav_exam_history,
                R.id.takeExamFragment,
                R.id.examResultFragment,
                R.id.examFragment
        )
                .setOpenableLayout(drawer)
                .build();
        final NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Logo click -> home
        ImageView logo = findViewById(R.id.toolbar_logo);
        if (logo != null) {
            logo.setOnClickListener(v -> {
                navController.popBackStack(R.id.nav_home, false);
                navController.navigate(R.id.nav_home);
            });
        }

        // Hide toolbar & drawer for login/forgot fragments
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            if (id == R.id.nav_login || id == R.id.nav_forgot) {
                binding.appBarMain.toolbar.setVisibility(View.GONE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                binding.appBarMain.toolbar.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

        // Ensure nav header exists (some setups don't inflate it automatically)
        if (navigationView.getHeaderCount() == 0) {
            navigationView.inflateHeaderView(R.layout.nav_header_main);
        }

        // Get header view safely
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.e(TAG, "Navigation header view is null. Check nav_view header layout.");
            return;
        }

        // Header child views
        LinearLayout headerTop = headerView.findViewById(R.id.nav_header_top);
        ImageView arrow = headerView.findViewById(R.id.nav_header_arrow);
        LinearLayout dropdown = headerView.findViewById(R.id.nav_header_dropdown);
        TextView myProfile = headerView.findViewById(R.id.nav_my_profile);
        TextView history = headerView.findViewById(R.id.nav_history);

        headerImage = headerView.findViewById(R.id.imageView);
        headerName = headerView.findViewById(R.id.nav_header_name);
        headerSection = headerView.findViewById(R.id.nav_header_section);

        // Dropdown toggle
        final boolean[] expanded = {false};
        headerTop.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            dropdown.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            arrow.animate().rotation(expanded[0] ? 180f : 0f).setDuration(200).start();
        });

        myProfile.setOnClickListener(v -> navController.navigate(R.id.nav_profile));
        history.setOnClickListener(v -> navController.navigate(R.id.nav_exam_history));
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_exam_item_page) {
                navController.navigate(R.id.nav_exam_item_page);
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.nav_home) {
                navController.navigate(R.id.nav_home);
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.nav_myschedule) {
                navController.navigate(R.id.nav_myschedule);
                drawer.closeDrawers();
                return true;
            }

            // fallback → let NavigationUI handle other clicks
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });

        // initial load (single fetch)
        loadUserProfile();
    }

    /**
     * Single Firestore fetch to populate the header. Public so other fragments (Login) can call it
     * immediately after saving userId to SharedPreferences.
     */
    public void loadUserProfile() {
        Log.d(TAG, "loadUserProfile() called");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);
        Log.d(TAG, "userId from prefs = " + userId);

        // Clear header immediately so old values don't flash
        resetHeader();

        if (userId == null) {
            Log.d(TAG, "no userId saved; header left cleared");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String program = doc.getString("program");
                        String yearBlock = doc.getString("yearBlock");
                        String semester = doc.getString("semester");
                        profileImageUrl = doc.getString("profileImage");

                        // update header text
                        headerName.setText(name != null ? name : "No Name");

                        StringBuilder sectionText = new StringBuilder();
                        if (program != null) sectionText.append(program).append(" ");
                        if (yearBlock != null) sectionText.append(yearBlock).append(" ");
                        if (semester != null) sectionText.append("(").append(semester).append(")");
                        String section = sectionText.toString().trim();
                        headerSection.setText(!section.isEmpty() ? section : "No Section");

                        // handle Imgur page links => i.imgur.com direct + .jpg
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            if (profileImageUrl.contains("imgur.com") && !profileImageUrl.contains("i.imgur.com")) {
                                profileImageUrl = profileImageUrl.replace("imgur.com", "i.imgur.com") + ".jpg";
                            }

                            Glide.with(this)
                                    .load(profileImageUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_person)
                                    .error(R.drawable.ic_person)
                                    .into(headerImage);
                        } else {
                            headerImage.setImageResource(R.drawable.ic_person);
                        }
                    } else {
                        Log.w(TAG, "User doc not found for id: " + userId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user profile", e);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetHeader() {
        // Clear text and image right away
        if (headerName != null) headerName.setText("No Name");
        if (headerSection != null) headerSection.setText("No Section");
        if (headerImage != null) {
            // clear any active Glide request and set placeholder
            try {
                Glide.with(this).clear(headerImage);
            } catch (Exception ignored) { }
            headerImage.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Still refresh if activity is recreated
        loadUserProfile();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Notifications Badge
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        notificationItem.setActionView(R.layout.notification_badge);

        View actionView = notificationItem.getActionView();
        TextView badgeTextView = actionView.findViewById(R.id.notification_badge);

        int notificationCount = 0;
        if (notificationCount > 0) {
            badgeTextView.setText(String.valueOf(notificationCount));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

        return true;
    }

    // show icons in overflow
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null && menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
            try {
                Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(menu, true);
            } catch (Exception e) {
                Log.w(TAG, "Failed to show icons in overflow", e);
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notifications) {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // ❌ Do NOT remove userId (next login will overwrite it)
        editor.remove("authToken");
        editor.putBoolean("isLoggedIn", false);

        editor.apply();

        resetHeader();

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.popBackStack(R.id.nav_home, true);
        navController.navigate(R.id.nav_login);
    }

}
