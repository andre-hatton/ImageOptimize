package com.image.yoshizuka.imageoptimize;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.PhotoPickerActivity;
import me.iwf.photopicker.utils.PhotoPickerIntent;


public class MainActivity extends AppCompatActivity implements MainAdapter.OnMainAdapterListener {

    private final static int REQUEST_CODE = 1;

    private ArrayList<String> imageSelected;

    private RecyclerView imageList;

    private MainAdapter adapter;

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
                imageSelected = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
                mainList = new ArrayList<>();
                int i = 0;
                for(final String path : imageSelected) {
                    Compress compress = Compress.getInstance(this);
                    final int j = i;
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
                    compress.setCanCancel(false);
                    compress.compressImage(path, 80);
                    i++;
                }
                adapter.setMainList(mainList);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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
