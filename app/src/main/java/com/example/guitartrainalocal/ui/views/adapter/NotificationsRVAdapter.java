package com.example.guitartrainalocal.ui.views.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.guitartrainalocal.R;
import com.example.guitartrainalocal.activities.exercises.ExercisesActivity;
import com.example.guitartrainalocal.services.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationsRVAdapter extends RecyclerView.Adapter<NotificationsRVAdapter.NotifViewHolder> {

    private View.OnClickListener onDeleteTuningClickListener;

    public void setOnDeleteTuningClickListener(View.OnClickListener onClickListener) {
        this.onDeleteTuningClickListener = onClickListener;
    }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView tvTitle;
        final TextView tvBody;

        NotifViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.item);
            tvTitle = itemView.findViewById(R.id.tuning_title);
            tvBody = itemView.findViewById(R.id.tuning_notes);
        }
    }

    public int getItem() {
        return item;
    }

    private final List<Notification> notificationList;
    private int item;
    private final Context context;

    public NotificationsRVAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        NotifViewHolder notifViewHolder = new NotifViewHolder(view);
        ImageButton btnDelete = notifViewHolder.itemView.findViewById(R.id.tuning_delete);
        btnDelete.setVisibility(View.VISIBLE);
        btnDelete.setOnClickListener(view12 -> {
            if (onDeleteTuningClickListener == null) {
                return;
            }
            int adapterPos = notifViewHolder.getAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                item = adapterPos;
                onDeleteTuningClickListener.onClick(view);
            }
        });
        return notifViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        String pattern = "dd/MM/yyyy hh:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        String date = simpleDateFormat.format(notification.getDate());
        switch (notification.getType()) {
            case 1: {
                holder.tvTitle.setText(R.string.posture_reminder);
                holder.tvBody.setText(String.format("%s %s", context.getString(R.string.notification_posture_reminder_desc), date));
                holder.itemView.setOnClickListener(v -> dialogBuilder().show());
                break;
            }
            case 2: {
                holder.tvTitle.setText(R.string.practice_reminder);
                holder.tvBody.setText(String.format("%s %s", context.getString(R.string.notification_practice_reminder_desc), date));
                holder.itemView.setOnClickListener(v -> {
                    context.startActivity(new Intent(context, ExercisesActivity.class));
                    if (context instanceof Activity) {
                        ((Activity) context).finishAfterTransition();
                    }
                });
                break;
            }
        }
    }

    private AlertDialog dialogBuilder() {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.posture)
                .setMessage(R.string.posture_dialog_info)
                .setPositiveButton("OK", null)
                .setOnCancelListener(DialogInterface::cancel)
                .create();
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}