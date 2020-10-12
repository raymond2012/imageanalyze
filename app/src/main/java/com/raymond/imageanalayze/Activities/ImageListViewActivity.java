package com.raymond.imageanalayze.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.raymond.imageanalayze.Data.DatabaseHandler;
import com.raymond.imageanalayze.Model.OpenCVImage;
import com.raymond.imageanalayze.UI.RecyclerViewAdapter;
import com.raymond.imageanalayze.R;

import java.util.ArrayList;
import java.util.List;

public class ImageListViewActivity extends Activity {
    private static final String TAG = "ImageListView::Activity";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<OpenCVImage> listItems;
    private List<OpenCVImage> openCVImageList;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview_activity);

        db = new DatabaseHandler(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewID);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        openCVImageList = new ArrayList<>();
        listItems = new ArrayList<>();

        openCVImageList = db.getAllOpenCVImage();
        for (OpenCVImage item: openCVImageList) {
            OpenCVImage openCVImage = new OpenCVImage();
            openCVImage.setImage(item.getImage());
            Log.d(TAG, item.getImage());
            openCVImage.setName(item.getName());
            openCVImage.setDate(item.getDate());
            openCVImage.setId(item.getId());


            listItems.add(openCVImage);
        }

        adapter = new RecyclerViewAdapter(this, openCVImageList);
        recyclerView.setAdapter(adapter);
    }
}
