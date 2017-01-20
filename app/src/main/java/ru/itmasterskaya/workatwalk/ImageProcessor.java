/*
 * Copyright abenefic (c) 2017.
 */

package ru.itmasterskaya.workatwalk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

class ImageProcessor {
    private final String mFilePath;
    private final Context mContext;

    public ImageProcessor(String mFilePath, Context context) {
        this.mFilePath = mFilePath;
        this.mContext = context;
    }

    public boolean ScaleAndRotate() {
        FileInputStream fileIn;
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(mFilePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotation = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotation = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotation = 270;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        try {
            int photoQuality = mContext.getSharedPreferences(Constant.PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(Constant.PHOTO_QUALITY, 100);
            if (photoQuality < 100 || rotation != 0) {
                fileIn = new FileInputStream(mFilePath);

                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);

                Bitmap myBitmap = BitmapFactory.decodeStream(fileIn);
                myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap

                FileOutputStream fileOutputStream = new FileOutputStream(mFilePath);
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                myBitmap.compress(Bitmap.CompressFormat.JPEG, photoQuality, bos);
                bos.flush();
                bos.close();
            }
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}