package android.commutr.com.commutr.adapters;

import android.commutr.com.commutr.LocationsFragment;
import android.commutr.com.commutr.R;
import android.commutr.com.commutr.model.LocationHour;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by oviroa on 2/13/15.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationViewHolder>{

    private List<LocationHour> locationList;
    private LocationsFragment parentFragment;

    public LocationAdapter(List<LocationHour> locationList, LocationsFragment parentFragment) {
        this.locationList = locationList;
        this.parentFragment = parentFragment;
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    @Override
    public void onBindViewHolder(LocationViewHolder locationViewHolder, int i) {
        LocationHour location = locationList.get(i);
        locationViewHolder.pickup.setText(location.getPickupLocation().getName());
        locationViewHolder.dropoff.setText(location.getDropoffLocation().getName());
        locationViewHolder.id = location.getId();
        locationViewHolder.setClickListener(new LocationItemClickListener() {
            @Override
            public void onClick(View v, int pos, long id) {
                parentFragment.callback(id);
            }
        });
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.location_card_view, viewGroup, false);
        return new LocationViewHolder(itemView);
    }
}
