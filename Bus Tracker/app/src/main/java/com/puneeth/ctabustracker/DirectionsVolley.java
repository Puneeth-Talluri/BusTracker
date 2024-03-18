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

public class DirectionsVolley {

    private static final String directionsUrl="https://www.ctabustracker.com/bustime/api/v2/getdirections";

    private static final String TAG = "DirectionsVolley";

    private static final ArrayList<String> dirList=new ArrayList<>();

    public static void downloadRoutesDirections(MainActivity mainActivityIn,String rid) {

        RequestQueue queue = Volley.newRequestQueue(mainActivityIn);

        Uri.Builder buildURL = Uri.parse(directionsUrl).buildUpon();
        buildURL.appendQueryParameter("key", "bWVLMusbi3iNKvWaA5kvACkCu");
        buildURL.appendQueryParameter("format", "json");
        buildURL.appendQueryParameter("rt",rid);
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccessDirections(response.toString(), mainActivityIn);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFailDirections(error1, mainActivityIn);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccessDirections(String responseText,
                                                MainActivity mainActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);
        Log.d(TAG, "handleSuccessDirections: invoked");
        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray directions = jsonObject.getJSONArray("directions");
        for (int i = 0; i < directions.length(); i++) {
            JSONObject temp = directions.getJSONObject(i);
            String dir = temp.getString("dir");

            dirList.add(dir);

        }


        mainActivity.runOnUiThread(() -> mainActivity.acceptDirections(dirList));
        dirList.clear();
    }

    private static void handleFailDirections(VolleyError ve, MainActivity mainActivity) {
        mainActivity.runOnUiThread(() -> mainActivity.acceptFailDirections(ve.getClass().getSimpleName()));
    }
}
