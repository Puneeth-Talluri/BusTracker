package com.puneeth.ctabustracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.puneeth.ctabustracker.databinding.ActivityMainBinding;
import com.puneeth.ctabustracker.databinding.ActivityStopsBinding;

import java.util.ArrayList;

public class StopsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "StopsActivity";

    private ActivityStopsBinding binding;
    private Intent intent;

    private Route receivedRoute;
    private String direction;
    private int idx;

    private StopAdapter adapter;

    private ArrayList<Stop> allStops=new ArrayList<>();

    private Location loc;

    private Stop temp;
    private AdView adView;
    private static final String adUnitId = "ca-app-pub-3940256099942544/6300978111";

    public int nightModeFlags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStopsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        intent=getIntent();
        receivedRoute=(Route)intent.getSerializableExtra("Route");
        direction=intent.getStringExtra("dir");
        loc=intent.getParcelableExtra("Location");
        binding.directionStop.setText(direction+" Stops");
        int col= Color.parseColor(receivedRoute.getRouteColor());
        binding.directionStop.setBackgroundColor(col);
        binding.recycler.setBackgroundColor(col);

        adapter=new StopAdapter(this,allStops,loc);
        binding.recycler.setAdapter(adapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        StopsVolley.downloadStops(this,receivedRoute.routeId,direction,loc);

        //adds
        MobileAds.initialize(this, initializationStatus ->
                Log.d(TAG, "onInitializationComplete:"));

        adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        binding.adViewContainer.addView(adView);
        binding.adViewContainer.post(this::loadAdaptiveBanner);

        nightModeFlags =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;


    }

    protected void acceptStops(ArrayList<Stop> stops){
        allStops.addAll(stops);
        if(stops.isEmpty())
        {
            //show an alert box
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("No Stops Near you");
            alertDialogBuilder.setMessage(
                    "Found no stops within 1km of your current location to this route");
            alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> finish());
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
            for(Stop s: stops)
            {
                Log.d(TAG, "acceptStops: "+s.getStpName());
            }
            adapter.notifyItemRangeChanged(0, allStops.size());
            //  routeAdapter.notifyItemRangeChanged(0, displayedRoutes.size());
        }


    }


    protected void acceptFail(String str){
        Log.d(TAG, "acceptFail: "+str);
    }

    @Override
    public void onClick(View v) {
        idx=binding.recycler.getChildLayoutPosition(v);
        temp= allStops.get(idx);
       Toast.makeText(this,temp.getStpName(),Toast.LENGTH_SHORT).show();

       Intent startPredictions=new Intent(this,PredictionsActivity.class);
       startPredictions.putExtra("Route",receivedRoute);
        startPredictions.putExtra("Stop",temp);
        startPredictions.putExtra("Location",loc);
        startPredictions.putExtra("Direction",direction);
       startActivity(startPredictions);
//        DirectionsVolley.downloadRoutesDirections(this,temp.routeId);
//        selectedView=v;
    }

    //adds-----------------------------------------------
    private void loadAdaptiveBanner() {

        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        adView.setAdListener(new StopsActivity.BannerAdListener());

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
            Toast.makeText(StopsActivity.this,
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

