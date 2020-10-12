package com.raymond.imageanalayze.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.raymond.imageanalayze.R;

import java.io.File;
import java.io.IOException;

public class ImageDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView name, date, count, totalArea, avgSize, areaPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_activity);

        Bundle bundle = getIntent().getExtras();
        imageView = (ImageView) findViewById(R.id.tvImgViewDetailID);
        name = (TextView) findViewById(R.id.tvNameDetailID);
        date = (TextView) findViewById(R.id.tvdateDetailID);
        count = (TextView) findViewById(R.id.tvCountDetailID);
        totalArea = (TextView) findViewById(R.id.tvTotalAreaDetailID);
        avgSize = (TextView) findViewById(R.id.tvAvgSizeDetailID);
        areaPercent = (TextView) findViewById(R.id.tvAreaPercentDetailID);

        if (bundle != null){
//            Bitmap bitImage = getIntent().getParcelableExtra("img");
            String imagePath = bundle.getString("img");
            if (imagePath != null) {
                File file = new File(imagePath);
                Log.d("File", file.getAbsolutePath());
                Uri uri = Uri.fromFile(file.getAbsoluteFile());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    Log.d("Bitmap", String.valueOf(bitmap.getByteCount()));
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            Bitmap bitImage = BitmapFactory.decodeResource(getResources(), R.drawable.dog);
//            imageView.setImageBitmap(bitImage);
            name.setText(bundle.getString("name"));
            date.setText(bundle.getString("date"));
            count.setText(bundle.getString("count"));
            totalArea.setText(bundle.getString("totalArea"));
            avgSize.setText(bundle.getString("avgSize"));
            areaPercent.setText(bundle.getString("areaPercent"));
        }
    }
}