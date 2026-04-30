package com.yoav_s.tashtit.ADPTERS;

import androidx.annotation.NonNull;

import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.CareTask;
import com.yoav_s.model.Plant;
import com.yoav_s.tashtit.R;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyPlantsAdapter extends GenericAdapter<Plant> {

    public interface Listener {
        void onOpen(Plant plant);
        void onMarkDone(Plant plant);
        void onSkip(Plant plant);
    }

    private Listener listener;

    private String hiddenNextTaskPlantId = null;

    private Map<String, String> speciesNameByPlantId = new HashMap<>();
    private Map<String, CareTask> nextTaskByPlantId = new HashMap<>();

    public MyPlantsAdapter(List<Plant> items) {
        super(
                items,
                R.layout.item_my_plant,
                holder -> {
                    holder.putView("tvNickname", holder.itemView.findViewById(R.id.tvNickname));
                    holder.putView("tvLocation", holder.itemView.findViewById(R.id.tvLocation));
                    holder.putView("tvSpecies", holder.itemView.findViewById(R.id.tvSpecies));
                    holder.putView("tvNextTask", holder.itemView.findViewById(R.id.tvNextTask));
                    holder.putView("btnOpen", holder.itemView.findViewById(R.id.btnOpen));
                    holder.putView("btnMarkDone", holder.itemView.findViewById(R.id.btnMarkDone));
                    holder.putView("btnSkip", holder.itemView.findViewById(R.id.btnSkip));
                },
                (holder, item, position) -> {
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Plant item = getItem(position);

        TextView tvNickname = holder.getView("tvNickname");
        TextView tvLocation = holder.getView("tvLocation");
        TextView tvSpecies = holder.getView("tvSpecies");
        TextView tvNextTask = holder.getView("tvNextTask");
        MaterialButton btnOpen = holder.getView("btnOpen");
        MaterialButton btnMarkDone = holder.getView("btnMarkDone");
        MaterialButton btnSkip = holder.getView("btnSkip");

        String plantId = item != null ? item.getIdFs() : null;
        String speciesName = "-";
        CareTask nextTask = null;

        if (plantId != null) {
            speciesName = safeText(speciesNameByPlantId.get(plantId));
            nextTask = nextTaskByPlantId.get(plantId);
        }

        if (plantId != null && plantId.equals(hiddenNextTaskPlantId)) {
            nextTask = null;
        }

        tvNickname.setText("nickname: " + safeText(item != null ? item.getNickname() : null));
        tvLocation.setText("location: " + safeText(item != null ? item.getLocation() : null));
        tvSpecies.setText("species: " + speciesName);
        tvNextTask.setText("next: " + (nextTask != null ? formatTaskType(nextTask.getType()) : "-"));

        boolean hasNextTask = nextTask != null;

        btnMarkDone.setEnabled(hasNextTask);
        btnSkip.setEnabled(hasNextTask);

        btnMarkDone.setAlpha(hasNextTask ? 1f : 0.55f);
        btnSkip.setAlpha(hasNextTask ? 1f : 0.55f);

        btnOpen.setOnClickListener(v -> {
            if (listener != null && item != null) {
                listener.onOpen(item);
            }
        });

        btnMarkDone.setOnClickListener(v -> {
            if (listener != null && item != null && hasNextTask) {
                listener.onMarkDone(item);
            }
        });

        btnSkip.setOnClickListener(v -> {
            if (listener != null && item != null && hasNextTask) {
                listener.onSkip(item);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setSpeciesNameByPlantId(Map<String, String> speciesNameByPlantId) {
        this.speciesNameByPlantId = speciesNameByPlantId != null ? speciesNameByPlantId : new HashMap<>();
        notifyDataSetChanged();
    }

    public void setNextTaskByPlantId(Map<String, CareTask> nextTaskByPlantId) {
        this.nextTaskByPlantId = nextTaskByPlantId != null ? nextTaskByPlantId : new HashMap<>();
        notifyDataSetChanged();
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }

    private static String formatTaskType(CareTask.Type type) {
        if (type == null) return "-";

        String value = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
    public void setHiddenNextTaskPlantId(String hiddenNextTaskPlantId) {
        this.hiddenNextTaskPlantId = hiddenNextTaskPlantId;
        notifyDataSetChanged();
    }
}
