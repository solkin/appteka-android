package com.tomclaw.appsend.net.request;

import android.os.Bundle;

import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Igor on 07.07.2015.
 */
public class FetchRequest extends BaseRequest {

    private long time;

    public FetchRequest() {
    }

    public FetchRequest(long time) {
        this.time = time;
    }

    @Override
    protected String getApiName() {
        return "fetch";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("time", time);
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            long fetchTime = object.getLong("time");
            JSONArray sent = object.optJSONArray("sent");
            if (sent != null) {
                ArrayList<Message> messages = new ArrayList<>();
                for (int c = 0; c < sent.length(); c++) {
                    JSONObject item = sent.getJSONObject(c);
                    long userId = item.getLong("user_id");
                    long msgId = item.getLong("msg_id");
                    long prevMsgId = item.getLong("prev_msg_id");
                    long time = item.getLong("time") * 1000;
                    String cookie = item.getString("cookie");
                    Message message = new Message(userId, msgId, prevMsgId, time, cookie);
                    messages.add(message);
                }
                Bundle messagesBundle = new Bundle();
                messagesBundle.putSerializable(GlobalProvider.KEY_MESSAGES, messages);
                getContentResolver().call(Config.MESSAGES_RESOLVER_URI,
                        GlobalProvider.METHOD_UPDATE_MESSAGES, null, messagesBundle);
            }
            JSONObject last = object.optJSONObject("last");
            if (last != null) {
                long userId = last.getLong("user_id");
                int msgCount = last.getInt("msg_count");
                long msgId = last.getLong("msg_id");
                long prevMsgId = last.getLong("prev_msg_id");
                long time = last.getLong("time") * 1000;
                int type = last.getInt("type");
                String text = last.getString("text");
                boolean incoming = last.getBoolean("incoming");

                int direction;
                if (type == GlobalProvider.MESSAGE_TYPE_PLAIN) {
                    direction = incoming ?
                            GlobalProvider.DIRECTION_INCOMING :
                            GlobalProvider.DIRECTION_OUTGOING;
                } else {
                    direction = GlobalProvider.DIRECTION_SERVICE;
                }

                Message message = new Message(
                        userId,
                        prevMsgId,
                        msgId,
                        text,
                        time,
                        "",
                        type,
                        direction);

                ArrayList<Message> messages = new ArrayList<>();
                messages.add(message);

                Bundle messagesBundle = new Bundle();
                messagesBundle.putSerializable(GlobalProvider.KEY_MESSAGES, messages);
                getContentResolver().call(Config.MESSAGES_RESOLVER_URI,
                        GlobalProvider.METHOD_INSERT_MESSAGES, null, messagesBundle);

                DiscussController.getInstance().incrementUnreadCount(msgCount);
            }
            getUserHolder().getUserData().onFetchSuccess(fetchTime);
            getUserHolder().store();
            return REQUEST_DELETE;
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return true;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
