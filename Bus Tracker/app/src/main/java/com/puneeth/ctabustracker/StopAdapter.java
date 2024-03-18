package com.puneeth.ctabustracker;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.puneeth.ctabustracker.databinding.RouteEntryBinding;
import com.puneeth.ctabustracker.databinding.StopEntryBinding;

import java.util.ArrayList;

public class StopAdapter extends RecyclerView.Adapter<StopAdapter.StopViewHolder>{

    private final StopsActivity stopsActivity;

    private final ArrayList<Stop> allStops;
    private Location location;

    public StopAdapter(StopsActivity stopsActivity, ArrayList<Stop> allStops,Location loc) {
        this.stopsActivity = stopsActivity;
        this.allStops = allStops;
        this.location=loc;
    }

    @NonNull
    @Override
    public StopAdapter.StopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        StopEntryBinding bind=  StopEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        bind.getRoot().setOnClickListener(stopsActivity);
        return new StopAdapter.StopViewHolder(bind);
    }

    @Override
    public void onBindViewHolder(@NonNull StopAdapter.StopViewHolder holder, int position) {
        Stop r=allStops.get(position);
        //setting stop name
        holder.binding.stopName.setText(r.getStpName());
        LatLng stopLocation=r.getLocation();//getting coordinates of the stop
        LatLng current=new LatLng(location.getLatitude(),location.getLongitude());//getting device coordinates
//
        Double d= SphericalUtil.computeDistanceBetween(current, stopLocation);
        Long dis=Math.round(d);
        double delta = 22.5;
        double heading = SphericalUtil.computeHeading(current, stopLocation);
        if ((heading >= 0 && heading < delta) || (heading < 0 && heading >= -delta)) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" north of your location");
        } else if (heading >= delta && heading < 90 - delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" northeast of your location");
        } else if (heading >= 90 - delta && heading < 90 + delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" east of your location");
        } else if (heading >= 90 + delta && heading < 180 - delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" southeast of your location");
        } else if (heading >= 180 - delta || heading <= -180 + delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" south of your location");
        } else if (heading >= -180 + delta && heading < -90 - delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" southwest of your location");
        } else if (heading >= -90 - delta && heading < -90 + delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" west of your location");
        } else if (heading >= -90 + delta && heading < -delta) {
            holder.binding.distanceAway.setText(dis.toString()+"m"+" northwest of your location");
        }


    }

    @Override
    public int getItemCount() {
        return allStops.size();
    }

    static class StopViewHolder extends RecyclerView.ViewHolder{

        StopEntryBinding binding;

        public StopViewHolder( StopEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String getDir(float degIn) {

        int deg = 45 * (Math.round(degIn/45));
        switch (deg) {
            case 0:
                return "north";
            case 45:
                return "north-east";
            case 90:
                return "east";
            case 135:
                return "south-east";
            case 180:
                return "south";
            case 225:
                return "south-west";
            case 270:
                return "west";
            case 315:
                return "north-west";
        }
        return null;
    }
}
