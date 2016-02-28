package com.image.yoshizuka.imageoptimize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by yoshizuka on 28/02/16.
 */
public class Compress {

    private Context context;

    private OnCompressListener onCompressListener;

    private static Compress _instance;

    private AsyncTask task;

    private boolean canCancel;

    public static Compress getInstance(Context context) {
        if(_instance == null) _instance = new Compress();
        _instance.context = context;
        _instance.canCancel = true;
        return _instance;
    }

    private Compress() {
    }

    public void setOnCompressListener(OnCompressListener onCompressListener) {
        this.onCompressListener = onCompressListener;
    }

    public void compressImage(final String imagePath, final int progress) {
        if(task != null && canCancel)
            task.cancel(false);
        task = new AsyncTask<Object, ByteArrayOutputStream, ByteArrayOutputStream>() {

            private Bitmap bitmap;
            private String ext;
            private ImageOptimize imageOptimize;
            private File file;
            private int w, h;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                onCompressListener.onCompressStart();
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                onCompressListener.onCompressCancel();
                if(bitmap != null)
                    bitmap.recycle();
            }

            @Override
            protected ByteArrayOutputStream doInBackground(Object... params) {
                String[] paths = imagePath.split("\\/");
                String fileName = paths[paths.length - 1];

                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "ImageOptimizer");
                if(!directory.exists())
                    directory.mkdirs();
                file = new File(directory.getAbsolutePath() + "/" + fileName);
                int i = 0;
                while(file.exists()) {
                    file = new File(directory.getAbsolutePath() + "/" + i + fileName);
                    i++;
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                BitmapFactory.Options options2 = new BitmapFactory.Options();
                options2.inJustDecodeBounds = false;
                options2.outWidth = options.outWidth;
                options2.outHeight = options.outHeight;
                options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options2.inDither = true;
                try {
                    bitmap = BitmapFactory.decodeFile(imagePath, options2);
                } catch (OutOfMemoryError e) {
                    cancel(true);
                    e.printStackTrace();
                }

                String[] splitFile = fileName.split("\\.");
                ext = splitFile[splitFile.length - 1];
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                if(ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, progress, os);
                } else if(ext.toLowerCase().equals("png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, progress, os);
                }
                w = bitmap.getWidth();
                h = bitmap.getHeight();
                bitmap.recycle();
                return os;
            }

            @Override
            protected void onPostExecute(ByteArrayOutputStream os) {
                super.onPostExecute(os);

                imageOptimize = new ImageOptimize();
                imageOptimize.setOut(file);
                imageOptimize.setPath(imagePath);
                imageOptimize.setStream(os);
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int width = context.getResources().getDisplayMetrics().widthPixels;
                if(w > width) {
                    h = (h * width) / w;
                    w = width;
                }
                imageOptimize.setWidth(w);
                imageOptimize.setHeight(h);

                MainObject mainObject = new MainObject();
                mainObject.setImageOptimize(imageOptimize);
                mainObject.setImagePath(imagePath);
                mainObject.setSize(String.valueOf(new File(imagePath).length() / 1024));

                onCompressListener.onCompress(mainObject);
            }
        };
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object());

    }

    public boolean isCanCancel() {
        return canCancel;
    }

    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    public interface OnCompressListener {
        void onCompress(MainObject mainObject);
        void onCompressStart();
        void onCompressCancel();
    }
}
