package com.yoav_s.tashtit.ADPTERS;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.yoav_s.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.HistoryNote;
import com.yoav_s.tashtit.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.yoav_s.helper.BitMapHelper;

public class HistoryEntriesAdapter extends GenericAdapter<HistoryNote> {

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public HistoryEntriesAdapter(List<HistoryNote> items) {
        super(
                items,
                R.layout.item_history_entry,
                holder -> {
                    holder.putView("ivHistoryPhoto", holder.itemView.findViewById(R.id.ivHistoryPhoto));
                    holder.putView("tvHistoryDate", holder.itemView.findViewById(R.id.tvHistoryDate));
                    holder.putView("tvHistoryLabel", holder.itemView.findViewById(R.id.tvHistoryLabel));
                    holder.putView("tvHistoryValue", holder.itemView.findViewById(R.id.tvHistoryValue));
                },
                (holder, item, position) -> {
                    // real binding below
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        HistoryNote item = getItem(position);

        ImageView ivHistoryPhoto = holder.getView("ivHistoryPhoto");
        TextView tvHistoryDate = holder.getView("tvHistoryDate");
        TextView tvHistoryLabel = holder.getView("tvHistoryLabel");
        TextView tvHistoryValue = holder.getView("tvHistoryValue");

        tvHistoryDate.setText(formatDate(item));
        tvHistoryLabel.setText(formatLabel(item));
        tvHistoryValue.setText(formatValue(item));

        Bitmap bitmap = BitMapHelper.decodeBase64(item != null ? item.getPhoto() : null);
        if (bitmap != null) {
            ivHistoryPhoto.setVisibility(View.VISIBLE);
            ivHistoryPhoto.setImageBitmap(bitmap);
        } else {
            ivHistoryPhoto.setVisibility(View.GONE);
            ivHistoryPhoto.setImageDrawable(null);
        }
    }

    private String formatDate(HistoryNote item) {
        if (item == null || item.getCreatedAt() == null) {
            return "-";
        }
        return dateFormat.format(new Date(item.getCreatedAt().toDate().getTime()));
    }

    private String formatLabel(HistoryNote item) {
        if (item == null || item.getEntryType() == null) {
            return "Text:";
        }

        if (item.getEntryType() == HistoryNote.EntryType.TASK) {
            return "Task:";
        }

        return "Text:";
    }

    private String formatValue(HistoryNote item) {
        if (item == null) {
            return "\"-\"";
        }

        String text = item.getText() != null ? item.getText().trim() : "";
        boolean hasPhoto = item.getPhoto() != null && !item.getPhoto().trim().isEmpty();

        if (!text.isEmpty()) {
            return "\"" + text + "\"";
        }

        if (hasPhoto) {
            return "\"Photo note\"";
        }

        return "\"-\"";
    }
}