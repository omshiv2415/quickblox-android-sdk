package com.quickblox.sample.gcmchecker.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.sample.gcmchecker.R;
import com.quickblox.sample.gcmchecker.main.models.Report;

import java.util.ArrayList;


/**
 * Created by tereha on 15.09.15.
 */
public class ReportAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Report> listReports;
    private LayoutInflater inflater;

    public ReportAdapter (Context context, ArrayList<Report> listReports){
        this.inflater = LayoutInflater.from(context);
        this.listReports = listReports;
    }

    @Override
    public int getCount() {
        return listReports.size();
    }

    @Override
    public Report getItem(int position) {
        return listReports.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_time_report, null);
            holder = new ViewHolder();
            holder.dateSendTV = (TextView) convertView.findViewById(R.id.sendTimeTV);
            holder.currentDateTV = (TextView) convertView.findViewById(R.id.deliveryTimeTV);
            holder.travelingDateTV = (TextView) convertView.findViewById(R.id.trevelingTimeTV);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Report report = listReports.get(position);


        if (report != null) {
            int travelingTimeSecond = Integer.parseInt(report.getTravelingTime());
            holder.dateSendTV.setText(report.getSendTime());
            holder.currentDateTV.setText(report.getDeliveryTime());
            holder.travelingDateTV.setText(report.getTravelingTime());

            if (travelingTimeSecond > Consts.PUSH_TIMEOUT/1000){
//                convertView.setBackgroundResource(R.color.);
            } else {
//                convertView.setBackgroundResource(R.color.green_light);
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView dateSendTV;
        TextView currentDateTV;
        TextView travelingDateTV;
    }
}
