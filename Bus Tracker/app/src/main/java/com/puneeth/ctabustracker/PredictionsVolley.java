package com.puneeth.ctabustracker;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PredictionsVolley {

    private static final String predictionsUrl ="https://www.ctabustracker.com/bustime/api/v2/getpredictions";

    private static final String vehiclesUrl="https://www.ctabustracker.com/bustime/api/v2/getvehicles";

    private static final String TAG = "PredictionsVolley";

    private static final ArrayList<Predictions> predictionsList=new ArrayList<>();

    private static Vehicle v;

    public static void downloadRoutesDirections(PredictionsActivity predictionsActivity,String rid,String stpid) {

        RequestQueue queue = Volley.newRequestQueue(predictionsActivity);

        Uri.Builder buildURL = Uri.parse(predictionsUrl).buildUpon();
        buildURL.appendQueryParameter("key", "bWVLMusbi3iNKvWaA5kvACkCu");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt",rid);
        buildURL.appendQueryParameter("stpid",stpid);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccessDirections(response.toString(), predictionsActivity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFailDirections(error1, predictionsActivity);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccessDirections(String responseText,
                                                PredictionsActivity predictionsActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        Log.d(TAG, "handleSuccessDirections: invoked");
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray directions = jsonObject.getJSONArray("prd");
        Log.d(TAG, "handleSuccessDirections: loaded "+directions.length()+" Predictions");
        for (int i = 0; i < directions.length(); i++) {
            JSONObject temp = directions.getJSONObject(i);
            String vid = temp.getString("vid");
            String rtdir = temp.getString("rtdir");
            String des = temp.getString("des");
            String prdtm = temp.getString("prdtm");
            String dly = temp.getString("dly");
            boolean dlys=false;
            if(dly.equalsIgnoreCase("false"))
            {
                dlys=false;
            }
            else{
                dlys=true;
            }
            String prdctdn = temp.getString("prdctdn");
            Predictions prd=new Predictions(vid,rtdir,des,prdtm,dlys,prdctdn);
            predictionsList.add(prd);
           // dirList.add(dir);

        }


        predictionsActivity.runOnUiThread(() -> predictionsActivity.acceptPredictions(predictionsList));
        predictionsList.clear();
    }

    private static void handleFailDirections(VolleyError ve, PredictionsActivity predictionsActivity) {
        predictionsActivity.runOnUiThread(() -> predictionsActivity.acceptFailPredictions(ve.getClass().getSimpleName()));
    }

    //downloading vehicle data-----------------------------------------------------------------------------------------------
    public static void downloadVehicles(PredictionsActivity predictionsActivity,String vid) {

        RequestQueue queue = Volley.newRequestQueue(predictionsActivity);

        Uri.Builder buildURL = Uri.parse(vehiclesUrl).buildUpon();
        buildURL.appendQueryParameter("key", "bWVLMusbi3iNKvWaA5kvACkCu");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("vid",vid);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccessVehicles(response.toString(), predictionsActivity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFailVehicles(error1, predictionsActivity);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccessVehicles(String responseText,
                                                PredictionsActivity predictionsActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        Log.d(TAG, "handleSuccessDirections: invoked");
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray vehicle = jsonObject.getJSONArray("vehicle");
        Log.d(TAG, "handleSuccessDirections: loaded vehicle");
        for (int i = 0; i < vehicle.length(); i++) {
            JSONObject temp = vehicle.getJSONObject(i);
            String vid=temp.getString("vid");
            String lat = temp.getString("lat");
            String lon = temp.getString("lon");

           v=new Vehicle(vid,lat,lon);


        }


        predictionsActivity.runOnUiThread(() -> predictionsActivity.acceptVehicle(v));
    }

    private static void handleFailVehicles(VolleyError ve, PredictionsActivity predictionsActivity) {
        predictionsActivity.runOnUiThread(() -> predictionsActivity.acceptFailVehicle(ve.getClass().getSimpleName()));
    }
}
