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

public class RoutesVolley {

    private static final String vehiclesUrl =
            "https://www.ctabustracker.com/bustime/api/v2/getroutes";

    private static final String directionsUrl="https://www.ctabustracker.com/bustime/api/v2/getdirections";
    private static final String TAG = "RoutesVolley";

    private static final ArrayList<Route> allRoutes=new ArrayList<>();

    private static final ArrayList<String> dirList=new ArrayList<>();

    public static void downloadRoutes(MainActivity mainActivityIn) {

        RequestQueue queue = Volley.newRequestQueue(mainActivityIn);

        Uri.Builder buildURL = Uri.parse(vehiclesUrl).buildUpon();
        buildURL.appendQueryParameter("key", "bWVLMusbi3iNKvWaA5kvACkCu");
        buildURL.appendQueryParameter("format", "json");
        String urlToUse = buildURL.build().toString();

        Response.Listener<JSONObject> listener = response -> {
            try {
                handleSuccess(response.toString(), mainActivityIn);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        };

        Response.ErrorListener error = error1 -> handleFail(error1, mainActivityIn);

        // Request a string response from the provided URL.
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, urlToUse,
                        null, listener, error);

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    private static void handleSuccess(String responseText,
                                      MainActivity mainActivity) throws JSONException {
        JSONObject response = new JSONObject(responseText);

        JSONObject jsonObject = response.getJSONObject("bustime-response");
        JSONArray routes = jsonObject.getJSONArray("routes");
        for (int i = 0; i < routes.length(); i++) {
            JSONObject route = routes.getJSONObject(i);
            String rNum = route.getString("rt");
            String rName = route.getString("rtnm");
            String rColor = route.getString("rtclr");

            Route r=new Route(rNum,rName,rColor);

            allRoutes.add(r);

            // Here I would make an object to example purposes I am not.
        }


        mainActivity.runOnUiThread(() -> mainActivity.acceptRoutes(allRoutes));
    }

    private static void handleFail(VolleyError ve, MainActivity mainActivity) {
        mainActivity.runOnUiThread(() -> mainActivity.acceptFail(ve.getClass().getSimpleName()));
    }

//directions volley part-------------------------------------------------------------------------------------------------------------------



}
