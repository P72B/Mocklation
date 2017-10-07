package de.p72b.mocklation.main;


import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.p72b.mocklation.R;
import de.p72b.mocklation.service.database.LocationItem;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {
    private static final String TAG = LocationListAdapter.class.getSimpleName();
    private List<LocationItem> mDataset;
    private final View.OnClickListener mOnClickListener;
    private LocationItem mSelectedItem;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;
        public View mFlagView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.info_text);
            mFlagView = view.findViewById(R.id.item_flag);
        }

        public void flagVisibility(int visibility) {
            mFlagView.setVisibility(visibility);
        }
    }

    public LocationListAdapter(View.OnClickListener listener) {
        mDataset = new ArrayList<>();
        mOnClickListener = listener;
    }

    @Override
    public LocationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_location_fixed_mode_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocationItem locationItem = mDataset.get(position);

        holder.mTextView.setText(locationItem.code());
        if (mSelectedItem != null && locationItem.code().equals(mSelectedItem.code())) {
            holder.flagVisibility(View.VISIBLE);
        } else {
            holder.flagVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public LocationItem getItemAt(int position) {
        return mDataset.get(position);
    }

    public void setData(List<LocationItem> items) {
        Log.d(TAG, "LocationItems list size: " + items.size());
        mDataset = items;
        notifyDataSetChanged();
    }

    public void flagItem(LocationItem item) {
        mSelectedItem = item;
        notifyDataSetChanged();
    }
}
