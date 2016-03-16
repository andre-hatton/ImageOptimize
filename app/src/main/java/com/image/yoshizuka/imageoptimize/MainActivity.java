package com.image.yoshizuka.imageoptimize;

import android.Manifest;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;

/**
 * Activité principal (normalement comme c'est une petite application il n'y a rien d'autre)
 */
public class MainActivity extends AppCompatActivity implements MainAdapter.OnMainAdapterListener {

    /**
     * code de retour de l'explorateur d'image
     */
    private final static int REQUEST_CODE = 1;

    /**
     * Liste des images selectionnées (chemin des fichiers)
     */
    private ArrayList<String> imageSelected;

    /**
     * La liste des données une fois des images selectionnées
     */
    private RecyclerView imageList;

    /**
     * L'adapter lié à la liste
     */
    private MainAdapter adapter;

    /**
     * Bouton pour finir la compression ou l'annuler
     */
    private Button compressButton;

    /**
     * Bouton d'action de la toolbar
     */
    private ImageView toolbarAction;

    /**
     * Annulation de l'optimisation
     */
    private boolean isCancel;

    /**
     * Réponse pour autoriser les permissions de lecture/écriture sur le stockage externe
     */
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;

    /**
     * Les instructions si aucune image
     */
    private TextView rule;

