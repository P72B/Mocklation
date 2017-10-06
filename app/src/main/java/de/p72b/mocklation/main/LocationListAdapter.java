package de.p72b.mocklation.main;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.database.LocationItem;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {
    private List<LocationItem> mDataset;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.info_text);
        }
    }

    public LocationListAdapter() {
        mDataset = new ArrayList<>();
    }

    @Override
    public LocationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_location_fixed_mode_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocationItem locationItem = mDataset.get(position);

        holder.mTextView.setText(locationItem.code());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setData(List<LocationItem> items) {
        mDataset = items;
        notifyDataSetChanged();
    }
}
