package com.image.yoshizuka.imageoptimize;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
     * Liste des images selectionnées
     */
    private List<MainObject> mainList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageList = (RecyclerView)findViewById(R.id.main_image);
        imageList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MainAdapter(this);
        imageList.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoPickerIntent intent = new PhotoPickerIntent(MainActivity.this);
                intent.setPhotoCount(9);
                intent.setShowCamera(false);
                intent.setShowGif(false);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                // liste des chemin des images d'origine
                imageSelected = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);

                // vide la liste des données de la listView
                mainList = new ArrayList<>();
                adapter.setMainList(mainList);
                int i = 0;
                // parcours des images
                for(final String path : imageSelected) {
                    Compress compress = Compress.getInstance(this);
                    final int j = i;
                    // on detecte la fin de la compression pour ajouter l'image à la liste
                    compress.setOnCompressListener(new Compress.OnCompressListener() {
                        @Override
                        public void onCompress(MainObject mainObject) {
                            mainList.add(mainObject);
                            adapter.setMainList(mainList, j);
                        }

                        @Override
                        public void onCompressStart() {

                        }

                        @Override
                        public void onCompressCancel() {

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
     * Sur le clic du bouton de compression
     * Ecrit dans un fichier chaque images compressées
     * @param view Le bouton
     */
    public void onCompress(View view) {
        for(MainObject mainObject : mainList) {
            FileOutputStream out;
            try {
                out = new FileOutputStream(mainObject.getImageOptimize().getOut());
                out.write(mainObject.getImageOptimize().getStream().toByteArray());
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateRatio(int position, MainObject mainObject) {
        mainList.set(position, mainObject);
    }
}