    /**
     * Liste des images selectionnées
     */
    private List<MainObject> mainList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        rule = (TextView)findViewById(R.id.main_rule);
        rule.setVisibility(View.VISIBLE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarAction = (ImageView)toolbar.findViewById(R.id.toolbar_action);
        setSupportActionBar(toolbar);


        compressButton = (Button)findViewById(R.id.main_compress);
        compressButton.setVisibility(View.GONE);

        imageList = (RecyclerView)findViewById(R.id.main_image);
        imageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        imageList.setVisibility(View.GONE);
        imageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING) hideKeyBoard();
            }
        });

        adapter = new MainAdapter(this);
        imageList.setAdapter(adapter);

       Uri data = getIntent().getData();

        if(data != null)
            onCompressImage(data);

    }

    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
            case  ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                System.gc();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {

        }
    }

    private int k;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                toolbarAction.setClickable(false);
                toolbarAction.setVisibility(View.GONE);
                // liste des chemin des images d'origine
                imageSelected = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                rule.setVisibility(View.GONE);
                imageList.setVisibility(View.VISIBLE);

                // vide la liste des données de la listView
                mainList = new ArrayList<>();
                adapter.setMainList(mainList);
                compressButton.setVisibility(View.VISIBLE);
                compressButton.setText(R.string.cancel);
                int i = 0;
                k = 0;
                // parcours des images
                isCancel = false;
                final Compress compress = Compress.getInstance(this);
                for(final String path : imageSelected) {
                    final int j = i;
                    // on detecte la fin de la compression pour ajouter l'image à la liste
                    compress.setOnCompressListener(new Compress.OnCompressListener() {
                        @Override
                        public void onCompress(MainObject mainObject) {
                            if(k < mainList.size()) {
                                if(mainObject.getImageOptimize() != null && mainObject.getImageOptimize().getStream() != null) {
                                    mainList.set(k, mainObject);
                                    adapter.updateMainList(mainList, k);
                                } else {
                                    mainList.remove(k);
                                    adapter.setMainListRemove(mainList, k);
                                }
                                k++;
                                if (k > j) {
                                    if(isCancel)
                                        Snackbar.make(findViewById(android.R.id.content), R.string.image_cancel, Snackbar.LENGTH_LONG).show();
                                    toolbarAction.setClickable(true);
                                    toolbarAction.setVisibility(View.VISIBLE);
                                    compressButton.setText(R.string.compress);
                                    System.gc();
                                }
                            }
                        }

                        @Override
                        public void onCompressStart(MainObject mainObject) {
                            mainList.add(mainObject);
                            adapter.setMainList(mainList, j);
                        }

                        @Override
                        public void onCompressCancel() {
                            isCancel = true;
                            if(mainList.size() > 0)
                                mainList.remove(mainList.size() - 1);
                            adapter.setMainListRemove(mainList, k);
                            k++;
                            if (k > j) {
                                Snackbar.make(findViewById(android.R.id.content), R.string.image_cancel, Snackbar.LENGTH_LONG).show();
                                toolbarAction.setClickable(true);
                                toolbarAction.setVisibility(View.VISIBLE);
                                if(mainList.size() == 0) {
                                    compressButton.setVisibility(View.GONE);
                                    rule.setVisibility(View.VISIBLE);
                                    imageList.setVisibility(View.GONE);
                                }
                                compressButton.setText(R.string.compress);
                                System.gc();
                            }
                        }
                    });
                    // on empêche l'annulation des tache pour chargé toutes les images
                    compress.setCanCancel(false);
                    // compression de l'image avec un ratio de 80%
                    compress.compressImage(path, 80);
                    i++;
                }
            }
        }
    }

    /**
     * Compressse un image donnée
     * @param image L'image
     */
    private void onCompressImage(Uri image) {

        imageSelected = new ArrayList<>();
        imageSelected.add(image.getPath());
        // vide la liste des données de la listView
        mainList = new ArrayList<>();
        adapter.setMainList(mainList);
        compressButton.setVisibility(View.VISIBLE);
        compressButton.setText(R.string.cancel);
        isCancel = false;
        rule.setVisibility(View.GONE);
        imageList.setVisibility(View.VISIBLE);
        final Compress compress = Compress.getInstance(this);
        // on detecte la fin de la compression pour ajouter l'image à la liste
        compress.setOnCompressListener(new Compress.OnCompressListener() {
            @Override
            public void onCompress(MainObject mainObject) {
                if(k < mainList.size()) {
                    if(mainObject.getImageOptimize() != null && mainObject.getImageOptimize().getStream() != null) {
                        mainList.set(0, mainObject);
                        adapter.updateMainList(mainList, 0);
                    } else {
                        mainList.remove(0);
                        adapter.setMainListRemove(mainList, 0);
                    }
                    if(isCancel)
                        Snackbar.make(findViewById(android.R.id.content), R.string.image_cancel, Snackbar.LENGTH_LONG).show();
                    toolbarAction.setClickable(true);
                    toolbarAction.setVisibility(View.VISIBLE);
                    compressButton.setText(R.string.compress);
                    rule.setVisibility(View.GONE);
                    imageList.setVisibility(View.VISIBLE);
                    System.gc();
                }
            }

            @Override
            public void onCompressStart(MainObject mainObject) {
                mainList.add(mainObject);
                adapter.setMainList(mainList, 0);
            }

            @Override
            public void onCompressCancel() {
                isCancel = true;
                if(mainList.size() > 0)
                    mainList.remove(mainList.size() - 1);
                adapter.setMainListRemove(mainList, 0);
                Snackbar.make(findViewById(android.R.id.content), R.string.image_cancel, Snackbar.LENGTH_LONG).show();
                toolbarAction.setClickable(true);
                toolbarAction.setVisibility(View.VISIBLE);
                if(mainList.size() == 0) {
                    compressButton.setVisibility(View.GONE);
                    rule.setVisibility(View.VISIBLE);
                    imageList.setVisibility(View.GONE);
                }
                compressButton.setText(R.string.compress);
                System.gc();
            }
        });
        // on empêche l'annulation des tache pour chargé toutes les images
        compress.setCanCancel(false);
        // compression de l'image avec un ratio de 80%
        compress.compressImage(image.getPath(), 80);
    }

    /**
     * Affiche l'explorateur d'images
     * @param view Le bouton
     */
    public void onShowImage(View view) {
        PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
        intent.setPhotoCount(9);
        intent.setShowCamera(false);
        intent.setShowGif(false);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Sur le clic du bouton de compression
     * Ecrit dans un fichier chaque images compressées
     * @param view Le bouton
     */
    public void onCompress(View view) {
        if(compressButton.getText().toString().equals(getString(R.string.cancel))) {
            Compress.getInstance(this).cancel();
        } else {
            for (MainObject mainObject : mainList) {
                FileOutputStream out;
                try {
                    out = new FileOutputStream(mainObject.getImageOptimize().getOut());
                    out.write(mainObject.getImageOptimize().getStream().toByteArray());
                    out.close();
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mainObject.getImageOptimize().getOut())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.compress_end, Snackbar.LENGTH_LONG);
            /*
            snackbar.setAction("ouvrir", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setData(Uri.parse(Compress.OPTIMAGE_PATH));
                    intent.setType("");
                    startActivity(Intent.createChooser(intent, "Ouvrir le dossier"));
                }
            });*/
            snackbar.show();
        }
    }

    @Override
    public void onUpdateRatio(int position, MainObject mainObject) {
        mainList.set(position, mainObject);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void hideKeyBoard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
