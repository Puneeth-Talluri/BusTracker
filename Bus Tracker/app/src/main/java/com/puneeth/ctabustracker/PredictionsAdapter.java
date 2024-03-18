package com.puneeth.ctabustracker;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.puneeth.ctabustracker.databinding.PredictionsEntryBinding;
import com.puneeth.ctabustracker.databinding.RouteEntryBinding;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class PredictionsAdapter extends RecyclerView.Adapter<PredictionsAdapter.PredictionsViewHolder>{

    private final PredictionsActivity predictionsActivity;

    private final ArrayList<Predictions> allPredictions;

    public PredictionsAdapter(PredictionsActivity predictionsActivity, ArrayList<Predictions> allPredictions) {
        this.predictionsActivity = predictionsActivity;
        this.allPredictions = allPredictions;
    }

    @NonNull
    @Override
    public PredictionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PredictionsEntryBinding bind=  PredictionsEntryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        bind.getRoot().setOnClickListener(predictionsActivity);
        return new PredictionsViewHolder(bind);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionsViewHolder holder, int position) {

        Predictions pred=allPredictions.get(position);
        holder.binding.busNumber.setText(pred.vid);
        holder.binding.routeDir.setText(pred.rtdir+" to "+pred.des);
        if(pred.prdctdn.equalsIgnoreCase("due"))
        {
            holder.binding.dueText.setText(pred.prdctdn);
        }
        else if(pred.prdctdn.equalsIgnoreCase("dly")){
            holder.binding.dueText.setText("Delayed");
        }
        else{
            holder.binding.dueText.setText("due in "+pred.prdctdn+" mins at");
        }
        String dueTime=pred.getPrdtm().substring(9);

        // Parse the string to a LocalTime object
        LocalTime time = LocalTime.parse(dueTime, DateTimeFormatter.ofPattern("HH:mm"));

        // Format the LocalTime object to a string in 12-hour format with AM/PM
        String timeIn12HourFormat = time.format(DateTimeFormatter.ofPattern("hh:mm a"));

        holder.binding.timeText.setText(timeIn12HourFormat.toString());

    }

    @Override
    public int getItemCount() {
        return allPredictions.size();
    }


    static class PredictionsViewHolder extends RecyclerView.ViewHolder{

        PredictionsEntryBinding binding;

        public PredictionsViewHolder( PredictionsEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
