package com.quickblox.sample.groupchatwebrtc.util;

import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tereha on 15.04.16.
 */
public class OpponentsManager {

    private String TAG = OpponentsManager.class.getSimpleName();


    public static ArrayList<QBUser> currentUsersList = new ArrayList<>();

    public static ArrayList<QBUser> getCurrentUsersList() {
        return currentUsersList;
    }

    public void setCurrentUsersList(ArrayList<QBUser> currentUsersList) {
        this.currentUsersList = currentUsersList;
    }

    public ArrayList<QBUser> loadUsers(){

        return null;

    }

    public ArrayList<QBUser> loadUsersByTag(String tag){
        final ArrayList<QBUser> qbUsers = new ArrayList<>();
        final Bundle bundle = new Bundle();


        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        final List<String> tags = new LinkedList<>();
        tags.add(tag);

        try {
            qbUsers.addAll(QBUsers.getUsersByTags(tags, requestBuilder, bundle));
        } catch (QBResponseException e) {
            e.printStackTrace();
        }

        return qbUsers;
    }

    public static String getUserNameByID(Integer callerID) {
        for (QBUser user : getCurrentUsersList()) {
            if (user.getId().equals(callerID)) {
                return user.getFullName();
            }
        }
        return callerID.toString();
    }

    public static int getUserIndexByID(Integer callerID) {
        for (QBUser user : getCurrentUsersList()) {
            if (user.getId().equals(callerID)) {
                return currentUsersList.indexOf(user);
            }
        }
        return -1;
    }



}
