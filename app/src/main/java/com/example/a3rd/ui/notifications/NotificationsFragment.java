package com.example.a3rd.ui.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.a3rd.MainActivity;
import com.example.a3rd.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment implements NotificationsAdapter.OnNotificationClickListener {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<NotificationItem> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;

    public NotificationsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_notifications, container, false);

        recyclerView = view.findViewById(R.id.rv_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationsAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("userId", null);

        if (currentUserId != null) {
            loadNotifications();
        }

        return view;
    }

    private void loadNotifications() {
        CollectionReference notifRef = db.collection("users")
                .document(currentUserId)
                .collection("notifications");

        notifRef.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }
            notificationList.clear();
            if (snapshots != null) {
                for (QueryDocumentSnapshot doc : snapshots) {
                    String examId = doc.getId();

                    // ✅ Null-safe defaults here
                    String title = doc.getString("title") != null ? doc.getString("title") : "New Exam";
                    String message = doc.getString("message") != null ? doc.getString("message") : "";
                    boolean viewed = doc.getBoolean("viewed") != null && doc.getBoolean("viewed");

                    notificationList.add(new NotificationItem(examId, title, message, viewed));
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onNotificationClick(NotificationItem item) {
        // Delegate to MainActivity → marks viewed & opens TakeExamFragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openExamFromNotification(item.getExamId());
        }
    }
}

