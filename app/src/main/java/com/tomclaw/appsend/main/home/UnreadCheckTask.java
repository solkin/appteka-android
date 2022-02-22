package com.tomclaw.appsend.main.home;

import static com.tomclaw.appsend.core.Config.HOST_URL;

import androidx.annotation.Nullable;

import com.tomclaw.appsend.core.HttpTask;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.LegacyLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UnreadCheckTask extends HttpTask {

    private @Nullable
    UnreadListener listener;
    private int count = 0;
    private String guid;

    public UnreadCheckTask(String guid) {
        super(HOST_URL + "/api/1/chat/topics", new HttpParamsBuilder().appendParam("guid", guid));
    }

    @Override
    protected void onLoaded(JSONObject jsonObject) {
        JSONObject result = jsonObject.optJSONObject("result");
        if (result != null) {
            try {
                JSONArray topics = result.optJSONArray("entries");
                if (topics != null) {
                    count = 0;
                    for (int i = 0; i < topics.length(); i++) {
                        JSONObject topic = topics.getJSONObject(i);
                        boolean isPinned = topic.optBoolean("pinned");
                        if (isPinned) {
                            int readMsgId = topic.optInt("read_msg_id");
                            JSONObject lastMsg = topic.optJSONObject("last_msg");
                            if (lastMsg != null) {
                                int lastMsgId = lastMsg.getInt("msg_id");
                                if (readMsgId < lastMsgId) {
                                    count++;
                                }
                            }
                        }
                    }
                    notifyListener();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onError() {
        LegacyLogger.log("Error loading unread count");
    }

    public void resetUnreadCount() {
        count = 0;
        notifyListener();
    }

    public void setListener(@Nullable UnreadListener listener) {
        this.listener = listener;
    }

    public void detachListener() {
        listener = null;
    }

    private void notifyListener() {
        if (listener != null) {
            MainExecutor.execute(() -> listener.onUnread(count));
        }
    }

    public interface UnreadListener {

        void onUnread(int count);

    }

}
