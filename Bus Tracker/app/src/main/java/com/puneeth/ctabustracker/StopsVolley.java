package com.puneeth.ctabustracker;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StopsVolley {

    private static final String stopsUrl="https://www.ctabustracker.com/bustime/api/v2/getstops";

    private static final String TAG = "StopsVolley";

    private static final ArrayList<Stop> stopsList=new ArrayList<>();
    private static FusedLocationProviderClient mFusedLocationClient;
    private static Location receivedLoc;
    private static LatLng currentCoordinates;

    public static void downloadStops(StopsActivity stopsActivity,String rid,String dir, Location loc) {
        receivedLoc=loc;
        if (receivedLoc != null) {

            currentCoordinates=new LatLng(receivedLoc.getLatitude(),receivedLoc.getLongitude());
//            Log.d(TAG, "downloadStops: currentCoordinates initialized");
//            Log.d(TAG, "downloadStops: "+currentCoordinates.toString());
        }
        else{
            Log.d(TAG, "downloadStops: receivedLoc is null");

        }
        RequestQueue queue = Volley.newRequestQueue(stopsActivity);

        Uri.Builder buildURL = Uri.parse(stopsUrl).buildUpon();
        buildURL.appendQueryParameter("key", "bWVLMusbi3iNKvWaA5kvACkCu");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt",rid);
        buildURL.appendQueryParameter("dir",dir);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccess(response.toString(), stopsActivity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFail(error1, stopsActivity);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccess(String responseText,
                                                StopsActivity stopsActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);
       // Log.d(TAG, "handleSuccessDirections: invoked");
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray stops = jsonObject.getJSONArray("stops");
        Log.d(TAG, "handleSuccess: downloaded "+stops.length()+" Stops");
        for (int i = 0; i < stops.length(); i++) {
            JSONObject temp = stops.getJSONObject(i);
            String stpid = temp.getString("stpid");
            String stpnm= temp.getString("stpnm");
            double lat=Double.parseDouble(temp.getString("lat"));
            double lon=Double.parseDouble(temp.getString("lon"));

            LatLng l= new LatLng(lat,lon);
            Double dis=SphericalUtil.computeDistanceBetween(currentCoordinates, l);
            if(dis<1000.0){
                Stop s=new Stop(stpid,stpnm,l);

                stopsList.add(s);
            }


        }


        stopsActivity.runOnUiThread(() -> stopsActivity.acceptStops(stopsList));
        stopsList.clear();
    }

    private static void handleFail(VolleyError ve, StopsActivity stopsActivity) {
        stopsActivity.runOnUiThread(() -> stopsActivity.acceptFail(ve.getClass().getSimpleName()));
    }
}
