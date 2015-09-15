package com.quickblox.simplesample.messages.main;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.quickblox.simplesample.messages.R;
import com.quickblox.simplesample.messages.main.models.Report;
import com.quickblox.users.model.QBUser;

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
                convertView.setBackgroundResource(R.color.red_light);
            } else {
                convertView.setBackgroundResource(R.color.green_light);
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
