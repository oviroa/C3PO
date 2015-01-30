package android.commutr.com.commutr;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.commutr.com.commutr.base.BaseActivity;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.utils.Installation;
import android.commutr.com.commutr.utils.Logger;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CommuteActivity extends BaseActivity {

    private SwipeRefreshLayout swipeView;

    //request queue for server calls
    private RequestQueue commuteVolley;
    //tag for Volley
    private final Object TAG = new Object();

    private static Calendar nextAvailableCalendar;
    private static Calendar selectedPickupDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_commute);

        handleProgressBar();

        setSpinners();

        setNextAvailableDate();

        handleButtonEvents();

        selectedPickupDateTime = Calendar.getInstance();
    }

    private void handleButtonEvents()
    {
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCommute();
            }
        });

        ImageButton editCommuteButton = (ImageButton) findViewById(R.id.edit_commute);
        editCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCommute();
            }
        });

        ImageButton cancelCommuteButton = (ImageButton) findViewById(R.id.cancel_commute);
        cancelCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelCommute();
            }
        });
    }

    private void handleProgressBar() {
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        swipeView.setProgressViewOffset(true, actionBarSize, actionBarSize + getResources().getInteger(R.integer.pull_refresh_offset));
        swipeView.setEnabled(false);
    }


    private void cancelCommute()
    {

    }

    private void editCommute()
    {
        hideFloatingUI();
        enableFormElements();
    }

    private void confirmCommute()
    {
        //disable form
        disableFormElements();

        //save commute
        saveCommute(buildCommute());


    }

    private void showFloatingUI()
    {
        //show floating buttond for edit/cancel
        ImageButton editCommuteButton = (ImageButton) findViewById(R.id.edit_commute);
        editCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
        editCommuteButton.setVisibility(View.VISIBLE);

        ImageButton cancelCommuteButton = (ImageButton) findViewById(R.id.cancel_commute);
        cancelCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
        cancelCommuteButton.setVisibility(View.VISIBLE);
    }


    private void hideFloatingUI()
    {
        //hide floating button for edit
        ImageButton editCommuteButton = (ImageButton) findViewById(R.id.edit_commute);
        editCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
        editCommuteButton.setVisibility(View.GONE);

        ImageButton cancelCommuteButton = (ImageButton) findViewById(R.id.cancel_commute);
        cancelCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
        cancelCommuteButton.setVisibility(View.GONE);
    }


    private void saveCommute(final Commute commute)
    {
        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }

        swipeView.setRefreshing(true);

        getDataManager().storeCommute
                (
                        commute,
                        getApplicationContext(),
                        commuteVolley,
                        TAG,
                        new Listener<JSONObject>() {

                            public void onResponse(JSONObject result) {

                                getDataManager().cacheCommute(commute,getApplicationContext());
                                swipeView.setRefreshing(false);
                                showFloatingUI();

                            }
                        },
                        new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {

                                swipeView.setRefreshing(true);

                                //TODO show error message

                                Logger.warn("RESPONSE","EROOR");
                                hideFloatingUI();
                                enableFormElements();
                            }
                        }
                );
    }



    private Commute buildCommute(){

        //create commute model instance
        Commute myCommute = new Commute();

        myCommute.setEmail(getDataManager().retrieveUserEmail(getApplicationContext()));
        myCommute.setDeviceIdentifier(Installation.id(getApplicationContext()));
        myCommute.setTransportModeToPickup(getTransportModeToPickup());
        myCommute.setTransportModeToDropoff(getTransportModeToDropoff());
        myCommute.setPickupLocation(getResources().getString(R.string.default_pickup_location));
        myCommute.setDropoffLocation(getResources().getString(R.string.default_dropoff_location));
        myCommute.setScheduledPickupArrivalTime(getScheduledPickupArrivalTime());
        myCommute.setConfirmTime(System.currentTimeMillis() / 1000L);

        return myCommute;

    }


    private int getTransportModeToPickup() {


        //getting to pickup
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);


        if(gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.walking))) {
            return 1;
        }
        else if (gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.bike))) {
            return 2;
        }
        else if (gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.car))) {
            return 5;
        }

        //error
        return -1;
    }

    private int getTransportModeToDropoff() {

        //type of commuter
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);

        if(commuterTypeSpinner.getSelectedItem().equals(getResources().getString(R.string.driver))) {
            return 3;
        }
        else if (commuterTypeSpinner.getSelectedItem().equals(getResources().getString(R.string.rider))) {
            return 4;
        }

        return -1;
    }

    private long getScheduledPickupArrivalTime() {

        return selectedPickupDateTime.getTimeInMillis()/1000L;
    }

    private void disableFormElements()
    {
        //pick commute date
        Button pickupArrivalButton = (Button) findViewById(R.id.pickup_arrival_button);
        pickupArrivalButton.setEnabled(false);

        //pick pickup arrival
        Button commuteDateButton = (Button) findViewById(R.id.commute_date_button);
        commuteDateButton.setEnabled(false);

        //type of commuter
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        commuterTypeSpinner.setEnabled(false);

        //getting to pickup
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        gettingToPickupSpinner.setEnabled(false);

        //confirm button
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setEnabled(false);
        confirmCommuteButton.setAlpha(0.6f);
    }

    private void enableFormElements()
    {
        //pick commute date
        Button pickupArrivalButton = (Button) findViewById(R.id.pickup_arrival_button);
        pickupArrivalButton.setEnabled(true);

        //pick pickup arrival
        Button commuteDateButton = (Button) findViewById(R.id.commute_date_button);
        commuteDateButton.setEnabled(true);

        //type of commuter
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        commuterTypeSpinner.setEnabled(true);

        //getting to pickup
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        gettingToPickupSpinner.setEnabled(true);

        //confirm button
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setEnabled(true);
        confirmCommuteButton.setAlpha(1f);
    }

    private void setNextAvailableDate(){

        nextAvailableCalendar = Calendar.getInstance();
        int hour = nextAvailableCalendar.get(Calendar.HOUR_OF_DAY);
        int day = nextAvailableCalendar.get(Calendar.DAY_OF_WEEK);

        //if later than deadline for setting a commute for today, show tommorow
        if(hour >= getResources().getInteger(R.integer.latest_commute_set_time)){
            if(day == Calendar.FRIDAY){
                nextAvailableCalendar.add(Calendar.DATE, 3);
            } else if(day == Calendar.SATURDAY){
                nextAvailableCalendar.add(Calendar.DATE, 2);
            } else {
                nextAvailableCalendar.add(Calendar.DATE, 1);
            }
        }


        TextView commuteDateValue = (TextView) findViewById(R.id.commute_date_value);
        TextView selectedCommuteDate = (TextView) findViewById(R.id.select_commute_date_value);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
        String currentDate = sdf.format(nextAvailableCalendar.getTime());
        commuteDateValue.setText(currentDate);
        selectedCommuteDate.setText(currentDate);

    }

    private void setSpinners()
    {
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.type_of_commuter_options, R.layout.spinner_item);
        setSingleSpinner(commuterTypeSpinner,adapter);

        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.getting_to_pickup_options, R.layout.spinner_item);
        setSingleSpinner(gettingToPickupSpinner,adapter);

    }

    private void setSingleSpinner(Spinner spinner, ArrayAdapter <CharSequence> adapter)
    {
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_commute, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int year = nextAvailableCalendar.get(Calendar.YEAR);
            int month = nextAvailableCalendar.get(Calendar.MONTH);
            int day = nextAvailableCalendar.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            datePickerDialog.getDatePicker().setMinDate(nextAvailableCalendar.getTimeInMillis());
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView selectedCommuteDate = (TextView) getActivity().findViewById(R.id.select_commute_date_value);
            TextView commuteDateValue = (TextView) getActivity().findViewById(R.id.commute_date_value);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
            selectedPickupDateTime.set(Calendar.YEAR,year);
            selectedPickupDateTime.set(Calendar.MONTH,month);
            selectedPickupDateTime.set(Calendar.DAY_OF_MONTH,day);
            String selectedDate = sdf.format(selectedPickupDateTime.getTimeInMillis());
            selectedCommuteDate.setText(selectedDate);
            commuteDateValue.setText(selectedDate);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            TextView selectetArrivalTime = (TextView) getActivity().findViewById(R.id.pickup_arrival_value);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
            selectedPickupDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedPickupDateTime.set(Calendar.MINUTE, minute);
            String selectedTime = sdf.format(selectedPickupDateTime.getTimeInMillis());
            selectetArrivalTime.setText(selectedTime);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        //TODO check if commute cached
        Commute currentCommute = getDataManager().getCachedCommute(getApplicationContext());


        if(currentCommute != null)
        {
            populateUIWithCommute(currentCommute);
        }

        //TODO check if commute in the past

        //TODO display cached data in interface if in the future

    }

    private void populateUIWithCommute(Commute commute)
    {
        long cachedArrivalTime = commute.getScheduledPickupArrivalTime()*1000;

        //set commute date date in top/bottom window
        TextView commuteDateValue = (TextView) findViewById(R.id.commute_date_value);
        TextView selectedCommuteDate = (TextView) findViewById(R.id.select_commute_date_value);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
        String commuteDate = sdf.format(cachedArrivalTime);
        commuteDateValue.setText(commuteDate);
        selectedCommuteDate.setText(commuteDate);

        //arrival time
        TextView selectetArrivalTime = (TextView) findViewById(R.id.pickup_arrival_value);
        sdf = new SimpleDateFormat("h:mm a");
        String arrivalTime = sdf.format(cachedArrivalTime);
        selectetArrivalTime.setText(arrivalTime);

        //set local selected pickup time
        selectedPickupDateTime.setTimeInMillis(cachedArrivalTime);


        //type of commuter
        //getting to pickup//

        disableFormElements();
        showFloatingUI();

    }
}
