package com.yoav_s.tashtit.ADPTERS;

import androidx.annotation.NonNull;

import android.widget.TextView;

import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.GuideInteraction;
import com.yoav_s.tashtit.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuideCommentsAdapter extends GenericAdapter<GuideInteraction> {

    public interface Listener {
        void onCommentClicked(GuideInteraction interaction);
    }

    private Listener listener;
    private Map<String, String> userDisplayNameById = new HashMap<>();

    public GuideCommentsAdapter(List<GuideInteraction> items) {
        super(
                items,
                R.layout.item_guide_comment,
                holder -> {
                    holder.putView("tvCommentAuthor", holder.itemView.findViewById(R.id.tvCommentAuthor));
                    holder.putView("tvCommentBody", holder.itemView.findViewById(R.id.tvCommentBody));
                },
                (holder, item, position) -> {
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        GuideInteraction item = getItem(position);

        TextView tvCommentAuthor = holder.getView("tvCommentAuthor");
        TextView tvCommentBody = holder.getView("tvCommentBody");

        tvCommentAuthor.setText(resolveAuthorName(item));
        tvCommentBody.setText(resolveCommentBody(item));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && item != null) {
                listener.onCommentClicked(item);
            }
        });
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setUserDisplayNameById(Map<String, String> userDisplayNameById) {
        this.userDisplayNameById = userDisplayNameById != null
                ? userDisplayNameById
                : new HashMap<>();
        notifyDataSetChanged();
    }

    private String resolveAuthorName(GuideInteraction item) {
        if (item == null || item.getUserId() == null) {
            return "-";
        }

        if ("DELETED_USER".equals(item.getUserId())) {
            return "Deleted user";
        }

        String name = userDisplayNameById.get(item.getUserId());
        return safeText(name);
    }

    private String resolveCommentBody(GuideInteraction item) {
        if (item == null) {
            return "-";
        }

        return safeText(item.getBody());
    }

    private static String safeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        return text.trim();
    }
}