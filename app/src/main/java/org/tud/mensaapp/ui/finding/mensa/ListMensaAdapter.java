package org.tud.mensaapp.ui.finding.mensa;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import org.tud.mensaapp.R;
import org.tud.mensaapp.model.entity.Mensa;

import java.util.ArrayList;
import java.util.List;

import static org.tud.mensaapp.model.service.mensa.Utils.formatDistance;

public class ListMensaAdapter extends RecyclerView.Adapter<ListMensaAdapter.Holder> {

    static class Holder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        TextView distance;
        IconicsImageView icon;

        Holder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.job_offer_title);
            description = (TextView) itemView.findViewById(R.id.job_offer_description);
            distance = (TextView) itemView.findViewById(R.id.job_offer_distance_text);
            icon = (IconicsImageView) itemView.findViewById(R.id.job_offer_distance_icon);
        }
    }

    private List<Mensa> items;
    private Location currentLocation;
    private Context context;

    ListMensaAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    ListMensaAdapter(List<Mensa> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public void setItems(List<Mensa> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mensa_offers_list_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final Mensa mensa = items.get(position);
        holder.title.setText(mensa.getName());
        holder.description.setText(mensa.getAddress());
        if (currentLocation != null) {
            holder.distance.setText(formatDistance(mensa.getCoordinates().distanceTo(currentLocation), 1));
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.distance.setText("");
            holder.icon.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MensaDetailsActivity.class);
                i.putExtra("id", String.valueOf(mensa.getId()));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
