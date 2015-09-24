package com.quickblox.sample.gcmchecker.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.models.ResultTests;

import java.util.ArrayList;

/**
 * Created by tereha on 24.09.15.
 */
public class TestResultsAdapter extends BaseAdapter {
    private ArrayList<ResultTests> resultsSelectedServer;
    private LayoutInflater inflater;

    public TestResultsAdapter(Context context, ArrayList<ResultTests> resultsSelectedServer) {
        this.inflater = LayoutInflater.from(context);
        this.resultsSelectedServer = resultsSelectedServer;
    }

    @Override
    public int getCount() {
        return resultsSelectedServer.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_result_test, null);
            holder = new ViewHolder();
            holder.timeErrorTV = (TextView) convertView.findViewById(R.id.timeErrorTV);
            holder.textErrorTV = (TextView) convertView.findViewById(R.id.textErrorTV);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ResultTests resultTests = resultsSelectedServer.get(position);


        if (resultTests != null) {
            holder.timeErrorTV.setText(resultTests.getErrorTime());
            holder.textErrorTV.setText(resultTests.getErrorText());
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView timeErrorTV;
        TextView textErrorTV;
    }
}