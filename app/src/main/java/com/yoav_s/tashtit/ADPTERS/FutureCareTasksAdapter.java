package com.yoav_s.tashtit.ADPTERS;

import androidx.annotation.NonNull;

import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.CareTask;
import com.yoav_s.tashtit.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FutureCareTasksAdapter extends GenericAdapter<CareTask> {

    public interface Listener {
        void onEdit(CareTask task);
    }

    private Listener listener;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public FutureCareTasksAdapter(List<CareTask> items) {
        super(
                items,
                R.layout.item_future_care_task,
                holder -> {
                    holder.putView("tvFutureTaskType", holder.itemView.findViewById(R.id.tvFutureTaskType));
                    holder.putView("tvFutureTaskEveryDays", holder.itemView.findViewById(R.id.tvFutureTaskEveryDays));
                    holder.putView("tvFutureTaskDue", holder.itemView.findViewById(R.id.tvFutureTaskDue));
                    holder.putView("btnEditFutureTask", holder.itemView.findViewById(R.id.btnEditFutureTask));
                },
                (holder, item, position) -> {
                    // real binding is below
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        CareTask item = getItem(position);

        TextView tvFutureTaskType = holder.getView("tvFutureTaskType");
        TextView tvFutureTaskEveryDays = holder.getView("tvFutureTaskEveryDays");
        TextView tvFutureTaskDue = holder.getView("tvFutureTaskDue");
        MaterialButton btnEditFutureTask = holder.getView("btnEditFutureTask");

        tvFutureTaskType.setText(formatTaskType(item != null ? item.getType() : null));
        tvFutureTaskEveryDays.setText(formatEveryDays(item != null ? item.getEveryDays() : 0));
        tvFutureTaskDue.setText(formatDueDate(item));

        btnEditFutureTask.setOnClickListener(v -> {
            if (listener != null && item != null) {
                listener.onEdit(item);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private String formatDueDate(CareTask task) {
        if (task == null || task.getNextDueAt() == null) {
            return "-";
        }
        return dateFormat.format(task.getNextDueAt().toDate());
    }

    private static String formatEveryDays(int everyDays) {
        if (everyDays <= 0) {
            return "Every - days";
        }
        return "Every " + everyDays + " days";
    }

    private static String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
