package si.kostakdd.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import si.kostakdd.Constants;
import si.kostakdd.R;


class UpdateDialog {


    static void show(final Context context, String content, final String downloadUrl) {
        if (isContextValid(context)) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.android_auto_update_dialog_title)
                    .setMessage(content)
                    .setPositiveButton(R.string.android_auto_update_dialog_btn_download, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            goToDownload(context, downloadUrl);
                        }
                    })
                    .setNegativeButton(R.string.android_auto_update_dialog_btn_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((Activity) context).finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    private static boolean isContextValid(Context context) {
        return context instanceof Activity && !((Activity) context).isFinishing();
    }


    private static void goToDownload(Context context, String downloadUrl) {
        Intent intent = new Intent(context.getApplicationContext(), DownloadService.class);
        intent.putExtra(Constants.APK_DOWNLOAD_URL, downloadUrl);
        ///PRIDE DO TU Toast.makeText(context,downloadUrl,Toast.LENGTH_LONG).show();
        context.startService(intent);
    }
}
