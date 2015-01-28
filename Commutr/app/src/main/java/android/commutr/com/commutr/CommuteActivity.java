package android.commutr.com.commutr;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.commutr.com.commutr.base.BaseActivity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CommuteActivity extends BaseActivity {

    private static Calendar nextAvailableCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commute);
        setSpinners();

        setNextAvailableDate();
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
        R.array.type_of_commuter_options, android.R.layout.simple_spinner_item);
        setSingleSpinner(commuterTypeSpinner,adapter);

        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.getting_to_pickup_options, android.R.layout.simple_spinner_item);
        setSingleSpinner(gettingToPickupSpinner,adapter);

    }

    private void setSingleSpinner(Spinner spinner, ArrayAdapter <CharSequence> adapter)
    {
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.DAY_OF_MONTH,day);
            String selectedDate = sdf.format(c.getTimeInMillis());
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
            SimpleDateFormat sdf = new SimpleDateFormat("h:m a");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            String selectedTime = sdf.format(c.getTimeInMillis());
            selectetArrivalTime.setText(selectedTime);
        }
    }
}
