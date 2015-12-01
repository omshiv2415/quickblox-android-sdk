package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

import vc908.stickerfactory.StickersManager;

public class DialogsAdapter extends BaseAdapter {
    private List<QBDialog> dialogs;
    private LayoutInflater inflater;

    public DialogsAdapter(Context ctx, List<QBDialog> dialogs) {
        this.dialogs = dialogs;
        this.inflater = LayoutInflater.from(ctx);
    }

    public List<QBDialog> getDialogs() {
        return dialogs;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return dialogs.get(position);
    }

    @Override
    public int getCount() {
        return dialogs.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_room, parent, false);

            holder = new ViewHolder();
            holder.nameTextView = (TextView) convertView.findViewById(R.id.roomName);
            holder.lastMessageTextView = (TextView) convertView.findViewById(R.id.lastMessage);
            holder.dialogTypeTextView = (TextView) convertView.findViewById(R.id.textViewGroupType);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        QBDialog dialog = dialogs.get(position);
        if (dialog.getType().equals(QBDialogType.GROUP)) {
            holder.nameTextView.setText(dialog.getName());
        } else {
            // get opponent name for private dialog
            //
            Integer opponentId = ChatHelper.getInstance().getOpponentIdForPrivateDialog(dialog);
            QBUser user = ChatHelper.getInstance().getDialogsUsersMap().get(opponentId);
            if (user != null) {
                holder.nameTextView.setText(user.getLogin() == null ? user.getFullName() : user.getLogin());
            }
        }

        String lastMessage = dialog.getLastMessage();
        if (!TextUtils.isEmpty(lastMessage) && StickersManager.isSticker(lastMessage)) {
            holder.lastMessageTextView.setText(R.string.chat_sticker);
        } else {
            holder.lastMessageTextView.setText(lastMessage);
        }
        holder.dialogTypeTextView.setText(dialog.getType().toString());

        return convertView;
    }

    private static class ViewHolder {
        TextView nameTextView;
        TextView lastMessageTextView;
        TextView dialogTypeTextView;
    }
}