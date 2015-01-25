package com.tomclaw.appsend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.Html;
import android.widget.Toast;

import java.io.*;

/**
 * Created by Solkin on 11.12.2014.
 */
public class ExportApkTask extends PleaseWaitTask {

    private final AppInfo appInfo;

    private File destination;

    public ExportApkTask(Context context, AppInfo appInfo) {
        super(context);
        this.appInfo = appInfo;
    }

    @Override
    public void executeBackground() throws Throwable {
        Context context = getWeakObject();
        if (context != null) {
            File externalDirectory = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ?
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) : Environment.getExternalStorageDirectory();
            File directory = new File(externalDirectory, "Apps");
            directory.mkdirs();
            File file = new File(appInfo.getPath());
            destination = new File(directory, FileHelper.escapeFileSymbols(appInfo.getLabel() + "-" + appInfo.getVersion()) + ".apk");
            if (destination.exists()) {
                destination.delete();
            }
            byte[] buffer = new byte[200 * 1024];
            InputStream inputStream = new FileInputStream(file);
            OutputStream outputStream = new FileOutputStream(destination);
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        }
    }

    @Override
    public void onSuccessMain() {
        final Context context = getWeakObject();
        if (context != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.success)
                    .setMessage(Html.fromHtml(context.getString(R.string.app_extract_success, destination.getPath())))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.fromFile(destination);
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, destination.getName());
                            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                            sendIntent.setType(FileHelper.getMimeType(destination.getPath()));
                            context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
                        }
                    }).setNegativeButton(R.string.no, null)
                    .create();
            alertDialog.show();
        }
    }

    @Override
    public void onFailMain() {
        Context context = getWeakObject();
        if (context != null) {
            Toast.makeText(context, R.string.app_extract_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
