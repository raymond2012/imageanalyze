package com.raymond.imageanalayze.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import com.raymond.imageanalayze.Data.DatabaseHandler;
import com.raymond.imageanalayze.Model.OpenCVImage;
import com.raymond.imageanalayze.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button changeButton;
    private Button saveButton;
    private ImageView img;

    private static final String TAG = "Main::Activity";
    private Bitmap oriBitmap;
    private Bitmap grayBitmap;
    private static boolean flag = true;
    private String imagePath = null;
    private String savedImagePath = null;

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int LIST_PICTURE = 3;

    private DatabaseHandler db;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        db = new DatabaseHandler(this);

        img = (ImageView)findViewById(R.id.tvImgViewDetailID);
        changeButton = (Button)findViewById(R.id.btnChange);
        saveButton = (Button)findViewById(R.id.btnSave);
        changeButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//        srcBitmap = oriBitmap;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.imCamera:
                Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cInt,TAKE_PHOTO);
                Toast.makeText(this, "Take Photo", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.imStorage:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
                Toast.makeText(this, "Find Storage", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.imImageList:
                Intent intent = new Intent(MainActivity.this, ImageListViewActivity.class);
                startActivity(intent);
//                finish();
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    oriBitmap = (Bitmap) data.getExtras().get("data");
                    procSrc2Gray(oriBitmap);
                    if(grayBitmap != null) {
                        img.setImageBitmap(grayBitmap);
                        Log.i("TAKE_PHOTO", oriBitmap.toString());
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    handleImage(data);
//                    Log.i("CHOOSE_PHOTO", oriBitmap.toString());
                }
                break;
            default:
                break;

        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void procSrc2Gray(Bitmap bitmap){
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        if ( bitmap != null){
            grayBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
            Utils.bitmapToMat(bitmap, rgbMat);//convert original bitmap to Mat, R G B.
            Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
            Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
            Log.i(TAG, "procSrc2Gray success...");
        }
//        if (oriBitmap == null) srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dog);
//        else srcBitmap = oriBitmap;
//        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
//        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
//        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
//        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
//        Log.i(TAG, "procSrc2Gray success...");
    }

    public void filter(Bitmap bitmap){
        if ( bitmap != null) {
            Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_16U);
            Mat src_gray = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_16U);

            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
//            Imgproc.adaptiveThreshold(src_gray,src_gray,255,Imgproc.ADAPTIVE_THRESH_MEAN_C , Imgproc.THRESH_BINARY_INV,11,10);
//            Imgproc.threshold(src_gray, src_gray, 30, 0, Imgproc.THRESH_BINARY);
            Core.inRange(src_gray, new Scalar(0,0,0), new Scalar(140,140,140), src_gray);
//
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(src_gray, output);
            int count = Core.countNonZero(src_gray);
            Log.d(TAG, "Count: " + count);
            Log.d(TAG, String.valueOf(Core.minMaxLoc(src_gray).maxVal));

            grayBitmap = output;
        }
    }

    public Bitmap grayscale(Bitmap src){
        final double GS_RED = 0.2989;
        final double GS_GREEN = 0.5870;
        final double GS_BLUE = 0.1140;

        int totalArea = 0;

        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.RGBA_F16);
        // pixel information
        int A, R, G, B;
        double Rf = 0.0;
        int pixel;

        // get image size
        int width = src.getWidth();
        int height = src.getHeight();

        // scan through every single pixel
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get one pixel color
                pixel = src.getPixel(x, y);
                // retrieve color of all channels
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                // take conversion up to one single value
                int temp = (int) (Math.round(GS_RED * R + GS_GREEN * G + GS_BLUE * B));
//                Rf = (GS_RED * R + GS_GREEN * G + GS_BLUE * B) * 257;
                R = G = B = temp;
                if ( temp < 30 || temp > 150){
                    R = G = B = 0;
                } else {
                    R = G = B = 1;
                    totalArea++;
                }
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        Log.i(TAG, "R: " + Rf);
        Log.i(TAG, String.valueOf(Color.red(bmOut.getPixel(0,0))));
        Log.i(TAG, String.valueOf((float) totalArea/(height*width)));
        return bmOut;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnChange:
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inScaled = false;
//                oriBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.image_23, options);
//                grayBitmap = grayscale(oriBitmap);
//                if (grayBitmap != null) {
//                    img.setImageBitmap(grayBitmap);
//                }
//                if (flag) {
//                    img.setImageBitmap(oriBitmap);
//                    flag = false;
//                } else {
//                    img.setImageBitmap(grayBitmap);
//                    flag = true;
//                }
                Boolean exported = db.exportDatabase();
                if (exported) Toast.makeText(this, "Exported in " + db.getFilePath(), Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Failed to     Export", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnSave:
                if (grayBitmap != null) {
                    try {
                        savedImagePath = saveBitmap(oriBitmap);
                        if (savedImagePath != null) {
                            saveOpenCVImageToDB();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }


    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private void handleImage(Intent data) {
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if ("com.android.providers.media.downloads".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads.public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }else if ("content".equalsIgnoreCase(uri.getScheme())){
                imagePath = uri.getPath();
            }else if ("file".equalsIgnoreCase(uri.getScheme())){
                imagePath=uri.getPath();
            }
            Log.i(TAG, "image path: " + imagePath);
            displayImage(imagePath);
        }
    }

    private String getImagePath(Uri uri,String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if (cursor.moveToFirst()){
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        cursor.close();
        return path;
    }

    private void displayImage(String imagePath){
        if (imagePath !=null){
            oriBitmap = BitmapFactory.decodeFile(imagePath);
            procSrc2Gray(oriBitmap);
            img.setImageBitmap(grayBitmap);
            Log.d(TAG, "oriBitmap: " + oriBitmap);
            Log.d(TAG, "grayBitmap: " + grayBitmap);
        }else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveOpenCVImageToDB() {
        OpenCVImage openCVImage = new OpenCVImage(savedImagePath, "Test");
//        openCVImage.setName("Test");

        // Save to DB
        db.AddOpenCVImage(openCVImage);
        Log.d("Item Added ID", String.valueOf(db.getOpenCVImagesCount()));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //start a new activity
                startActivity(new Intent(MainActivity.this, ImageListViewActivity.class));
            }
        }, 1200); // 1 second
    }

    public String saveBitmap(Bitmap bitmap) throws IOException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String timestamp = simpleDateFormat.format(System.currentTimeMillis());
        String fileName = timestamp +".jpg";
        OutputStream outputStream;
        boolean isSave;
        String filePath = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/OpenCV");
            Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            outputStream = resolver.openOutputStream(uri);
//            filePath = file.getAbsolutePath() + "/" + fileName;
            filePath = Environment.getExternalStorageDirectory() + "/DCIM/OpenCV/" + fileName;
        } else {
            String path = Environment.getExternalStorageDirectory().toString();
            File folder = new File(path + "/OpenCV");
            if (!folder.exists()){
                folder.mkdir();
            }
            File file = new File(folder, fileName);
            if (file.exists()){
                file.delete();
            }
            outputStream = new FileOutputStream(file);
            filePath = file.getAbsolutePath() + "/" + fileName;
        }
        isSave = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        if (isSave) {
            Toast.makeText(this, "Saved Successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
        outputStream.flush();
        outputStream.close();
        Log.i(TAG, filePath);
        return filePath;
    }
}
