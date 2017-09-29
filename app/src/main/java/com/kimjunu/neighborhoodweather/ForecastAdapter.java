package com.kimjunu.neighborhoodweather;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    ArrayList<SimpleForecastInfo> mDataset = new ArrayList<>();

    @Override
    public ForecastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ForecastAdapter.ViewHolder holder, int position) {
        holder.tvForeTime.setText(mDataset.get(position).time);
        holder.tvForeSky.setText(mDataset.get(position).sky);
        holder.tvForeTemp.setText(mDataset.get(position).temperature);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setDataset(ArrayList<SimpleForecastInfo> dataset) {
        this.mDataset = dataset;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvForeSky)
        TextView tvForeSky;
        @BindView(R.id.tvForeTemp)
        TextView tvForeTemp;
        @BindView(R.id.tvForeTime)
        TextView tvForeTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
