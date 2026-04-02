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

public class UpcomingTasksAdapter extends GenericAdapter<CareTask> {

    public interface Listener {
        void onEdit(CareTask task);
        void onMarkDone(CareTask task);
        void onSkip(CareTask task);
    }

    private Listener listener;
    private String busyTaskId = null;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public UpcomingTasksAdapter(List<CareTask> items) {
        super(
                items,
                R.layout.item_upcoming_task,
                holder -> {
                    holder.putView("tvTaskType", holder.itemView.findViewById(R.id.tvTaskType));
                    holder.putView("tvDueDate", holder.itemView.findViewById(R.id.tvDueDate));
                    holder.putView("btnEdit", holder.itemView.findViewById(R.id.btnEdit));
                    holder.putView("btnMarkDone", holder.itemView.findViewById(R.id.btnMarkDone));
                    holder.putView("btnSkip", holder.itemView.findViewById(R.id.btnSkip));
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

        TextView tvTaskType = holder.getView("tvTaskType");
        TextView tvDueDate = holder.getView("tvDueDate");

        MaterialButton btnEdit = holder.getView("btnEdit");
        MaterialButton btnMarkDone = holder.getView("btnMarkDone");
        MaterialButton btnSkip = holder.getView("btnSkip");

        tvTaskType.setText(formatTaskType(item != null ? item.getType() : null));
        tvDueDate.setText(formatDueDate(item));

        boolean isBusy = item != null
                && item.getIdFs() != null
                && item.getIdFs().equals(busyTaskId);

        btnEdit.setEnabled(!isBusy);
        btnMarkDone.setEnabled(!isBusy);
        btnSkip.setEnabled(!isBusy);

        float alpha = isBusy ? 0.55f : 1f;
        btnEdit.setAlpha(alpha);
        btnMarkDone.setAlpha(alpha);
        btnSkip.setAlpha(alpha);

        btnEdit.setOnClickListener(v -> {
            if (listener != null && item != null && !isBusy) {
                listener.onEdit(item);
            }
        });

        btnMarkDone.setOnClickListener(v -> {
            if (listener != null && item != null && !isBusy) {
                listener.onMarkDone(item);
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (listener != null && item != null && !isBusy) {
                listener.onSkip(item);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setBusyTaskId(String busyTaskId) {
        this.busyTaskId = busyTaskId;
        notifyDataSetChanged();
    }

    private String formatDueDate(CareTask task) {
        if (task == null || task.getNextDueAt() == null) {
            return "-";
        }
        return dateFormat.format(task.getNextDueAt().toDate());
    }

    private static String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
