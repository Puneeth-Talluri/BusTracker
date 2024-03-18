package com.puneeth.ctabustracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.puneeth.ctabustracker.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private static final String TAG = "MainActivity";

    private static final int LOCATION_REQUEST=111;

    private ActivityMainBinding binding;

    private RouteAdapter routeAdapter;

    private boolean keepOn = true;
    private static final long minSplashTime = 2000;
    private long startTime;

    private static final int PERMISSION_REQUEST = 111;

    private int idx;

    private View selectedView;

    private ArrayList<Route> allRoutes=new ArrayList<>();
    private ArrayList<Route> displayedRoutes=new ArrayList<>();

    private FusedLocationProviderClient mFusedLocationClient;

    public static Location locationCurrent;

    private AdView adView;
    private static final String adUnitId = "ca-app-pub-3940256099942544/6300978111";// mine-"ca-app-pub-2970649429918875/7759466679";  test-"ca-app-pub-3940256099942544/6300978111"
    public int nightModeFlags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        startTime = System.currentTimeMillis();
        //---adapter settings-----------------------------------------------------------
        routeAdapter=new RouteAdapter(this,displayedRoutes);
        binding.recycler.setAdapter(routeAdapter);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));

        if(hasNetworkConnection()){
            RoutesVolley.downloadRoutes(this);
        }
        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {
                                //Log.d(TAG, "shouldKeepOnScreen: " + (System.currentTimeMillis() - startTime));
                                return keepOn||(System.currentTimeMillis() - startTime <= minSplashTime);
                            }
                        }
                );
        setContentView(binding.getRoot());

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {

                ArrayList<Route> temp = new ArrayList<>();
                for (Route s : allRoutes) {
                    if (s.getRouteName().toLowerCase().contains(editable.toString().toLowerCase()) || s.getRouteId().toLowerCase().contains(editable.toString().toLowerCase()))
                        temp.add(s);
                }
                int size = allRoutes.size();
                displayedRoutes.clear();
                routeAdapter.notifyItemRangeRemoved(0, size);

                displayedRoutes.addAll(temp);
                routeAdapter.notifyItemRangeChanged(0, displayedRoutes.size());
            }
        });
        checkNotificationPerm();

        //adds
        MobileAds.initialize(this, initializationStatus ->
                Log.d(TAG, "onInitializationComplete:"));

        adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        binding.adViewContainer.addView(adView);
        binding.adViewContainer.post(this::loadAdaptiveBanner);

        Toolbar myToolBar=(Toolbar) binding.toolbar;
        myToolBar.setTitleTextColor((Color.WHITE));
        Objects.requireNonNull((myToolBar.getOverflowIcon())).setTint(Color.WHITE);
        setSupportActionBar(myToolBar);


        nightModeFlags =
                getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;



    }
    
    // Location permission-------------------------------------------

    protected void checkNotificationPerm() {
        // Only needed for Android Tiramisu and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            // Check perm - if not then start the  request and return
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);

            } else {
                Log.d(TAG, "checkNotificationPerm: Permssion granted");
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            // Got last known location. In some situations this can be null.
                            if (location != null) {
                                locationCurrent = location;

                            }
                        })
                        .addOnFailureListener(this, e -> System.exit(0));
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: permission granted");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                showRationaleAlert();
            } else {
                //showTryAgainAlert();
                System.exit(0);
            }
        }
    }

    private void showRationaleAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Fine Accuracy Needed");
        alertDialogBuilder.setMessage(
                "This application needs Fine Accuracy permission in order to determine the closest bus stops to your location.It will not function properly without it.Will you allow it?");
        alertDialogBuilder.setPositiveButton("Yes", (arg0, arg1) ->
                checkNotificationPerm());
        alertDialogBuilder.setNegativeButton("No Thanks", (dialog, which) -> showTryAgainAlert());
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


    private void showTryAgainAlert() {
        Log.d(TAG, "showTryAgainAlert: permission denied at last");
        System.exit(0);
    }

    //----------------routes volley----------------------------------------------------

    protected void acceptRoutes(ArrayList<Route> receivedRoutes){
        allRoutes=receivedRoutes;
        displayedRoutes.addAll(allRoutes);
        routeAdapter.notifyItemRangeChanged(0, displayedRoutes.size());
        keepOn=false;
    }


    protected void acceptFail(String str){
        Log.d(TAG, "acceptFail: "+str);
    }


    protected void acceptDirections(ArrayList<String> dirList){
        Log.d(TAG, "acceptDirections: invoked"+dirList.get(0)+" "+dirList.get(1));
        Route temp=displayedRoutes.get(idx);
        temp.setDirections(dirList);
       // Toast.makeText(this,dirList.get(0)+" "+dirList.get(1),Toast.LENGTH_LONG).show();

        buildPopup(temp);

    }

    protected void acceptFailDirections(String str){
        Log.d(TAG, "acceptFail: Directions not downloaded");
    }

    @Override
    public void onClick(View v) {
        idx=binding.recycler.getChildLayoutPosition(v);
        Route temp=displayedRoutes.get(idx);
        //Toast.makeText(this,temp.getRouteName(),Toast.LENGTH_SHORT).show();
        DirectionsVolley.downloadRoutesDirections(this,temp.routeId);
        selectedView=v;
    }

    private void buildPopup(Route routeObj) {
        PopupMenu popupMenu = new PopupMenu(this, selectedView);
        int tempId=1;
        ArrayList<String> tempList=routeObj.getDirections();
        for(String s:tempList){
            popupMenu.getMenu().add(Menu.NONE,tempId,Menu.NONE,s);
            tempId++;
        }
        determineLocation();

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int selectedId = menuItem.getItemId();
//            String t=menuItem.getTitle().toString();
//            Log.d(TAG, "buildPopup: "+t);
           // LatLng currentLoc=new LatLng(locationCurrent.getLatitude(),locationCurrent.getLongitude());
            Intent intent=new Intent(MainActivity.this,StopsActivity.class);
            intent.putExtra("Route", routeObj);
            intent.putExtra("dir",menuItem.getTitle().toString());
            if (locationCurrent != null) {
                intent.putExtra("Location",locationCurrent);
                startActivity(intent);
            } else {
                showNullLocationAlert();
                Log.d(TAG, "buildPopup: location is null");
            }


            return true;
        });
        popupMenu.show();
    }

    private void determineLocation() {

        if(hasNetworkConnection()){
            Log.d(TAG, "determineLocation: is triggered");
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
                return;
            }
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some situations this can be null.
                        if (location != null) {
                            locationCurrent=location;

                        }
                    })
                    .addOnFailureListener(this, e ->
                            Log.d(TAG, "determineLocation: location not found"));
        }
        else{

        }

    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
            return true;
        }
        else{
            showNetworkDialog();
            return false;
        }
    }

    //adds---------------------------------------------
    private void loadAdaptiveBanner() {

        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        adView.setAdListener(new BannerAdListener());

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
            Toast.makeText(MainActivity.this,
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        Log.d(TAG, "onCreateOptionsMenu: tool bar inflated");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.infoMenuItem) {
            showCtaInfo();
            return true;
        }
        // Handle other menu items if needed
        return super.onOptionsItemSelected(item);
    }

    public void showCtaInfo() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bus Tracker - CTA");

        String message = "CTA Bus Tracker data provided by Chicago Transit Authority\nhttps://www.transitchicago.com/developers/bustracker/";

        final TextView messageView = new TextView(this);
        messageView.setText(message);
        messageView.setGravity(Gravity.CENTER_HORIZONTAL);
        messageView.setPadding(10, 10, 10, 10);
        messageView.setTextSize(16f);


        Linkify.addLinks(messageView, Linkify.WEB_URLS);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        builder.setIcon(R.drawable.ic_launcher_round);
        builder.setView(messageView);

        // Add OK button
        builder.setPositiveButton("OK", null);

        // Show the AlertDialog
        builder.show();
    }

    public void showNullLocationAlert(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bus Tracker - CTA");
        alertDialogBuilder.setMessage(
                "Unable to determine device location. if this is an emulator, please set a location.");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

    public void showNetworkDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Bus Tracker - CTA");
        alertDialogBuilder.setMessage(
                "Unable to contact Bus Tracker API due to network problem. Please check your network connection");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });


        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();
    }

}