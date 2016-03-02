package com.image.yoshizuka.imageoptimize;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

    private Button compressButton;

    private ImageView toolbarAction;

    /**
     * Liste des images selectionnées
     */
    private List<MainObject> mainList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarAction = (ImageView)toolbar.findViewById(R.id.toolbar_action);
        setSupportActionBar(toolbar);


        compressButton = (Button)findViewById(R.id.main_compress);
        compressButton.setVisibility(View.GONE);

        imageList = (RecyclerView)findViewById(R.id.main_image);
        imageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MainAdapter(this);
        imageList.setAdapter(adapter);
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

                // vide la liste des données de la listView
                mainList = new ArrayList<>();
                adapter.setMainList(mainList);
                compressButton.setVisibility(View.VISIBLE);
                compressButton.setText(R.string.cancel);
                int i = 0;
                k = 0;
                // parcours des images
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
                                if (k >= j) {
                                    toolbarAction.setClickable(true);
                                    toolbarAction.setVisibility(View.VISIBLE);
                                    compressButton.setText(R.string.compress);
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
                            if(mainList.size() > 0)
                                mainList.remove(mainList.size() - 1);
                            adapter.setMainListRemove(mainList, k);
                            k++;
                            if (k >= j) {
                                toolbarAction.setClickable(true);
                                toolbarAction.setVisibility(View.VISIBLE);
                                if(mainList.size() == 0) compressButton.setVisibility(View.GONE);
                                compressButton.setText(R.string.compress);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Snackbar.make(findViewById(android.R.id.content), R.string.compress_end, Snackbar.LENGTH_LONG).setAction("ouvrir", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(mainList.get(0).getImageOptimize().getOut().getParentFile()), "resource/folder");
                    startActivity(Intent.createChooser(intent, "Ouvrir le dossier"));
                }
            }).show();
        }
    }

    @Override
    public void onUpdateRatio(int position, MainObject mainObject) {
        mainList.set(position, mainObject);
    }
}
