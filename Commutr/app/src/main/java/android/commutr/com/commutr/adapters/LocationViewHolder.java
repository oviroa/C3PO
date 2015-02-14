package android.commutr.com.commutr.adapters;

import android.commutr.com.commutr.R;
import android.commutr.com.commutr.base.CommutrTextView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by oviroa on 2/13/15.
 */
public class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected CommutrTextView pickup;
    protected CommutrTextView dropoff;
    protected long id;
    private LocationItemClickListener clickListener;

    public LocationViewHolder(View view) {
        super(view);
        pickup =  (CommutrTextView) view.findViewById(R.id.pickup_location);
        dropoff =  (CommutrTextView) view.findViewById(R.id.dropoff_location);
        view.setOnClickListener(this);
    }

    public void setClickListener(LocationItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View view) {
        clickListener.onClick(view, getPosition(), id);
    }
}
