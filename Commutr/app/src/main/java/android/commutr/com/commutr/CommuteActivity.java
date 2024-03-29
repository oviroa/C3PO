package android.commutr.com.commutr;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.commutr.com.commutr.base.BaseActivity;
import android.commutr.com.commutr.base.CommutrTextView;
import android.commutr.com.commutr.model.Commute;
import android.commutr.com.commutr.model.Location;
import android.commutr.com.commutr.model.LocationHour;
import android.commutr.com.commutr.model.TurboCommute;
import android.commutr.com.commutr.utils.Alarms;
import android.commutr.com.commutr.utils.ClientUtility;
import android.commutr.com.commutr.utils.DisplayMessenger;
import android.commutr.com.commutr.utils.Installation;
import android.commutr.com.commutr.utils.Logger;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommuteActivity extends BaseActivity implements OnItemSelectedListener{

    private SwipeRefreshLayout swipeView;
    private RequestQueue commuteVolley;
    private final Object TAG = new Object();
    private static Calendar nextAvailableCalendar;
    private static Calendar minDateCalendar;
    private static Calendar selectedPickupDateTime;
    private boolean viewIsInEditMode = true;
    private int screenOrientation;
    private LocationServices locationService;
    private PendingIntent geofenceRequestIntent;
    private GoogleApiClient apiClient;
    private CheckInFragment checkInDialog;
    private LocationsFragment locationsDialog;
    private LocationHour selectedRoute;
    /**
     ********************************************************************
     * Activity lifecycle functions
     * ******************************************************************
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commute);
        loadMixpanelSuperData();
        checkAndHandlePlayServices();
        handleProgressBar();
        setSpinners();
        calculateNextAvailableDate();
        displayAdjustedTime();
        handleButtonEvents();
        selectedPickupDateTime = nextAvailableCalendar;
        registerReceivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenOrientation = getRequestedOrientation();
        Commute currentCommute = getDataManager().getCachedCommute(getApplicationContext());
        Long currentTimeInMilliseconds = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        if(
                currentCommute != null
                        &&
                        (
                                ( currentTimeInMilliseconds < (currentCommute.getScheduledPickupArrivalTime()*1000 +
                                        getResources().getInteger(R.integer.arrival_offset)))
                                        ||
                                        ( sdf.format(currentTimeInMilliseconds).equals(sdf.format(currentCommute.getScheduledPickupArrivalTime()*1000)))
                        )
                )
        {
            handleResumeWhenCommutePresent(currentCommute);
        } else {
            handleResumeWhenCommuteNotPresent();
        }
        if(getDataManager().getCachedCheckInStatus(getApplicationContext()) != null) {
            showCheckinDialog();
        }
    }

    private void handleResumeWhenCommutePresent(Commute currentCommute) {
        populateUIWithCommute(currentCommute);
        String state = getDataManager().getCachedCommuteRequestStatus(getApplicationContext());
        if(state != null) {
            int id = getResources().getIdentifier(state, "string", "android.commutr.com.commutr");
            handleReservationStateDisplay(getResources().getString(id));
        }
    }

    private void handleResumeWhenCommuteNotPresent() {
        clearCache();
        calculateNextAvailableDate();
        displayAdjustedTime();
        selectedPickupDateTime = nextAvailableCalendar;
        if(!viewIsInEditMode) {
            enableFormElements();
            hideFloatingUI();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        mixpanel.track(getResources().getString(R.string.application_started), null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        mixpanel.track(getResources().getString(R.string.application_stopped), null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.flush();
        try {
            unregisterReceiver(requestReceiver);
            unregisterReceiver(checkInReceiver);
        } catch(IllegalArgumentException e) {
            Logger.warn("Unregister receiver",e.getLocalizedMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(checkInDialog != null) {
            checkInDialog.dismiss();
        }
    }

    /**
     ********************************************************************
     * Data management functions
     * ******************************************************************
     */

    public void handleSelectedRoute(long id) {
        setSelectedRoute(id);
        displaySelectedPickup();
        displaySelectedDropOff();
    }

    private void setSelectedRoute(long id) {
        selectedRoute = LocationHour.findById(LocationHour.class, id);
    }

    private void confirmCommute() {
        if (ClientUtility.isNetworkAvailable(getApplicationContext())) {
            if(selectedRoute != null) {
                disableFormElements();
                disableConfirmationButton();
                saveCommute(buildCommute());
                MixpanelAPI mixpanel =
                        MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
                mixpanel.getPeople().identify(mixpanel.getDistinctId());
                mixpanel.track(getResources().getString(R.string.commute_submitted), null);
            }
            else {
                DisplayMessenger.showBasicToast
                        (getApplicationContext(),
                                getResources().getString(R.string.please_select_route));
            }
        } else {
            DisplayMessenger.showBasicToast
                    (getApplicationContext(),
                            getResources().getString(R.string.no_internet_message));
        }
    }

    private void saveCommute(final Commute commute) {
        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }
        swipeView.setRefreshing(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getDataManager().storeCommute
                (
                        commute,
                        getApplicationContext(),
                        commuteVolley,
                        TAG,
                        new Listener<JSONObject>() {
                            public void onResponse(JSONObject result) {
                                setRequestedOrientation(screenOrientation);
                                swipeView.setRefreshing(false);
                                if(result.has("error")) {
                                    enableFormElements();
                                    DisplayMessenger.showBasicToast
                                            (getApplicationContext(),
                                                    getResources().getString(R.string.commute_error_message));
                                } else {
                                    getDataManager().cacheCommute(commute, getApplicationContext());
                                    if(checkInDialog != null && checkInDialog.isShown()){
                                        handleCommuteCheckInStore();
                                    } else {
                                        handleCommuteStore(result, commute);
                                    }
                                }
                            }
                        },
                        new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                setRequestedOrientation(screenOrientation);
                                swipeView.setRefreshing(false);
                                DisplayMessenger.showBasicToast
                                        (getApplicationContext(),
                                                getResources().getString(R.string.commute_error_message));
                                enableFormElements();
                            }
                        }
                );
    }

    private void handleCommuteCheckInStore() {
        checkInDialog.dismiss();
        checkInDialog = null;
        getDataManager().cacheCheckInStatus(null,getApplicationContext());
        DisplayMessenger.showBasicToast
                (getApplicationContext(),
                        getResources().getString(R.string.commute_check_in_message));
    }

    private void handleCommuteStore(JSONObject result, Commute commute) {
        DisplayMessenger.showBasicToast
                (getApplicationContext(),
                        getResources().getString(R.string.commute_confirmed_message));
        showFloatingUI();
        handleReservationStateDisplay(getResources().getString(R.string.requested));
        getDataManager().cacheCommuteRequestStatus(CommutrApp.REQUEST_REQUESTED,getApplicationContext());
        registerLocationAlarms();
        handleCommuteRequestResponse(result, commute);
    }


    private void getLocations() {

        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }
        swipeView.setRefreshing(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getDataManager().retrieveLocations
                (
                        getApplicationContext(),
                        commuteVolley,
                        TAG,
                        new Listener<JSONObject>() {
                            public void onResponse(JSONObject result) {
                                setRequestedOrientation(screenOrientation);
                                if (result.has("error")) {
                                    enableFormElements();
                                    DisplayMessenger.showBasicToast
                                            (getApplicationContext(),
                                                    getResources().getString(R.string.commute_error_message));
                                } else {
                                    if(result.length()!=0) {
                                        persistLocations(result);
                                        persistLocationHours(result);
                                        persistTimestamp(result);
                                    }
                                    displayLocationsDialog();
                                }
                                swipeView.setRefreshing(false);
                            }
                        },
                        new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                setRequestedOrientation(screenOrientation);
                                swipeView.setRefreshing(false);
                                DisplayMessenger.showBasicToast
                                        (getApplicationContext(),
                                                getResources().getString(R.string.commute_error_message));
                                enableFormElements();
                            }
                        }
                );
    }

    private void persistLocations(JSONObject result) {
        try {
            Location.deleteAll(Location.class);
            JSONArray locations = result.getJSONArray("location");
            for(int i=0;i<locations.length(); i++) {
                JSONObject jsonLocation = locations.getJSONObject(i);
                Gson gson = new Gson();
                Location location = gson.fromJson(jsonLocation.toString(), Location.class);
                List<Location> rs = Location.find(Location.class, "code = ?", location.getCode());
                if(rs.size() > 0) {
                    Location persitedLocation = rs.get(0);

                }

                location.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void persistLocationHours(JSONObject result) {
        try {
            LocationHour.deleteAll(LocationHour.class);
            JSONArray locationHours = result.getJSONArray("location_hours");
            for(int i=0;i<locationHours.length(); i++) {
                JSONObject jsonLocationHour = locationHours.getJSONObject(i);
                LocationHour locationHour = new LocationHour();
                locationHour.setStartTime(jsonLocationHour.getLong("start_time"));
                locationHour.setEndTime(jsonLocationHour.getLong("end_time"));
                locationHour.setRecurring(jsonLocationHour.getBoolean("recurring"));
                locationHour.setPickupLocation
                        (
                                Location.find(Location.class, "code=?", jsonLocationHour.getString("pickup_location")).get(0)
                        );
                locationHour.setDropoffLocation
                        (
                                Location.find(Location.class, "code=?", jsonLocationHour.getString("dropoff_location")).get(0)
                        );
                locationHour.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void persistTimestamp(JSONObject result) {
        try {
            int version = result.getInt("location_version");
            getDataManager().cacheLocationRetrievalTimestamp(version, getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancelCommute() {
        Commute currentCommute = getDataManager().getCachedCommute(getApplicationContext());
        currentCommute.setConfirmTime(0L);
        currentCommute.setCancelTime(System.currentTimeMillis() / 1000L);
        if(currentCommute != null) {
            if (ClientUtility.isNetworkAvailable(getApplicationContext())) {
                disableConfirmationButton();
                registerCancelledCommute(currentCommute);
                MixpanelAPI mixpanel =
                        MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
                mixpanel.getPeople().identify(mixpanel.getDistinctId());
                mixpanel.track(getResources().getString(R.string.commute_cancelled), null);
            } else {
                DisplayMessenger.showBasicToast
                        (getApplicationContext(),
                                getResources().getString(R.string.no_internet_message));
            }
        }
    }

    private void registerCancelledCommute(final Commute commute){
        if(commuteVolley == null) {
            commuteVolley = Volley.newRequestQueue(getApplicationContext());
        }
        swipeView.setRefreshing(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getDataManager().storeCommute
                (
                        commute,
                        getApplicationContext(),
                        commuteVolley,
                        TAG,
                        new Listener<JSONObject>() {
                            public void onResponse(JSONObject result) {
                                setRequestedOrientation(screenOrientation);
                                swipeView.setRefreshing(false);
                                if(result.has("error")) {
                                    showCancelButton();
                                    DisplayMessenger.showBasicToast
                                            (getApplicationContext(),
                                                    getResources().getString(R.string.commute_error_message));
                                } else {
                                    DisplayMessenger.showBasicToast
                                            (getApplicationContext(),
                                                    getResources().getString(R.string.commute_cancelled_message));
                                    hideFloatingUI();
                                    enableFormElements();
                                    clearCache();
                                    clearSelectedLocations();
                                    calculateNextAvailableDate();
                                    displayAdjustedTime();
                                    selectedPickupDateTime = nextAvailableCalendar;
                                    Alarms.unRegisterLocationAlarms(getApplicationContext());
                                }
                            }
                        },
                        new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                setRequestedOrientation(screenOrientation);
                                showCancelButton();
                                swipeView.setRefreshing(false);
                                DisplayMessenger.showBasicToast
                                        (getApplicationContext(),
                                                getResources().getString(R.string.commute_error_message));
                            }
                        }
                );
    }

    private void handleCommuteRequestResponse(JSONObject result, Commute commute) {
        if(result.has("commute")) {
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Type arrayListType = new TypeToken<ArrayList<TurboCommute>>() {}.getType();
                ArrayList<TurboCommute> commutes = gson.fromJson(result.get("commute").toString(), arrayListType);
                for (TurboCommute tCommute : commutes) {
                    if (tCommute.getUniqueId().equals(commute.getUniqueId())) {
                        getDataManager().cacheCommuteKey(tCommute.getKey(), getApplicationContext());
                        Alarms.registerCommuteConfirmationRequest(getApplicationContext());
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private Commute buildCommute() {
        Commute myCommute = new Commute();
        myCommute.setEmail(getDataManager().retrieveUserEmail(getApplicationContext()));
        myCommute.setTransportModeToPickup(getTransportModeToPickup());
        myCommute.setTransportModeToDropoff(getTransportModeToDropoff());
        myCommute.setPickupLocation(selectedRoute.getPickupLocation().getCode());
        myCommute.setDropoffLocation(selectedRoute.getDropoffLocation().getCode());
        myCommute.setScheduledPickupArrivalTime(getScheduledPickupArrivalTime());
        myCommute.setConfirmTime(System.currentTimeMillis() / 1000L);
        myCommute.setCancelTime(0L);
        myCommute.setDeviceIdentifier(Installation.id(getApplicationContext()));
        return myCommute;
    }

    private void clearCache() {
        getDataManager().cacheCommute(null, getApplicationContext());
        getDataManager().cacheCommuteKey(null, getApplicationContext());
        getDataManager().cacheCommuteRequestStatus(null, getApplicationContext());
        getDataManager().cacheCheckInStatus(null, getApplicationContext());
    }

    /**
     ********************************************************************
     * Event handling functions
     * ******************************************************************
     */

    private void registerReceivers() {
        IntentFilter comuteRequestFilter = new IntentFilter(CommutrApp.REQUEST_CONFIRMATION_EVENT);
        registerReceiver(requestReceiver, comuteRequestFilter);
        IntentFilter checkInFilter = new IntentFilter(CommutrApp.CHECK_IN_EVENT);
        registerReceiver(checkInReceiver, checkInFilter);
    }

    private void registerLocationAlarms() {
        Commute commute = getDataManager().getCachedCommute(getApplicationContext());
        if(commute != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(commute.getScheduledPickupArrivalTime() * 1000);
            Alarms.registerLocationAlarms(getApplicationContext(), calendar);
        }
    }

    protected final BroadcastReceiver requestReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CommutrApp.REQUEST_CONFIRMATION_EVENT)) {
                String state = intent.getStringExtra(CommutrApp.REQUEST_CONFIRMATION_STATE);
                int id = getResources().getIdentifier(state, "string", "android.commutr.com.commutr");
                handleReservationStateDisplay(getResources().getString(id));
                showRequestConfirmationNotification(getResources().getString(id));
            }
        }
    };

    protected final BroadcastReceiver checkInReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CommutrApp.CHECK_IN_EVENT)) {
                showCheckinDialog();
            }
        }
    };


    /**
     ********************************************************************
     * Device/app state functions
     * ******************************************************************
     */

    private void checkAndHandlePlayServices() {
        if(!ClientUtility.isGooglePlayServicesAvailable(getApplicationContext())) {
            MixpanelAPI mixpanel =
                    MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
            mixpanel.getPeople().identify(mixpanel.getDistinctId());
            mixpanel.track(getResources().getString(R.string.no_play_services), null);
            DisplayMessenger.showBasicToast(getApplicationContext(),getResources().getString(R.string.google_play_services_error));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getResources().getString(R.string.play_services_link)));
            startActivity(intent);
        }
    }

    private void showRequestConfirmationNotification(String state) {
        final Intent notificationIntent = new Intent(getApplicationContext(), CommuteActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(new StringBuilder().
                        append(getResources().getString(R.string.request_response_notification_title)).
                        append(" ").
                        append(state.toLowerCase()).toString())
                .setColor(getResources().getColor(R.color.top_bar_background))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                        + getApplicationContext().getPackageName() + "/raw/whisper"))
                .setContentText(getResources().getString(R.string.request_response_notification))
                .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true).build();
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    /**
     ********************************************************************
     * Time/Date functions
     * ******************************************************************
     */

    private long getScheduledPickupArrivalTime() {
        return selectedPickupDateTime.getTimeInMillis()/1000L;
    }

    private void calculateNextAvailableDate(){
        nextAvailableCalendar = Calendar.getInstance();
        int hour = nextAvailableCalendar.get(Calendar.HOUR_OF_DAY);
        int day = nextAvailableCalendar.get(Calendar.DAY_OF_WEEK);
        if(day == Calendar.SATURDAY){
            setNextAvailableCalendarFutureDayAM(2);
        } else if(day == Calendar.SUNDAY) {
            setNextAvailableCalendarFutureDayAM(1);
        } //Friday
        else if(hour >= getResources().getInteger(R.integer.latest_commute_set_time_am)){
            if(day == Calendar.FRIDAY){
                if(hour >= getResources().getInteger(R.integer.latest_commute_set_time_pm)) {
                    setNextAvailableCalendarFutureDayAM(3);
                } else {
                    setNextAvailableCalendarSameDayPM();
                }

            } else {
                if(hour >= getResources().getInteger(R.integer.latest_commute_set_time_pm)) {
                    setNextAvailableCalendarFutureDayAM(1);
                } else {
                    setNextAvailableCalendarSameDayPM();
                }
            }
        } else {
            setNextAvailableCalendarSameDayAM();
        }

        minDateCalendar = Calendar.getInstance();
        minDateCalendar.setTimeInMillis(nextAvailableCalendar.getTimeInMillis());
    }

    private void setNextAvailableCalendarSameDayAM () {
        nextAvailableCalendar.set(Calendar.HOUR_OF_DAY, getResources().getInteger(R.integer.earliest_commute_set_time_am));
        nextAvailableCalendar.set(Calendar.MINUTE, 0);
    }

    private void setNextAvailableCalendarSameDayPM () {
        nextAvailableCalendar.set(Calendar.HOUR_OF_DAY, getResources().getInteger(R.integer.earliest_commute_set_time_pm));
        nextAvailableCalendar.set(Calendar.MINUTE, 0);
    }

    private void setNextAvailableCalendarFutureDayAM (int offset) {
        nextAvailableCalendar.add(Calendar.DATE, offset);
        nextAvailableCalendar.set(Calendar.HOUR_OF_DAY, getResources().getInteger(R.integer.earliest_commute_set_time_am));
        nextAvailableCalendar.set(Calendar.MINUTE, 0);
    }


    public void checkIn(View v) {
        Commute commute = getDataManager().getCachedCommute(getApplicationContext());
        commute.setPickupCheckinTime(System.currentTimeMillis() / 1000L);
        saveCommute(commute);
    }

    /**
     ********************************************************************
     * Analytics functions
     * ******************************************************************
     */

    private void loadMixpanelSuperData() {
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        mixpanel.getPeople().set(getResources().getString(R.string.user_email),
                getDataManager().retrieveUserEmail(getApplicationContext()));
        Crashlytics.setUserIdentifier(mixpanel.getDistinctId());
    }

    /**
     ********************************************************************
     * UI handling functions
     * ******************************************************************
     */

    private void displaySelectedPickup() {
        CommutrTextView pickupTextView = (CommutrTextView) findViewById(R.id.pickup_location_value);
        pickupTextView.setText(selectedRoute.getPickupLocation().getName());
    }

    private void displaySelectedDropOff() {
        CommutrTextView dropOffTextView = (CommutrTextView) findViewById(R.id.drop_off_location_value);
        dropOffTextView.setText(selectedRoute.getDropoffLocation().getName());
    }

    private void displayAdjustedTime() {
        TextView commuteDateValue = (TextView) findViewById(R.id.commute_date_value);
        TextView selectedCommuteDate = (TextView) findViewById(R.id.select_commute_date_value);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
        String currentDate = sdf.format(nextAvailableCalendar.getTime());
        commuteDateValue.setText(currentDate);
        selectedCommuteDate.setText(currentDate);
        TextView selectetArrivalTime = (TextView) findViewById(R.id.pickup_arrival_value);
        sdf = new SimpleDateFormat("h:mm a");
        String selectedTime = sdf.format(nextAvailableCalendar.getTimeInMillis());
        selectetArrivalTime.setText(selectedTime);
    }

    public void showLocations(View v) {
        getLocations();
    }

    public void displayLocationsDialog() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            locationsDialog = new LocationsFragment();
            Bundle bundle = new Bundle();
            bundle.putLong("commute_time", selectedPickupDateTime.getTimeInMillis());
            locationsDialog.setArguments(bundle);
            locationsDialog.show(fragmentManager, "locations_dialog");
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        switch(parent.getId()) {
            case R.id.commuter_type_spinner:
                if(commuterTypeSpinner.getSelectedItem().equals(getResources().getString(R.string.driver))) {
                    gettingToPickupSpinner.setSelection
                            (((ArrayAdapter<String>)gettingToPickupSpinner.getAdapter()).getPosition(getResources().getString(R.string.car)),
                                    true);
                }
                break;
            case R.id.getting_to_pickup_spinner:
                if(gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.bike))
                        || gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.walking))) {
                    commuterTypeSpinner.setSelection(((ArrayAdapter<String>)commuterTypeSpinner.getAdapter()).getPosition(getResources().getString(R.string.rider)),
                            true);
                }
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private void handleButtonEvents() {
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

    private void editCommute() {
        hideFloatingUI();
        enableFormElements();
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getApplicationContext(), getResources().getString(R.string.mixpanel_token));
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        mixpanel.track(getResources().getString(R.string.commute_edited), null);
    }

    private void showFloatingUI() {
        ImageButton editCommuteButton = (ImageButton) findViewById(R.id.edit_commute);
        editCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
        editCommuteButton.setVisibility(View.VISIBLE);
        showCancelButton();
    }

    private void hideFloatingUI() {
        //hide floating button for edit
        ImageButton editCommuteButton = (ImageButton) findViewById(R.id.edit_commute);
        editCommuteButton.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
        editCommuteButton.setVisibility(View.GONE);
        showConfirmButton();
    }

    private void handleReservationStateDisplay(String message) {
        CardView statusCardView = (CardView) findViewById(R.id.status_card_view);
        CommutrTextView statusTextView = (CommutrTextView) findViewById(R.id.status_text_view);
        statusTextView.setText(message);
        if(statusCardView.getVisibility() == View.GONE) {
            statusCardView.setVisibility(View.VISIBLE);
            statusCardView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_fade_in));
        }
    }

    private void disableFormElements() {
        viewIsInEditMode = false;
        setFormElementsState(false);
    }

    private void enableFormElements() {
        viewIsInEditMode = true;
        setFormElementsState(true);
        showConfirmButton();
        hideRequestStatus();
    }

    private void setFormElementsState(Boolean enabled) {
        Button pickupArrivalButton = (Button) findViewById(R.id.pickup_arrival_button);
        pickupArrivalButton.setEnabled(enabled);
        Button commuteDateButton = (Button) findViewById(R.id.commute_date_button);
        commuteDateButton.setEnabled(enabled);
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        commuterTypeSpinner.setEnabled(enabled);
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        gettingToPickupSpinner.setEnabled(enabled);
        Button locationsButton = (Button) findViewById(R.id.commute_location_button);
        locationsButton.setEnabled(enabled);
    }

    private void disableConfirmationButton() {
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setEnabled(false);
        confirmCommuteButton.setAlpha(0.6f);
    }


    private void showConfirmButton() {
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setEnabled(true);
        confirmCommuteButton.setAlpha(1f);
        confirmCommuteButton.setText(getResources().getString(R.string.confirm_my_commute));
        confirmCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCommute();
            }
        });
    }

    private void showCancelButton() {
        //confirm button
        Button confirmCommuteButton = (Button) findViewById(R.id.confirm_commute_button);
        confirmCommuteButton.setEnabled(true);
        confirmCommuteButton.setAlpha(1f);
        confirmCommuteButton.setText(getResources().getString(R.string.cancel_my_commute));
        confirmCommuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelCommute();
            }
        });
    }

    private void hideRequestStatus() {
        CardView statusCardView = (CardView) findViewById(R.id.status_card_view);
        if(statusCardView.getVisibility() == View.VISIBLE) {
            statusCardView.setVisibility(View.GONE);
            statusCardView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out));
        }
    }

    private void setSpinners() {
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_of_commuter_options, R.layout.spinner_item);
        setSingleSpinner(commuterTypeSpinner,adapter);
        commuterTypeSpinner.setOnItemSelectedListener(this);
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        adapter = ArrayAdapter.createFromResource(this,
                R.array.getting_to_pickup_options, R.layout.spinner_item);
        setSingleSpinner(gettingToPickupSpinner,adapter);
        gettingToPickupSpinner.setOnItemSelectedListener(this);
    }

    private void setSingleSpinner(Spinner spinner, ArrayAdapter <CharSequence> adapter) {
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
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
            try {
                datePickerDialog.getDatePicker().setMinDate(minDateCalendar.getTimeInMillis());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                DisplayMessenger.showBasicToast(getActivity().getApplicationContext(),
                        getActivity().getResources().getString(R.string.day_out_of_bounds));
            } else {
                ((CommuteActivity)this.getActivity()).clearSelectedLocations();
                TextView selectedCommuteDate = (TextView) getActivity().findViewById(R.id.select_commute_date_value);
                TextView commuteDateValue = (TextView) getActivity().findViewById(R.id.commute_date_value);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
                selectedPickupDateTime.set(Calendar.YEAR, year);
                selectedPickupDateTime.set(Calendar.MONTH, month);
                selectedPickupDateTime.set(Calendar.DAY_OF_MONTH, day);
                String selectedDate = sdf.format(selectedPickupDateTime.getTimeInMillis());
                selectedCommuteDate.setText(selectedDate);
                commuteDateValue.setText(selectedDate);
            }
        }
    }

    private void showCheckinDialog() {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            checkInDialog = new CheckInFragment();
            checkInDialog.show(fragmentManager, "check_in_dialog");
        } catch (IllegalStateException e) {

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
            if (
                    ((hourOfDay < getActivity().getResources().getInteger(R.integer.earliest_commute_set_time_am)
                            || hourOfDay >= getActivity().getResources().getInteger(R.integer.latest_commute_set_time_am)))
                            && ((hourOfDay < getActivity().getResources().getInteger(R.integer.earliest_commute_set_time_pm)
                            || hourOfDay >= getActivity().getResources().getInteger(R.integer.latest_commute_set_time_pm)))
            ) {
                DisplayMessenger.showBasicToast(getActivity().getApplicationContext(),
                        getActivity().getResources().getString(R.string.time_out_of_bounds));
                hourOfDay = getActivity().getResources().getInteger(R.integer.earliest_commute_set_time_am);
                minute = 0;
            }
            ((CommuteActivity)this.getActivity()).clearSelectedLocations();
            selectedPickupDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedPickupDateTime.set(Calendar.MINUTE, minute);
            String selectedTime = sdf.format(selectedPickupDateTime.getTimeInMillis());
            selectetArrivalTime.setText(selectedTime);
        }
    }

   public void clearSelectedLocations() {
        if(selectedRoute != null) {
            CommutrTextView pickupTextView = (CommutrTextView) findViewById(R.id.pickup_location_value);
            pickupTextView.setText("");
            CommutrTextView dropOffTextView = (CommutrTextView) findViewById(R.id.drop_off_location_value);
            dropOffTextView.setText("");
            selectedRoute = null;
        }
    }

    private void populateUIWithCommute(Commute commute) {
        long cachedArrivalTime = commute.getScheduledPickupArrivalTime()*1000;
        TextView commuteDateValue = (TextView) findViewById(R.id.commute_date_value);
        TextView selectedCommuteDate = (TextView) findViewById(R.id.select_commute_date_value);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd ");
        String commuteDate = sdf.format(cachedArrivalTime);
        commuteDateValue.setText(commuteDate);
        selectedCommuteDate.setText(commuteDate);
        TextView selectetArrivalTime = (TextView) findViewById(R.id.pickup_arrival_value);
        sdf = new SimpleDateFormat("h:mm a");
        String arrivalTime = sdf.format(cachedArrivalTime);
        selectetArrivalTime.setText(arrivalTime);
        selectedPickupDateTime.setTimeInMillis(cachedArrivalTime);
        setTransportModeToDropoff(commute.getTransportModeToDropoff());
        setTransportModeToPickupSpinner(commute.getTransportModeToPickup());
        populateRouteNames(commute);
        if(viewIsInEditMode) {
            disableFormElements();
            showFloatingUI();
        }
    }

    private void populateRouteNames(Commute commute) {
        Location pickupLocation = Location.find(Location.class, "code = ?", commute.getPickupLocation()).get(0);
        CommutrTextView pickupTextView = (CommutrTextView) findViewById(R.id.pickup_location_value);
        pickupTextView.setText(pickupLocation.getName());
        Location dropoffLocation = Location.find(Location.class, "code = ?", commute.getDropoffLocation()).get(0);
        CommutrTextView dropOffTextView = (CommutrTextView) findViewById(R.id.drop_off_location_value);
        dropOffTextView.setText(dropoffLocation.getName());
    }

    private void setTransportModeToPickupSpinner(int transportModeToPickup) {
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        if(transportModeToPickup == getResources().getInteger(R.integer.walking)) {
            gettingToPickupSpinner.setSelection(0, true);
        }
        else if (transportModeToPickup == getResources().getInteger(R.integer.bike)) {
            gettingToPickupSpinner.setSelection(1, true);
        }
        else if (transportModeToPickup == getResources().getInteger(R.integer.car))  {
            gettingToPickupSpinner.setSelection(2, true);
        }
    }

    private int getTransportModeToPickup() {
        Spinner gettingToPickupSpinner = (Spinner) findViewById(R.id.getting_to_pickup_spinner);
        if(gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.walking))) {
            return getResources().getInteger(R.integer.walking);
        }
        else if (gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.bike))) {
            return getResources().getInteger(R.integer.bike);
        }
        else if (gettingToPickupSpinner.getSelectedItem().equals(getResources().getString(R.string.car))) {
            return getResources().getInteger(R.integer.car);
        }
        return -1;
    }

    private void setTransportModeToDropoff(int transportModeToDropOff) {
        Spinner gettingToDropOffSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        if(transportModeToDropOff == getResources().getInteger(R.integer.driver)) {
            gettingToDropOffSpinner.setSelection(0,true);
        }
        else if (transportModeToDropOff == getResources().getInteger(R.integer.rider)) {
            gettingToDropOffSpinner.setSelection(1,true);
        }
    }

    private int getTransportModeToDropoff() {
        Spinner commuterTypeSpinner = (Spinner) findViewById(R.id.commuter_type_spinner);
        if(commuterTypeSpinner.getSelectedItem().equals(getResources().getString(R.string.driver))) {
            return getResources().getInteger(R.integer.driver);
        }
        else if (commuterTypeSpinner.getSelectedItem().equals(getResources().getString(R.string.rider))) {
            return getResources().getInteger(R.integer.rider);
        }
        return -1;
    }
}
