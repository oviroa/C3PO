package android.commutr.com.commutr;


import android.app.AlertDialog;
import android.app.Dialog;
import android.commutr.com.commutr.adapters.LocationAdapter;
import android.commutr.com.commutr.managers.DataManager;
import android.commutr.com.commutr.managers.LocationHoursManager;
import android.commutr.com.commutr.model.LocationHour;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link android.support.v4.app.DialogFragment} subclass.
 */
public class LocationsFragment extends DialogFragment {

    private AlertDialog alert;
    private boolean isShown = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_locations, null);
        RecyclerView locationList = (RecyclerView)view.findViewById(R.id.location_list);
        locationList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        locationList.setLayoutManager(llm);
        Calendar commuteTime = Calendar.getInstance();
        commuteTime.setTimeInMillis(getArguments().getLong("commute_time"));
        String userEmail = DataManager.getInstance().retrieveUserEmail(getActivity().getApplicationContext());
        List<LocationHour> locations = LocationHoursManager.getOpenLocationHours(commuteTime, userEmail);
        LocationAdapter locationAdapter =
                new LocationAdapter
                    (
                        locations,
                        new RouteIdCallback(){
                            @Override
                            public void execute(long id) {
                                ((CommuteActivity)getActivity()).handleSelectedRoute(id);
                                LocationsFragment.this.dismiss();
                            }
                        }
                    );
        locationList.setAdapter(locationAdapter);
        builder.setView(view);
        alert = builder.create();
        return alert;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (isShown) return;
        super.show(manager, tag);
        isShown = true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        isShown = false;
        super.onDismiss(dialog);
    }

    public boolean isShown() {
        return isShown;
    }


    public interface RouteIdCallback{
        void execute(long id);
    }
}
