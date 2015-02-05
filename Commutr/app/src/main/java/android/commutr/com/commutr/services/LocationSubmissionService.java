package android.commutr.com.commutr.services;

import android.app.Service;
import android.commutr.com.commutr.CommutrApp;
import android.commutr.com.commutr.utils.Logger;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by oviroa on 2/3/15.
 */
public class LocationSubmissionService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
                    LocationListener{


    private LocationRequest mLocationRequest;

    public LocationSubmissionService()
    {
        //super("LocationSubmissionService");
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(25000);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                CommutrApp.mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Logger.warn("Loco",""+location);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

         switch(intent.getStringExtra(CommutrApp.ACTION_TYPE)) {

             case CommutrApp.CONNECT:

                 Logger.warn("LOCO","CONNECT");

                 CommutrApp.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

                 CommutrApp.mGoogleApiClient.connect();

                 break;

             case CommutrApp.DISCONNECT:

                 Logger.warn("LOCO","DISCONNECT");

                 if(CommutrApp.mGoogleApiClient != null) {

                     Logger.warn("LOCO","IN DISCONNECT");
                     LocationServices.FusedLocationApi
                             .removeLocationUpdates(CommutrApp.mGoogleApiClient, this);
                     CommutrApp.mGoogleApiClient.disconnect();
                     CommutrApp.mGoogleApiClient = null;
                 }

                 stopSelf();

                 break;
         }

        return Service.START_REDELIVER_INTENT;

    }

}
