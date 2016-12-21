package org.tud.mensadresden.finding.offers.view;

import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;

import org.tud.mensadresden.R;

import java.util.ArrayList;
import java.util.List;

import static org.tud.mensadresden.finding.offers.service.Utils.formatDistance;

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

    private List<Job> items;
    private Location currentLocation;

    ListMensaAdapter() {
        this.items = new ArrayList<>();
    }

    ListMensaAdapter(List<Job> items) {
        this.items = items;
    }

    public void setItems(List<Job> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_offers_list_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Job job = items.get(position);
        holder.title.setText(job.getName());
        holder.description.setText(job.getDescription());
        if (currentLocation != null) {
            holder.distance.setText(formatDistance(job.getLocation().distanceTo(currentLocation), 1));
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.distance.setText("");
            holder.icon.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
