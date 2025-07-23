/*******************************************************************************
 * Copyright (c) 2010 Denis Solonenko.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors:
 *     Denis Solonenko - initial API and implementation
 ******************************************************************************/
package tw.tib.financisto.export;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import tw.tib.financisto.R;
import tw.tib.financisto.bus.GreenRobotBus_;
import tw.tib.financisto.bus.RefreshCurrentTab;
import tw.tib.financisto.db.DatabaseAdapter;
import tw.tib.financisto.utils.MyPreferences;

import static tw.tib.financisto.export.Export.uploadBackupFileToDropbox;
import static tw.tib.financisto.export.Export.uploadBackupFileToGoogleDrive;

public abstract class ImportExportAsyncTask extends AsyncTask<Uri, String, Object> {

    protected final Context context;
    protected final ProgressDialog dialog;
    private boolean showResultMessage = true;

    private ImportExportAsyncTaskListener listener;

    public ImportExportAsyncTask(Context context, ProgressDialog dialog) {
        this.dialog = dialog;
        this.context = context;
    }

    public void setListener(ImportExportAsyncTaskListener listener) {
        this.listener = listener;
    }

    public void setShowResultMessage(boolean showResultMessage) {
        this.showResultMessage = showResultMessage;
    }

    @Override
    protected Object doInBackground(Uri... params) {
        DatabaseAdapter db = new DatabaseAdapter(context);
        db.open();
        try {
            return work(context, db, params);
        } catch (Exception ex) {
            Log.e("Financisto", "Unable to do import/export", ex);
            return ex;
        } finally {
            db.close();
        }
    }

    protected abstract Object work(Context context, DatabaseAdapter db, Uri... params) throws Exception;

    protected String getSuccessMessage(Object result) {
        if (result instanceof Uri) {
            String filename = ((Uri) result).getLastPathSegment();
            if (filename != null) {
                return filename.substring(filename.lastIndexOf("/") + 1);
            }
        }
        if (result instanceof String) {
            return (String) result;
        }
        if (result != null) {
            return result.toString();
        }
        return null;
    }

    protected void doUploadToDropbox(Context context, Uri backupFileUri) throws Exception {
        if (MyPreferences.isDropboxUploadBackups(context)) {
            doForceUploadToDropbox(context, backupFileUri);
        }
    }

    protected void doForceUploadToDropbox(Context context, Uri backupFileUri) throws Exception {
        publishProgress(context.getString(R.string.dropbox_uploading_file));
        uploadBackupFileToDropbox(context, backupFileUri);
    }

    void doUploadToGoogleDrive(Context context, Uri backupFileUri) throws Exception {
        if (MyPreferences.isGoogleDriveUploadBackups(context)) {
            doForceUploadToGoogleDrive(context, backupFileUri);
        }
    }

    protected void doForceUploadToGoogleDrive(Context context, Uri backupFileUri) throws Exception {
        publishProgress(context.getString(R.string.google_drive_uploading_file));
        uploadBackupFileToGoogleDrive(context, backupFileUri);
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        dialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Object result) {
        dialog.dismiss();

        if (result instanceof UserRecoverableAuthIOException) {
            context.startActivity(((UserRecoverableAuthIOException)result).getIntent());
        }
        else if (result instanceof Exception exception) {
            StringBuilder sb = new StringBuilder();

            if (result instanceof ImportExportException importExportException) {
                if (importExportException.formatArgs != null) {
                    sb.append(context.getString(importExportException.errorResId, importExportException.formatArgs));
                } else {
                    sb.append(context.getString(importExportException.errorResId));
                }
            }
            var cause = exception.getCause();

            if (cause != null) {
                StackTraceElement[] stack = cause.getStackTrace();
                sb.append(" : ").append(cause).append("\n\n");
                for (StackTraceElement e : stack) {
                    String fileName = e.getFileName();
                    if (fileName.equals("ImportExportAsyncTask.java")) break;
                    sb.append(fileName).append(":").append(e.getLineNumber()).append("\n");
                }
            }
            new AlertDialog.Builder(context)
                    .setTitle(R.string.fail)
                    .setMessage(sb.toString())
                    .setPositiveButton(R.string.ok, null)
                    .show();
            return;
        }

        String message = getSuccessMessage(result);

        refreshMainActivity();
        if (listener != null) {
            listener.onCompleted(result);
        }

        if (showResultMessage) {
            Toast.makeText(context, context.getString(R.string.success, message), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshMainActivity() {
        GreenRobotBus_.getInstance_(context).post(new RefreshCurrentTab());
    }

}

