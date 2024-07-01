/*
 *  Copyright (c) 2020 Dennis Lang(LanDen Labs) landenlabs@gmail.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  associated documentation files (the "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 *  following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 *  LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 *  NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *  SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  @author Dennis Lang  (Jan-2020)
 *  @see https://landenlabs.com
 *
 */
package com.landenlabs.allThreadPenalty.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ShareActionProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.landenlabs.allThreadPenalty.R;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Share screen shot of screen.
 */
public class ShareUtil {
    private static final String TAG = "ShareUtil";

    public static void shareScreen(
            @NonNull  Activity activity, @NonNull View view,
            @NonNull  String what, @Nullable ShareActionProvider shareActionProvider) {
        Bitmap screenBitmap = getBitmap(view);
        List<Bitmap> bitmapList = new ArrayList<>();
        bitmapList.add(screenBitmap);
        shareList(activity, bitmapList, null, what, "allThreadTest.jpg", shareActionProvider);
    }

    /**
     * Helper to get screen shot of View object.
     */
    private static Bitmap getBitmap(@NonNull View view) {
        Bitmap screenBitmap =
                Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        screenBitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(screenBitmap);
        view.draw(canvas);
        return screenBitmap;
    }

    private static boolean isBitmapValid(@Nullable Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled() && bitmap.getHeight() * bitmap.getWidth() > 0;
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private static void shareList(
            @NonNull Activity activity,
            @Nullable List<Bitmap> shareImages,
            @Nullable List<String> shareCsv,
            @NonNull String what,
            String imageName,
            @Nullable ShareActionProvider shareActionProvider) {

        final String IMAGE_TYPE = "image/jpg";
        final String TEXT_TYPE = "text/plain";
        Intent shareIntent;

        if (shareImages != null && shareImages.size() > 0) {
            int imgCnt = shareImages.size();
            shareIntent = new Intent(imgCnt == 1 ? Intent.ACTION_SEND : Intent.ACTION_SEND_MULTIPLE);
            shareIntent.setType(IMAGE_TYPE);
            if (imgCnt == 1) {
                Bitmap bitmap = shareImages.get(0);
                if (!isBitmapValid(bitmap))
                    return;

                /*
                    // String screenImgFilename = Images.Media.insertImage(getContentResolver(), bitmap, imageName, null);
                    String screenImgFilename = Utils.saveBitmap(context, bitmap, imageName);

                    Uri uri = Uri.fromFile(new File(screenImgFilename));
                */
                Uri uri = getUriForBitmap(activity, bitmap, imageName);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                bitmap.recycle();
            } else {
                ArrayList<Uri> uris = new ArrayList<>();
                for (int bmIdx = 0; bmIdx != shareImages.size(); bmIdx++) {
                    Bitmap bitmap = shareImages.get(bmIdx);
                    if (isBitmapValid(bitmap)) {
                        /*
                        String screenImgFilename = Utils.saveBitmap(context, bitmap, String.valueOf(bmIdx) + imageName);
                        Uri uri = Uri.fromFile(new File(screenImgFilename));
                        */
                        Uri uri = getUriForBitmap(activity, bitmap, String.valueOf(bmIdx) + imageName);
                        uris.add(uri);
                        bitmap.recycle();
                    } else {
                        Log.e(TAG, "invalid bitmap");
                    }
                }
                shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            }
        } else {
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(TEXT_TYPE);
        }

        String appName = activity.getString(R.string.app_name);
        String shareBody = String.format("%s v%s\n%s\n%s\n",
                appName,
                activity.getString(R.string.versionName),
                activity.getString(R.string.websiteLanDenLabs),
                what);

        if (shareCsv != null && shareCsv.size() > 0) {
            shareBody += TextUtils.join("\n", shareCsv);
        }
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, appName + " " + what);
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

    	/*
        if (IMAGE_TYPE.equals(shareIntent.getMimeType())) {
        	shareActionProvider.setHistoryFileName(SHARE_IMAGE_HISTORY_FILE_NAME);
        } else if (TEXT_TYPE.equals(shareIntent.getMimeType())) {
        	shareActionProvider.setHistoryFileName(SHARE_TEXT_HISTORY_FILE_NAME);
        }
        */
        //	if (shareActionProvider != null) {
        //		shareActionProvider.setShareIntent(shareIntent);
        //	} else {
        activity.startActivity(Intent.createChooser(shareIntent, "Share"));
        //	}
    }

    /**
     * Save bitmap to local filesystem
     *
     * @param bitmap   Bitmap to save
     * @param baseName Base filename used to save image, ex: "screenshot.png"
     * @return full filename path
     */
    @SuppressWarnings("unused")
    private static Uri getUriForBitmap(@NonNull Context context, @NonNull Bitmap bitmap, String baseName) {

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "allThreadPenalty.jpg");
            values.put(MediaStore.Images.Media.TITLE, "Thread Penalty");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            Uri uri = context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            // context.grantUriPermission(mContext.getPackageName(), uri,
            //         Intent.FLAG_GRANT_WRITE_URI_PERMISSION + Intent.FLAG_GRANT_READ_URI_PERMISSION);
            OutputStream ostream = context.getContentResolver().openOutputStream(uri);

            // Jpeg format about 20x faster to export then PNG and smaller image.
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, ostream);
            ostream.close();
            return uri;
        } catch (Exception ex) {
            Log.e(TAG, "Save bitmap failed " + ex.getMessage());
        }
        return null;
    }

}
