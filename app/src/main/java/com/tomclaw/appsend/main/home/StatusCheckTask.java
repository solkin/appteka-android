package com.tomclaw.appsend.main.home;

import static com.tomclaw.appsend.core.Config.STATUS_HOST_URL;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.HttpTask;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.LocaleHelper;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class StatusCheckTask extends HttpTask {

    private WeakReference<Context> weakContext;
    private boolean isBlock;
    private String title;
    private String message;

    public StatusCheckTask(Context context) {
        super(STATUS_HOST_URL, new HttpParamsBuilder().appendParam("locale", LocaleHelper.getLocaleLanguage()));
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected void onLoaded(JSONObject jsonObject) {
        JSONObject result = jsonObject.optJSONObject("result");
        if (result != null) {
            isBlock = result.optBoolean("block");
            title = result.optString("title");
            message = result.optString("message");
        }
    }

    @Override
    protected void onError() {
    }

    @Override
    public void onPostExecuteMain() {
        Context context = weakContext.get();
        if (context != null && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(message)) {
            new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(!isBlock)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        if (isBlock) {
                            System.exit(0);
                        }
                    })
                    .create()
                    .show();
        }
    }
}
