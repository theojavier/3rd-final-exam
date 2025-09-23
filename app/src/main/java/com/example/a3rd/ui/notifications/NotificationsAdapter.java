package com.example.a3rd.ui.notifications;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a3rd.R;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotifViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem item);
    }

    private final List<NotificationItem> notificationList;
    private final OnNotificationClickListener listener;

    public NotificationsAdapter(List<NotificationItem> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationItem item = notificationList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notif_title);
            message = itemView.findViewById(R.id.notif_message);
        }

        public void bind(NotificationItem item, OnNotificationClickListener listener) {
            title.setText(item.getTitle());
            message.setText(item.getMessage());

            // Bold if not viewed
            title.setTypeface(null, item.isViewed() ? Typeface.NORMAL : Typeface.BOLD);

            itemView.setOnClickListener(v -> listener.onNotificationClick(item));
        }
    }
}

