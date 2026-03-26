package com.yoav_s.tashtit.ADPTERS;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uri_r.tashtit.ADPTERS.BASE.GenericAdapter;
import com.yoav_s.model.Specie;
import com.yoav_s.tashtit.R;

import java.util.List;

public class SpeciesAdapter extends GenericAdapter<Specie> {
    public SpeciesAdapter(List<Specie> items) {
        super(
                items,
                R.layout.item_species,
                holder -> holder.putView("tvName", holder.itemView.findViewById(R.id.tvSpeciesName)),
                (holder, item, position) ->
                        ((TextView) holder.getView("tvName")).setText(item.getName())
        );
    }
}