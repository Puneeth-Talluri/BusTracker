package com.puneeth.ctabustracker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.puneeth.ctabustracker.databinding.RouteEntryBinding;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {

    private final MainActivity mainActivity;

    private final ArrayList<Route> displayedRoutes;

    public RouteAdapter(MainActivity mainActivity, ArrayList<Route> displayedRoutes){
        this.displayedRoutes=displayedRoutes;
        this.mainActivity=mainActivity;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RouteEntryBinding bind=  RouteEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        bind.getRoot().setOnClickListener(mainActivity);
        return new RouteViewHolder(bind);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route r=displayedRoutes.get(position);
        holder.binding.routeId.setText(r.getRouteId());
        holder.binding.routeName.setText(r.routeName);
        int col= Color.parseColor(r.getRouteColor());
        holder.binding.getRoot().setBackgroundColor(col);
        if(isColorDark(col))
        {
            holder.binding.routeName.setTextColor(Color.WHITE);
        }
        else{
            holder.binding.routeName.setTextColor(Color.BLACK);
        }

    }

    @Override
    public int getItemCount() {
        return displayedRoutes.size();
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5; // It means it's a dark color
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder{

        RouteEntryBinding binding;

        RouteViewHolder(RouteEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
