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
            convertView = inflater.inflate(R.layout.item_report, null);
            holder = new ViewHolder();
            holder.countSuccessTV = (TextView) convertView.findViewById(R.id.countSuccessPushes);
            holder.countSendedTV = (TextView) convertView.findViewById(R.id.countSendedPushes);
            holder.statusOvalTV = (TextView) convertView.findViewById(R.id.statusOvalTV);
            holder.serverTitleTV = (TextView) convertView.findViewById(R.id.serverTitleTV);
            holder.deliveryTimeTV = (TextView) convertView.findViewById(R.id.deliveryTimeTV);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Report report = listReports.get(position);

        if (report != null) {
            if (report.isCurrentItem()){
                convertView.setBackgroundResource(Consts.TEST_ITEM_BACKGROUND_COLOR);
            } else {
                convertView.setBackgroundResource(Consts.NORMAL_ITEM_BACKGROUND_COLOR);
            }

            holder.countSuccessTV.setText(String.valueOf(report.getSuccessPushes()));
            holder.countSendedTV.setText(String.valueOf(report.getSendedPushes()));
            holder.statusOvalTV.setBackgroundResource(report.getColorStatusOval());
            holder.serverTitleTV.setText(report.getServerTitle());
            holder.deliveryTimeTV.setText(report.getDeliveryDateLastPush());
            holder.setViewTag(position);
        }

        return convertView;
    }

    public static class ViewHolder {
        int viewTag;
        TextView countSuccessTV;
        TextView countSendedTV;
        TextView serverTitleTV;
        TextView statusOvalTV;
        TextView deliveryTimeTV;

        public int getViewTag() {
            return viewTag;
        }

        public void setViewTag(int viewTag) {
            this.viewTag = viewTag;
        }
    }
}
