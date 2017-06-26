package com.tomclaw.appsend.net.request;

import android.os.Bundle;

import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.ContentResolverLayer;
import com.tomclaw.appsend.core.DatabaseLayer;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by solkin on 23/04/16.
 */
public class HistoryRequest extends BaseRequest {

    private long msgIdFrom;
    private long msgIdTill;

    public HistoryRequest() {
    }

    public HistoryRequest(long msgIdFrom, long msgIdTill) {
        this.msgIdFrom = msgIdFrom;
        this.msgIdTill = msgIdTill;
    }

    @Override
    protected String getApiName() {
        return "history";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("from", msgIdFrom);
        builder.appendParam("till", msgIdTill);
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            DatabaseLayer databaseLayer = ContentResolverLayer.from(getContentResolver());
            JSONArray history = object.optJSONArray("history");
            if (history != null) {
                ArrayList<Message> messages = new ArrayList<>();
                for (int c = 0; c < history.length(); c++) {
                    JSONObject item = history.getJSONObject(c);
                    long userId = item.getLong("user_id");
                    long msgId = item.getLong("msg_id");
                    long prevMsgId = item.getLong("prev_msg_id");
                    long time = item.getLong("time") * 1000;
                    int type = item.getInt("type");
                    String text = item.getString("text");
                    boolean incoming = item.getBoolean("incoming");

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
                    messages.add(message);
                }
                Bundle messagesBundle = new Bundle();
                messagesBundle.putSerializable(GlobalProvider.KEY_MESSAGES, messages);
                getContentResolver().call(Config.MESSAGES_RESOLVER_URI,
                        GlobalProvider.METHOD_INSERT_MESSAGES, null, messagesBundle);
            }
            return REQUEST_DELETE;
        } else if (status == STATUS_INVALID_DATA) {
            return REQUEST_DELETE;
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return true;
    }
}
