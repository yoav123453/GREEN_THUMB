package com.yoav_s.tashtit.ADPTERS;

import androidx.annotation.NonNull;

import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.Guide;
import com.yoav_s.tashtit.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuidesAdapter extends GenericAdapter<Guide> {

    public interface Listener {
        void onOpen(Guide guide);
        void onItemClicked(Guide guide);
    }

    private Listener listener;
    private Map<String, String> authorNameByUserId = new HashMap<>();

    public GuidesAdapter(List<Guide> items) {
        super(
                items,
                R.layout.item_guide,
                holder -> {
                    holder.putView("tvGuideTitle", holder.itemView.findViewById(R.id.tvGuideTitle));
                    holder.putView("tvGuideAuthor", holder.itemView.findViewById(R.id.tvGuideAuthor));
                    holder.putView("btnOpenGuide", holder.itemView.findViewById(R.id.btnOpenGuide));
                },
                (holder, item, position) -> {
                    // real binding below
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        Guide item = getItem(position);

        TextView tvGuideTitle = holder.getView("tvGuideTitle");
        TextView tvGuideAuthor = holder.getView("tvGuideAuthor");
        MaterialButton btnOpenGuide = holder.getView("btnOpenGuide");

        tvGuideTitle.setText(item != null ? safeText(item.getTitle()) : "-");
        tvGuideAuthor.setText("Author: " + resolveAuthorName(item));

        btnOpenGuide.setOnClickListener(v -> {
            if (listener != null && item != null) {
                listener.onOpen(item);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item != null) {
                listener.onItemClicked(item);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setAuthorNameByUserId(Map<String, String> authorNameByUserId) {
        this.authorNameByUserId = authorNameByUserId != null
                ? authorNameByUserId
                : new HashMap<>();
        notifyDataSetChanged();
    }

    private String resolveAuthorName(Guide guide) {
        if (guide == null || guide.getContentCreatorId() == null) {
            return "-";
        }

        if ("DELETED_CREATOR".equals(guide.getContentCreatorId())) {
            return "Deleted creator";
        }

        String name = authorNameByUserId.get(guide.getContentCreatorId());
        return safeText(name);
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }
}