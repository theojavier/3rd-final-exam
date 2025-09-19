package com.example.a3rd;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private String profileImageUrl; // still used for nav header

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
                R.id.nav_home, R.id.nav_myschedule, R.id.nav_exam_item_page)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // 🔹 Click logo → navigate Home
        ImageView logo = findViewById(R.id.toolbar_logo);
        logo.setOnClickListener(v -> {
            navController.popBackStack(R.id.nav_home, false);
            navController.navigate(R.id.nav_home);
        });

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

        // 🔽 Get Header View (first header of navView)
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerTop = headerView.findViewById(R.id.nav_header_top);
        ImageView arrow = headerView.findViewById(R.id.nav_header_arrow);
        LinearLayout dropdown = headerView.findViewById(R.id.nav_header_dropdown);
        TextView myProfile = headerView.findViewById(R.id.nav_my_profile);
        TextView history = headerView.findViewById(R.id.nav_history);

        // 🔽 Profile info
        ImageView headerImage = headerView.findViewById(R.id.imageView);
        TextView headerName = headerView.findViewById(R.id.nav_header_name);
        TextView headerSection = headerView.findViewById(R.id.nav_header_section);

        // 🔽 Dropdown toggle logic
        final boolean[] expanded = {false};
        headerTop.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            dropdown.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            arrow.animate().rotation(expanded[0] ? 180f : 0f).setDuration(200).start();
        });

        myProfile.setOnClickListener(v -> {
            navController.navigate(R.id.nav_profile);
        });

        history.setOnClickListener(v -> {
            navController.navigate(R.id.nav_exam_history);
        });

        // 🔽 Load user data from Firestore (like ProfileFragment)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(userId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String program = doc.getString("program");
                        String yearBlock = doc.getString("yearBlock");
                        String semester = doc.getString("semester");
                        profileImageUrl = doc.getString("profileImage");

                        headerName.setText(name != null ? name : "No Name");

                        // Combine program + yearBlock + semester into section
                        String sectionText = "";
                        if (program != null) sectionText += program + " ";
                        if (yearBlock != null) sectionText += yearBlock + " ";
                        if (semester != null) sectionText += "(" + semester + ")";
                        headerSection.setText(!sectionText.trim().isEmpty() ? sectionText : "No Section");

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
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // 🔹 Notifications Badge
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        notificationItem.setActionView(R.layout.notification_badge);

        View actionView = notificationItem.getActionView();
        TextView badgeTextView = actionView.findViewById(R.id.notification_badge);

        int notificationCount = 0; // Replace with your actual count
        if (notificationCount > 0) {
            badgeTextView.setText(String.valueOf(notificationCount));
            badgeTextView.setVisibility(View.VISIBLE);
        } else {
            badgeTextView.setVisibility(View.GONE);
        }

        return true;
    }

    // 🔹 Force icons to show in overflow menu
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_notifications) {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_logout) {
            finishAffinity();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}