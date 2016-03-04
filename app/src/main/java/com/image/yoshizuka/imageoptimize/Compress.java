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
 * Gestion de la compression des images
 */
public class Compress {

    /**
     * Le context
     */
    private Context context;

    /**
     * L'écouteur de la compression
     */
    private OnCompressListener onCompressListener;

    /**
     * L'instance en cours
     */
    private static Compress _instance;

    /**
     * La tache en cours
     */
    private AsyncTask task;

    /**
     * Empêche les taches de s'annuler
     */
    private boolean canCancel;

    private boolean isCancel = false;

    private boolean isCancelOnBar = false;

    public final static String OPTIMAGE_PATH = Environment.getExternalStorageDirectory() + File.separator + "Optimage";

    /**
     * Recuperation de l'instance (pour ne pas lancer de tache en parallele)
     * @param context Le context actuel
     * @return L'instance courante
     */
    public static Compress getInstance(Context context) {
        if(_instance == null) _instance = new Compress();
        _instance.isCancel = false;
        _instance.context = context;
        _instance.canCancel = true;
        return _instance;
    }

    private Compress() {
    }

    /**
     * Listener permettant de detecter la fin de la compression
     * @param onCompressListener Le listener
     */
    public void setOnCompressListener(OnCompressListener onCompressListener) {
        this.onCompressListener = onCompressListener;
    }

    /**
     * Annule la tache en cours
     * @return true si la tache et annulée
     */
    public boolean cancel() {
        boolean res = false;
        if(task != null) {
            res = task.cancel(true);
            task = null;
        }
        return res;
    }

    public boolean cancelBar() {
        isCancelOnBar = true;
        return cancel();
    }

    public AsyncTask getTask() {
        return task;
    }

    /**
     * Compresse l'image de façon asynchrone
     * @param imagePath L'image d'origine
     * @param progress Le ratio de compression
     */
    public void compressImage(final String imagePath, final int progress) {
        // créer une nouvelle tache
        task = new AsyncTask<Object, ByteArrayOutputStream, ByteArrayOutputStream>() {

            /**
             * L'image bitmap de base
             */
            private Bitmap bitmap;

            /**
             * L'extension du fichier de base
             */
            private String ext;

            /**
             * Données de l'objet optimisé
             */
            private ImageOptimize imageOptimize;

            /**
             * Le fichier de sortie
             */
            private File file;

            /**
             * Les dimension de l'image de sortie
             */
            private int w, h;

            private BitmapFactory.Options options2;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                String[] paths = imagePath.split("\\/");
                String fileName = paths[paths.length - 1];
                String[] splitFile = fileName.split("\\.");
                ext = splitFile[splitFile.length - 1];

                // creer le dossier ImageOptimizer s'il n'existe pas
                File directory = new File(OPTIMAGE_PATH);
                if(!directory.exists())
                    directory.mkdirs();

                // verifie si l'image de sortie existe ou non
                file = new File(directory.getAbsolutePath() + "/" + fileName);
                int i = 1;
                int index = fileName.lastIndexOf(ext);
                String baseName = fileName.substring(0, index - 1);
                while(file.exists()) {
                    // ajoute un identifiant en plus si l'image existe déjà
                    file = new File(directory.getAbsolutePath() + "/" + baseName + "_" + i + "." + ext);
                    i++;
                }

                // options du bitmap (est ce vraiment necessaire ? à voir)
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                options2 = new BitmapFactory.Options();
                options2.inJustDecodeBounds = false;
                options2.outWidth = options.outWidth;
                options2.outHeight = options.outHeight;
                options2.inMutable = true;
                options2.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options2.inDither = true;
                options2.outMimeType = options.outMimeType;


                int tw, th;
                tw = options.outWidth;
                th = options.outHeight;
                int width = context.getResources().getDisplayMetrics().widthPixels;
                if(tw > width) {
                    th = (th * width) / tw;
                    tw = width;
                }
                ImageOptimize imageOptimize = new ImageOptimize();
                imageOptimize.setHeight(th);
                imageOptimize.setWidth(tw);
                imageOptimize.setPath(imagePath);
                imageOptimize.setOut(file);
                imageOptimize.setStream(null);

                MainObject mainObject = new MainObject();
                mainObject.setImageOptimize(imageOptimize);
                mainObject.setImagePath(imagePath);
                mainObject.setSize(new File(imagePath).length());
                onCompressListener.onCompressStart(mainObject);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                System.out.println("onCancel");
                if(isCancelOnBar) {
                    isCancelOnBar = false;
                    isCancel = false;
                }
                else {
                    isCancel = true;
                    setCanCancel(true);
                }
                onCompressListener.onCompressCancel();
                if(bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }

            @Override
            protected ByteArrayOutputStream doInBackground(Object... params) {
                if(isCancelled() || isCancel) {
                    return null;
                }
                try {
                    // attention il peut y avoir un blocage en mémoire
                    bitmap = BitmapFactory.decodeFile(imagePath, options2);
                } catch (OutOfMemoryError e) {
                    System.gc();
                    cancel(true);
                    e.printStackTrace();
                    isCancel = true;
                    return null;
                }

                // la sortie de la compression (n'est pas un fichier pour le moment)
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                if(ext.toLowerCase().equals("jpg") || ext.toLowerCase().equals("jpeg")) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, progress, os);
                } else if(ext.toLowerCase().equals("png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, progress, os);
                }
                w = bitmap.getWidth();
                h = bitmap.getHeight();
                bitmap.recycle();
                System.gc();
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
                    if(os != null)
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
                mainObject.setSize(new File(imagePath).length());

                if(!isCancelled() && !isCancel)
                   onCompressListener.onCompress(mainObject);
                else
                    onCompressListener.onCompressCancel();
            }
        };
        // on utilise SERIAL_EXECUTOR pour éviter les fuite mémoire
        // on vide la mémoire après chacun des chargements
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, new Object());

    }


    /**
     * Empêche ou non l'annulation de la tache en cours
     * @param canCancel false pour ne pas annuler
     */
    public void setCanCancel(boolean canCancel) {
        this.canCancel = canCancel;
    }

    /**
     * Gestion des retour de la compression
     */
    public interface OnCompressListener {

        /**
         * La compression est terminée
         * @param mainObject L'objet contenant les données de l'image final
         */
        void onCompress(MainObject mainObject);

        /**
         * Lorsque la compression a commencé
         */
        void onCompressStart(MainObject mainObject);

        /**
         * Lorsque la compression a été annulée
         */
        void onCompressCancel();
    }
}
