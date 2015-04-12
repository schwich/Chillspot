package com.jordanschwichtenberg.chillspot;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by Jordan on 4/11/2015.
 */
public class EventAdapter extends CursorAdapter {

    public EventAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_event, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String subCategory = cursor.getString(EventListFragment.COL_EVENT_SUB_CATEGORY);
        viewHolder.subCategoryView.setText(subCategory);

        String category = cursor.getString(EventListFragment.COL_EVENT_CATEGORY);
        viewHolder.categoryView.setText(category);

        double distance = cursor.getDouble(EventListFragment.COL_EVENT_DISTANCE);
        viewHolder.distanceView.setText(Double.toString(distance));

        // TODO: add attending count


    }

    /**
     * Cache of the children view for an event list item
     */
    public static class ViewHolder {
        public final TextView subCategoryView;
        public final TextView categoryView;
        public final TextView distanceView;
        public final TextView attendingView;

        public ViewHolder(View view) {
            subCategoryView = (TextView) view.findViewById(R.id.list_item_sub_category_textview);
            categoryView = (TextView) view.findViewById(R.id.list_item_category_textview);
            distanceView = (TextView) view.findViewById(R.id.list_item_distance_textview);
            attendingView = (TextView) view.findViewById(R.id.list_item_attending_textview);
        }
    }
}

