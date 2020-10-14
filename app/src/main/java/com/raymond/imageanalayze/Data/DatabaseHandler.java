package com.raymond.imageanalayze.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.raymond.imageanalayze.Model.OpenCVImage;
import com.raymond.imageanalayze.Util.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHandler extends SQLiteOpenHelper {
    private Context mContext;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static String TAG = "DatabaseHandler";
    private String filePath;

    public DatabaseHandler(Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GROCERY_TABLE = "CREATE TABLE " + Constants.TABLE_NAME + "("
                + Constants.KEY_ID + " INTEGER PRIMARY KEY,"
                + Constants.KEY_IMAGE + " TEXT,"
                + Constants.KEY_NAME + " TEXT,"
                + Constants.KEY_DATE + " LONG,"
                + Constants.KEY_COUNT + " TEXT,"
                + Constants.KEY_TOTAL_AREA + " TEXT,"
                + Constants.KEY_AVG_SIZE + " TEXT,"
                + Constants.KEY_AREA_PERCENT + " TEXT);";

        db.execSQL(CREATE_GROCERY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);

        onCreate(db);
    }

    // Add OpenCVImage
    public void AddOpenCVImage(OpenCVImage image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_IMAGE, image.getImage());
        values.put(Constants.KEY_NAME, image.getName());
        values.put(Constants.KEY_DATE, System.currentTimeMillis());
        values.put(Constants.KEY_COUNT, image.getCount());
        values.put(Constants.KEY_TOTAL_AREA, image.getTotalArea());
        values.put(Constants.KEY_AVG_SIZE, image.getAvgSize());
        values.put(Constants.KEY_AREA_PERCENT, image.getAreaPercent());

        db.insert(Constants.TABLE_NAME, null, values);
        Log.d(TAG, "Save to DB");
    }

    // Get a OpenCVImage
    public OpenCVImage getOpenCVImage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Constants.TABLE_NAME,
                new String[] {Constants.KEY_ID, Constants.KEY_IMAGE, Constants.KEY_NAME, Constants.KEY_DATE, Constants.KEY_COUNT, Constants.KEY_TOTAL_AREA, Constants.KEY_AVG_SIZE, Constants.KEY_AREA_PERCENT},
                Constants.KEY_ID + "=?",
                new String[] {String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        OpenCVImage openCVImage = new OpenCVImage();
        openCVImage.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
        openCVImage.setImage(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)));
        openCVImage.setName(cursor.getString(cursor.getColumnIndex(Constants.KEY_NAME)));
        openCVImage.setCount(cursor.getString(cursor.getColumnIndex(Constants.KEY_COUNT)));
        openCVImage.setTotalArea(cursor.getString(cursor.getColumnIndex(Constants.KEY_TOTAL_AREA)));
        openCVImage.setAvgSize(cursor.getString(cursor.getColumnIndex(Constants.KEY_AVG_SIZE)));
        openCVImage.setAreaPercent(cursor.getString(cursor.getColumnIndex(Constants.KEY_AREA_PERCENT)));

        // Convert Timestamp to something readable
//        DateFormat dateFormat = DateFormat.getDateInstance();
        String formatedDate = sdf.format(new Date(cursor.getLong(cursor.getColumnIndex(Constants.KEY_DATE))).getTime());
        openCVImage.setDate(formatedDate);

        return openCVImage;
    }

    public void deleteOpenCVImage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(Constants.TABLE_NAME, Constants.KEY_ID + "=?", new String[] {String.valueOf(id)});

        db.close();
    }

    // Get all OpenCVImages
    public List<OpenCVImage> getAllOpenCVImage() {
        SQLiteDatabase db = this.getReadableDatabase();

        List<OpenCVImage> openCVImageList = new ArrayList<>();

        Cursor cursor = db.query(Constants.TABLE_NAME, new String[] {Constants.KEY_ID, Constants.KEY_IMAGE, Constants.KEY_NAME, Constants.KEY_DATE, Constants.KEY_COUNT, Constants.KEY_TOTAL_AREA, Constants.KEY_AVG_SIZE, Constants.KEY_AREA_PERCENT},
                null, null, null ,null, Constants.KEY_DATE + " DESC");

        if(cursor.moveToFirst()){
            do {
                OpenCVImage openCVImage = new OpenCVImage();
                openCVImage.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
                openCVImage.setImage(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)));
                openCVImage.setName(cursor.getString(cursor.getColumnIndex(Constants.KEY_NAME)));
                openCVImage.setCount(cursor.getString(cursor.getColumnIndex(Constants.KEY_COUNT)));
                openCVImage.setTotalArea(cursor.getString(cursor.getColumnIndex(Constants.KEY_TOTAL_AREA)));
                openCVImage.setAvgSize(cursor.getString(cursor.getColumnIndex(Constants.KEY_AVG_SIZE)));
                openCVImage.setAreaPercent(cursor.getString(cursor.getColumnIndex(Constants.KEY_AREA_PERCENT)));

                // Convert Timestamp to something readable
                String formatedDate = sdf.format(new Date(cursor.getLong(cursor.getColumnIndex(Constants.KEY_DATE))).getTime());
                openCVImage.setDate(formatedDate);

                // Add to the openCVImageList
                openCVImageList.add(openCVImage);
            } while (cursor.moveToNext());
        }

        return openCVImageList;
    }

    // Get count
    public int getOpenCVImagesCount() {
        String countQuery = "SELECT * FROM " + Constants.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(countQuery, null);
        return cursor.getCount();
    }

    public boolean exportDatabase() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        /**First of all we check if the external storage of the device is available for writing.
         * Remember that the external storage is not necessarily the sd card. Very often it is
         * the device storage.
         */
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        else {
            //We use the Download directory for saving our .csv file.
            File exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "OpenCV.csv";
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file;
            PrintWriter printWriter = null;
            try
            {
                file = new File(exportDir, fileName);
                file.createNewFile();
                printWriter = new PrintWriter(new FileWriter(file));

                SQLiteDatabase db = this.getReadableDatabase(); //open the database for reading

                Cursor curCSV = db.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
                printWriter.println("ID,IMAGE_PATH,NAME,DATE,COUNT,TOTAL_AREA,AVG_SIZE,AREA_PERCENT");
                while(curCSV.moveToNext())
                {
                    String id = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_ID));
                    String image = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_IMAGE));
                    String name = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_NAME));
                    String date = sdf.format(new Date(curCSV.getLong(curCSV.getColumnIndex(Constants.KEY_DATE))).getTime());
                    String count = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_COUNT));
                    String totalArea = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_TOTAL_AREA));
                    String AvgSize = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_AVG_SIZE));
                    String AreaPercent = curCSV.getString(curCSV.getColumnIndex(Constants.KEY_AREA_PERCENT));


                    /**Create the line to write in the .csv file.
                     * We need a String where values are comma separated.
                     * The field date (Long) is formatted in a readable text. The amount field
                     * is converted into String.
                     */
                    String record = id + "," + image + "," + name + "," + date + "," + count + "," + totalArea + "," + AvgSize + "," + AreaPercent;
                    printWriter.println(record); //write the record in the .csv file
                }

                curCSV.close();
                db.close();
            }

            catch(Exception exc) {
                //if there are any exceptions, return false
                return false;
            }
            finally {
                if(printWriter != null) printWriter.close();
            }

            //If there are no errors, return true.
            filePath = exportDir.getAbsolutePath() + "/" + fileName;
            return true;
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
