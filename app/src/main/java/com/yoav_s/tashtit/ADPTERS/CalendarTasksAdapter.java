package com.yoav_s.tashtit.ADPTERS;

import androidx.annotation.NonNull;

import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.CareTask;
import com.yoav_s.tashtit.R;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarTasksAdapter extends GenericAdapter<CareTask> {

    public interface Listener {
        void onEdit(CareTask task);
        void onMarkDone(CareTask task);
        void onSkip(CareTask task);
    }

    private Listener listener;
    private String busyTaskId = null;
    private Map<String, String> plantNicknameByPlantId = new HashMap<>();

    private final SimpleDateFormat dueDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public CalendarTasksAdapter(List<CareTask> items) {
        super(
                items,
                R.layout.item_calendar_task,
                holder -> {
                    holder.putView("tvTaskType", holder.itemView.findViewById(R.id.tvTaskType));
                    holder.putView("tvPlantNickname", holder.itemView.findViewById(R.id.tvPlantNickname));
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
        TextView tvPlantNickname = holder.getView("tvPlantNickname");

        MaterialButton btnEdit = holder.getView("btnEdit");
        MaterialButton btnMarkDone = holder.getView("btnMarkDone");
        MaterialButton btnSkip = holder.getView("btnSkip");

        tvTaskType.setText(formatTaskTitle(item));
        tvPlantNickname.setText(resolvePlantNickname(item));

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

    public void setPlantNicknameByPlantId(Map<String, String> plantNicknameByPlantId) {
        this.plantNicknameByPlantId = plantNicknameByPlantId != null
                ? plantNicknameByPlantId
                : new HashMap<>();
        notifyDataSetChanged();
    }

    private String formatTaskTitle(CareTask task) {
        if (task == null) return "-";

        String type = formatTaskType(task.getType());
        String due = formatDueDate(task);

        if ("-".equals(due)) {
            return type;
        }

        return type + " - " + due;
    }

    private String formatDueDate(CareTask task) {
        if (task == null || task.getNextDueAt() == null) {
            return "-";
        }

        return dueDateFormat.format(task.getNextDueAt().toDate());
    }

    private String resolvePlantNickname(CareTask task) {
        if (task == null || task.getPlantId() == null) {
            return "-";
        }

        String nickname = plantNicknameByPlantId.get(task.getPlantId());
        return safeText(nickname);
    }

    private static String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }
}
