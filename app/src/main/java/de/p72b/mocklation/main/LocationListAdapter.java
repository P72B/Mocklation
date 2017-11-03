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
import de.p72b.mocklation.service.room.LocationItem;

public class LocationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        SwipeAndTouchHelper.ActionCompletionContract {
    private static final String TAG = LocationListAdapter.class.getSimpleName();
    private List<LocationItem> mDataset;
    private final MainActivity.IAdapterListener mListener;
    private LocationItem mSelectedItem;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        View mFlagView;

        ViewHolder(View view) {
            super(view);
            mTextView = view.findViewById(R.id.info_text);
            mFlagView = view.findViewById(R.id.item_flag);
        }

        void flagVisibility(int visibility) {
            mFlagView.setVisibility(visibility);
        }
    }

    LocationListAdapter(MainActivity.IAdapterListener listener) {
        mDataset = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public LocationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_location_fixed_mode_item, parent, false);
        view.setOnClickListener(mListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LocationItem locationItem = mDataset.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.mTextView.setText(locationItem.getCode());
        if (mSelectedItem != null && locationItem.getCode().equals(mSelectedItem.getCode())) {
            viewHolder.flagVisibility(View.VISIBLE);
        } else {
            viewHolder.flagVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        // nothing to to
    }

    @Override
    public void onViewSwiped(int position) {
        LocationItem item = getItemAt(position);
        mDataset.remove(position);
        notifyItemRemoved(position);
        mListener.onItemRemoved(item);
    }

    LocationItem getItemAt(int position) {
        return mDataset.get(position);
    }

    public void setData(List<LocationItem> items) {
        Log.d(TAG, "LocationItems list size: " + items.size());
        mDataset = items;
        notifyDataSetChanged();
    }

    void flagItem(LocationItem item) {
        mSelectedItem = item;
        notifyDataSetChanged();
    }
}
