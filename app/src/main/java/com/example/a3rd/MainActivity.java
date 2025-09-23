package com.example.a3rd;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.a3rd.databinding.ActivityMainBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private DrawerLayout drawer;
    private String profileImageUrl;

    // Firestore
    private FirebaseFirestore db;
    private ListenerRegistration notificationListener;

    // Header views
    private ImageView headerImage;
    private TextView headerName, headerSection;

    // Badge view
    private TextView badgeTextView;

    // For building dropdown items
    private final List<NotificationEntry> notificationEntries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(binding.appBarMain.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = binding.drawerLayout;
        androidx.appcompat.widget.Toolbar toolbar = binding.appBarMain.toolbar;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

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

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

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

        // Ensure nav header exists
        if (binding.navView.getHeaderCount() == 0) {
            binding.navView.inflateHeaderView(R.layout.nav_header_main);
        }

        // Get header view safely
        View headerView = binding.navView.getHeaderView(0);
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

        // Dropdown toggle (profile)
        final boolean[] expanded = {false};
        headerTop.setOnClickListener(v -> {
            expanded[0] = !expanded[0];
            dropdown.setVisibility(expanded[0] ? View.VISIBLE : View.GONE);
            arrow.animate().rotation(expanded[0] ? 180f : 0f).setDuration(200).start();
        });

        myProfile.setOnClickListener(v -> navController.navigate(R.id.nav_profile));
        history.setOnClickListener(v -> navController.navigate(R.id.nav_exam_history));
        binding.navView.setNavigationItemSelectedListener(item -> {
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
            } else if (id == R.id.nav_exam_history) {
                navController.navigate(R.id.nav_exam_history);
                drawer.closeDrawers();
            } else if (id == R.id.nav_profile) {
                navController.navigate(R.id.nav_profile);
                drawer.closeDrawers();
            }

            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        });

        // Initial header load
        loadUserProfile();

        // If already logged in, attach notification listener (use userId stored in prefs)
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userDocId = prefs.getString("userId", null);
        if (userDocId != null) {
            Log.d(TAG, "userDocId found on start, ensuring notifications and starting listener for " + userDocId);
            // Ensure notification docs exist (asynchronously), then start listener immediately (listener will pick up any created docs)
            ensureUserNotificationsCollection(userDocId, null);
            startNotificationListener(userDocId);
        } else {
            Log.d(TAG, "no userId on start; notifications listener not started");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Notifications Badge
        MenuItem notificationItem = menu.findItem(R.id.action_notifications);
        notificationItem.setActionView(R.layout.notification_badge);

        View actionView = notificationItem.getActionView();
        badgeTextView = actionView.findViewById(R.id.notification_badge);

        // Start with hidden badge
        badgeTextView.setVisibility(View.GONE);

        // Click bell -> ensure per-user notification docs exist, then show dropdown of notifications
        actionView.setOnClickListener(v -> fetchNotificationsAndShowDropdown());

        return true;
    }

    /**
     * Start listening for unseen notifications (viewed == false) for a given userDocId.
     */
    public void startNotificationListener(@NonNull String userDocId) {
        if (notificationListener != null) {
            notificationListener.remove();
        }

        Log.d(TAG, "startNotificationListener -> " + userDocId);

        notificationListener = db.collection("users")
                .document(userDocId)
                .collection("notifications")
                .whereEqualTo("viewed", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "notificationListener failed", e);
                        return;
                    }

                    int unseen = 0;
                    if (snapshots != null) {
                        unseen = snapshots.size();
                    }
                    Log.d(TAG, "notificationListener unseen count = " + unseen);
                    updateBadge(unseen);
                });
    }

    private void updateBadge(int count) {
        if (badgeTextView != null) {
            if (count > 0) {
                badgeTextView.setVisibility(View.VISIBLE);
                badgeTextView.setText(String.valueOf(count));
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Ensure the user has notification documents for recent exams that match their program/yearBlock.
     * If onComplete != null it will run after processing (success or failure).
     */
    public void ensureUserNotificationsCollection(@NonNull String userDocId, Runnable onComplete) {
        db.collection("users").document(userDocId).get()
                .addOnSuccessListener(userDoc -> {
                    if (!userDoc.exists()) {
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    String program = userDoc.getString("program");
                    String yearBlock = userDoc.getString("yearBlock");
                    if (program == null || yearBlock == null) {
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    db.collection("exams")
                            .whereEqualTo("program", program)
                            .whereEqualTo("yearBlock", yearBlock)
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .limit(5)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot == null || snapshot.isEmpty()) {
                                    if (onComplete != null) onComplete.run();
                                    return;
                                }

                                final int total = snapshot.size();
                                final int[] processed = {0};

                                for (QueryDocumentSnapshot examDoc : snapshot) {
                                    String examId = examDoc.getId();
                                    String subject = examDoc.getString("subject");
                                    Timestamp createdAt = examDoc.getTimestamp("createdAt");

                                    DocumentReference notifRef = db.collection("users")
                                            .document(userDocId)
                                            .collection("notifications")
                                            .document(examId);

                                    // If the notification doc already exists do nothing, otherwise create with viewed=false and copy subject/createdAt
                                    notifRef.get().addOnSuccessListener(notifDoc -> {
                                        if (!notifDoc.exists()) {
                                            Map<String, Object> data = new HashMap<>();
                                            data.put("viewed", false);
                                            if (subject != null) data.put("subject", subject);
                                            if (createdAt != null) data.put("createdAt", createdAt);
                                            notifRef.set(data)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Log.d(TAG, "Created notif for user " + userDocId + " / exam " + examId);
                                                        processed[0]++;
                                                        if (processed[0] == total && onComplete != null) onComplete.run();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e(TAG, "Failed to create notif for exam " + examId, e);
                                                        processed[0]++;
                                                        if (processed[0] == total && onComplete != null) onComplete.run();
                                                    });
                                        } else {
                                            // If it exists we might want to ensure it has subject/createdAt; only update missing fields
                                            Map<String, Object> updates = new HashMap<>();
                                            boolean needUpdate = false;
                                            if (notifDoc.get("subject") == null && subject != null) {
                                                updates.put("subject", subject);
                                                needUpdate = true;
                                            }
                                            if (notifDoc.get("createdAt") == null && createdAt != null) {
                                                updates.put("createdAt", createdAt);
                                                needUpdate = true;
                                            }
                                            if (needUpdate) {
                                                notifRef.update(updates)
                                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated notif meta for " + examId))
                                                        .addOnFailureListener(e -> Log.w(TAG, "Failed to update notif meta", e));
                                            }
                                            processed[0]++;
                                            if (processed[0] == total && onComplete != null) onComplete.run();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed reading notif doc for exam " + examId, e);
                                        processed[0]++;
                                        if (processed[0] == total && onComplete != null) onComplete.run();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed fetching exams for ensureUserNotificationsCollection", e);
                                if (onComplete != null) onComplete.run();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed fetching user doc for ensureUserNotificationsCollection", e);
                    if (onComplete != null) onComplete.run();
                });
    }

    /**
     * Called by the bell click handler. Ensures notification docs are present and then shows a dropdown
     * that lists the latest notifications (subject + time). Clicking an item opens the exam (and marks viewed).
     */
    private void fetchNotificationsAndShowDropdown() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userDocId = prefs.getString("userId", null);
        if (userDocId == null) {
            Toast.makeText(this, "Please login to see notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make sure notifications exist for recent exams, then show dropdown when done
        ensureUserNotificationsCollection(userDocId, () -> showNotificationsDropdown(userDocId));
    }

    private void showNotificationsDropdown(@NonNull String userDocId) {
        db.collection("users")
                .document(userDocId)
                .collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        Toast.makeText(this, "No notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<DocumentSnapshot> notifDocs = snapshot.getDocuments();
                    String[] items = new String[notifDocs.size()];
                    final String[] examIds = new String[notifDocs.size()];

                    for (int i = 0; i < notifDocs.size(); i++) {
                        DocumentSnapshot n = notifDocs.get(i);
                        String examId = n.getId();
                        examIds[i] = examId;

                        String subject = n.getString("subject");
                        Timestamp createdAt = n.getTimestamp("createdAt");
                        String time = (createdAt != null) ? DateFormat.format("MMM d, yyyy h:mm a", createdAt.toDate()).toString() : "N/A";

                        items[i] = (subject != null ? subject : "Exam") + " (" + time + ")";
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Notifications")
                            .setItems(items, (dialog, which) -> {
                                String examId = examIds[which];
                                openExamFromNotificationWithUser(examId, userDocId);
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load notifications for dropdown", e);
                    Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Mark notification as viewed inside users/{userDocId}/notifications/{examId} and navigate to exam.
     */
    public void openExamFromNotificationWithUser(String examId, @NonNull String userDocId) {
        Log.d(TAG, "openExamFromNotificationWithUser -> marking viewed and navigating. examId=" + examId);

        DocumentReference notifRef = db.collection("users")
                .document(userDocId)
                .collection("notifications")
                .document(examId);

        Map<String, Object> viewedMap = new HashMap<>();
        viewedMap.put("viewed", true);

        notifRef.set(viewedMap)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Marked notification viewed for exam " + examId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to mark notification viewed", e));

        // then navigate to exam details (reuse your existing logic)
        db.collection("exams").document(examId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Log.w(TAG, "Exam document not found: " + examId);
                        Toast.makeText(this, "Exam not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long startMillis = -1;
                    long endMillis = -1;
                    if (doc.getTimestamp("startTime") != null) {
                        startMillis = doc.getTimestamp("startTime").toDate().getTime();
                    }
                    if (doc.getTimestamp("endTime") != null) {
                        endMillis = doc.getTimestamp("endTime").toDate().getTime();
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString("examId", examId);
                    bundle.putLong("startTime", startMillis);
                    bundle.putLong("endTime", endMillis);

                    NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                    navController.navigate(R.id.takeExamFragment, bundle);

                    // restart listener (it listens to viewed==false so it will update badge)
                    startNotificationListener(userDocId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch exam doc for " + examId, e);
                    Toast.makeText(this, "Failed to open exam", Toast.LENGTH_SHORT).show();
                });
    }

    // Convenience wrapper called elsewhere in your code (keeps older method names but uses userDocId)
    public void openExamFromNotification(String examId) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userDocId = prefs.getString("userId", null);
        if (userDocId == null) {
            Toast.makeText(this, "Please login to open notification", Toast.LENGTH_SHORT).show();
            return;
        }
        openExamFromNotificationWithUser(examId, userDocId);
    }

    public void loadUserProfile() {
        Log.d(TAG, "loadUserProfile() called");

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userDocId = prefs.getString("userId", null);

        resetHeader();

        if (userDocId == null) {
            Log.d(TAG, "no userId saved; header left cleared");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userDocId);

        userRef.get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String program = doc.getString("program");
                        String yearBlock = doc.getString("yearBlock");
                        String semester = doc.getString("semester");
                        profileImageUrl = doc.getString("profileImage");

                        headerName.setText(name != null ? name : "No Name");

                        StringBuilder sectionText = new StringBuilder();
                        if (program != null) sectionText.append(program).append(" ");
                        if (yearBlock != null) sectionText.append(yearBlock).append(" ");
                        if (semester != null) sectionText.append("(").append(semester).append(")");
                        String section = sectionText.toString().trim();
                        headerSection.setText(!section.isEmpty() ? section : "No Section");

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
                        Log.w(TAG, "User doc not found for id: " + userDocId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load user profile", e);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void resetHeader() {
        if (headerName != null) headerName.setText("No Name");
        if (headerSection != null) headerSection.setText("No Section");
        if (headerImage != null) {
            try {
                Glide.with(this).clear(headerImage);
            } catch (Exception ignored) {
            }
            headerImage.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

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

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("authToken");
        editor.putBoolean("isLoggedIn", false);
        editor.remove("userId");
        editor.remove("studentId");
        editor.apply();

        resetHeader();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.popBackStack(R.id.nav_home, true);
        navController.navigate(R.id.nav_login);

        // Stop listening to notifications
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    // Simple holder for building dropdown items
    private static class NotificationEntry {
        final String examId;
        final String subject;
        final Timestamp createdAt;

        NotificationEntry(String examId, String subject, Timestamp createdAt) {
            this.examId = examId;
            this.subject = subject;
            this.createdAt = createdAt;
        }
    }
    public void ensureUserNotificationsCollectionMinimal(@NonNull String userDocId, Runnable onComplete) {
        DocumentReference userRef = db.collection("users").document(userDocId);

        // Firestore doesn't allow empty collections, so we just check if user exists
        userRef.get()
                .addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        Log.d("ensureNotif", "User exists, ready to create notifications when needed: " + userDocId);
                    } else {
                        Log.w("ensureNotif", "User document does not exist: " + userDocId);
                    }

                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("ensureNotif", "Failed to fetch user", e);
                    if (onComplete != null) onComplete.run();
                });
    }

}
