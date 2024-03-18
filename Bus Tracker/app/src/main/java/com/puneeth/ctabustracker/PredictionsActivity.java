package com.puneeth.ctabustracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.puneeth.ctabustracker.databinding.ActivityPredictionsBinding;
import com.puneeth.ctabustracker.databinding.ActivityStopsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PredictionsActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "PredictionsActivity";

    private ActivityPredictionsBinding binding;

    private Route currentRoute;
    private Stop currentStop;
    private Location currentLocation;
    private Intent intent;
    String direction;

    private int idx;
    ArrayList<Predictions> predictionsList=new ArrayList<>();
    int ctr=0;

    private PredictionsAdapter adapter;

    Vehicle vehicle;

    private AdView adView;
    private static final String adUnitId = "ca-app-pub-3940256099942544/6300978111";
    public static int nightModeFlags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent=getIntent();

        currentRoute=(Route) intent.getSerializableExtra("Route");
        currentStop=intent.getParcelableExtra("Stop");
        if(currentStop==null){
            Log.d(TAG, "onCreate: Stop object is null");
        }
        currentLocation=intent.getParcelableExtra("Location");
        direction=intent.getStringExtra("Direction");

        adapter=new PredictionsAdapter(this,predictionsList);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        PredictionsVolley.downloadRoutesDirections(this,currentRoute.getRouteId(),currentStop.getStpId());
    //+predictionsList.get(0).getRtdir()+
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");

        // Get the current date and time
        String currentTime = sdf.format(new Date());
        binding.stopAndTime.setText(currentStop.getStpName()+"("+direction+")\n"+currentTime);
        int col= Color.parseColor(currentRoute.getRouteColor());
        binding.stopAndTime.setBackgroundColor(col);
        binding.recycler.setBackgroundColor(col);

        binding.swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PredictionsVolley.downloadRoutesDirections(PredictionsActivity.this,currentRoute.getRouteId(),currentStop.getStpId());
                String ct = sdf.format(new Date());
                binding.stopAndTime.setText(currentStop.getStpName()+"("+direction+")\n"+ct);
                binding.swiper.setRefreshing(false); // This stops the busy-circle
            }
        });

        MobileAds.initialize(this, initializationStatus ->
                Log.d(TAG, "onInitializationComplete:"));

        adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        binding.adViewContainer.addView(adView);
        binding.adViewContainer.post(this::loadAdaptiveBanner);

        nightModeFlags =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    protected void acceptPredictions(ArrayList<Predictions> prdList)
    {
        predictionsList.clear();
        predictionsList.addAll(prdList);
        if(predictionsList.isEmpty())
        {
            Log.d(TAG, "acceptPredictions: no buses");
            //show an alert box
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("No Buses Near you");
            alertDialogBuilder.setMessage(
                    "Found no buses at this Stop right now");
            alertDialogBuilder.setPositiveButton("OK", null);
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                alertDialogBuilder.setIcon(R.drawable.bus_icon);
            }
            else{
                alertDialogBuilder.setIcon(R.drawable.bus_icon_black);
            }

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);

            alertDialog.show();
        }
        else{
            for(Predictions s: predictionsList)
            {
                Log.d(TAG, "acceptStops: "+s.getVid());
            }
            adapter.notifyItemRangeChanged(0, predictionsList.size());
        }

    }

    protected  void acceptFailPredictions(String str)
    {
        Log.d(TAG, "acceptFailPredictions: "+str);
    }

    @Override
    public void onClick(View v) {
        idx=binding.recycler.getChildLayoutPosition(v);
        String vid=predictionsList.get(idx).getVid();
        PredictionsVolley.downloadVehicles(this,vid);

    }

    public void acceptVehicle(Vehicle v)
    {
        vehicle=v;
        LatLng vehicleCoordinates=new LatLng(Double.parseDouble(v.getLat()),Double.parseDouble(v.getLng()));
        LatLng deviceCoordinates=new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        double dis= Math.round(SphericalUtil.computeDistanceBetween(deviceCoordinates, vehicleCoordinates));

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bus #"+vehicle.getVid());
        if(dis<1000.0)
        {
            alertDialogBuilder.setMessage(
                    "Bus #"+vehicle.getVid()+" is "+dis+" m ("+getVehicleTime()+" min) away from "+currentStop.getStpName()+" stop");
        }else{
            dis=dis/1000.0;
            alertDialogBuilder.setMessage(
                    "Bus #"+vehicle.getVid()+" is "+Math.round(dis)+" km ("+getVehicleTime()+" min) away from "+currentStop.getStpName()+" stop.");
        }

        alertDialogBuilder.setPositiveButton("OK", (arg0, arg1) ->
                System.currentTimeMillis());
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            Log.d(TAG, "acceptVehicle: night mode triggered");
            alertDialogBuilder.setIcon(R.drawable.bus_icon);
        }else{
            alertDialogBuilder.setIcon(R.drawable.bus_icon_black);
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

    public void acceptFailVehicle(String str){
        Log.d(TAG, "acceptFailVehicle: "+str);
    }

    public String getVehicleTime(){

        String vid=predictionsList.get(idx).getPrdctdn();
        return vid;
    }

    //adds-------------------------------------------------------------------------------------

    private void loadAdaptiveBanner() {

        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        adView.setAdListener(new PredictionsActivity.BannerAdListener());

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float adWidthPixels = adView.getWidth();

        // If the ad hasn't been laid out, default to the full screen width.
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels;// * 0.75f;
        }

        float density = getResources().getDisplayMetrics().density;
        int adWidth = (int) (adWidthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);

    }

    //////

    class BannerAdListener extends AdListener {

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            Log.d(TAG, "onAdClosed: ");
        }

        @Override
        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            super.onAdFailedToLoad(loadAdError);
            Log.d(TAG, "onAdFailedToLoad: " + loadAdError);
            Toast.makeText(PredictionsActivity.this,
                    loadAdError.getMessage() + " (Code: " + loadAdError.getCode() + ")",
                    Toast.LENGTH_LONG).show();

        }

        @Override
        public void onAdOpened() {
            super.onAdOpened();
            Log.d(TAG, "onAdOpened: ");
        }

        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            Log.d(TAG, "onAdLoaded: ");
        }

        @Override
        public void onAdClicked() {
            super.onAdClicked();
            Log.d(TAG, "onAdClicked: ");
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            Log.d(TAG, "onAdImpression: ");
        }
    }


}