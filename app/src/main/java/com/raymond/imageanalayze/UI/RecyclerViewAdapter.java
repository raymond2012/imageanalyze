package com.raymond.imageanalayze.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.raymond.imageanalayze.Activities.ImageDetailActivity;
import com.raymond.imageanalayze.Data.DatabaseHandler;
import com.raymond.imageanalayze.Model.OpenCVImage;
import com.raymond.imageanalayze.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<OpenCVImage> openCVImageList;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog dialog;
    private LayoutInflater inflater;

    public RecyclerViewAdapter(Context context, List openCVImageList) {
        this.context = context;
        this.openCVImageList = openCVImageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item, parent, false);
        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OpenCVImage openCVImage = openCVImageList.get(position);
//        ListItem item = listItems.get(position);
        if (openCVImage.getImage() != null) {
            File file = new File(openCVImage.getImage());
//            Log.d("File", file.getAbsolutePath());
//            Uri uri = Uri.fromFile(file.getAbsoluteFile());
            Picasso.get().load(file).resize(100, 100).centerCrop().into(holder.img);
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
//                Log.d("Bitmap", String.valueOf(bitmap.getByteCount()));
//
////                holder.img.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        holder.name.setText(openCVImage.getName());
        holder.date.setText(openCVImage.getDate());
    }

    @Override
    public int getItemCount() {
        return openCVImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView img;
        public TextView name;
        public TextView date;
        public Button deleteButton;

        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            img = (ImageView) itemView.findViewById(R.id.ivImg);
            name = (TextView) itemView.findViewById(R.id.tvTitle);
            date = (TextView) itemView.findViewById(R.id.tvDate);
            deleteButton = (Button) itemView.findViewById(R.id.deleteButton);

            deleteButton.setOnClickListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    OpenCVImage openCVImage =  openCVImageList.get(position);
                    Intent intent = new Intent(context, ImageDetailActivity.class);
                    intent.putExtra("img", openCVImage.getImage());
                    intent.putExtra("name", openCVImage.getName());
                    intent.putExtra("date", openCVImage.getDate());
                    context.startActivity(intent);
                }
            });
        }

//        @Override
//        public void onClick(View view) {
//            // Get position of the row clicked or tapped.
//            int position = getAdapterPosition();
//
//            OpenCVImage openCVImage =  openCVImageList.get(position);
////            ListItem item = listItems.get(position);
//            Intent intent = new Intent(context, ImageDetailActivity.class);
//            intent.putExtra("img", openCVImage.getImage());
//            intent.putExtra("name", openCVImage.getName());
//            intent.putExtra("date", openCVImage.getDate());
////            intent.putExtra("count", openCVImage.getCount());
//            context.startActivity(intent);
//
//            Toast.makeText(context, openCVImage.getName(), Toast.LENGTH_SHORT).show();
//        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
//                case R.id.editButton:
//                    break;
                case R.id.deleteButton:
                    int position = getAdapterPosition();
                    OpenCVImage openCVImage = openCVImageList.get(position);
                    deleteItem(openCVImage.getId());
                    break;
            }
        }

        public void deleteItem (final int id) {
            // Create an AlertDialog
            alertDialogBuilder = new AlertDialog.Builder(context);
            inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.confirmation_dialog, null);

            Button noButton = (Button) view.findViewById(R.id.noButton);
            Button yesButton = (Button) view.findViewById(R.id.yesButton);

            alertDialogBuilder.setView(view);
            dialog = alertDialogBuilder.create();
            dialog.show();

            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Delete the item.
                    DatabaseHandler db = new DatabaseHandler(context);
                    db.deleteOpenCVImage(id);
                    openCVImageList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    dialog.dismiss();
                }
            });
        }
    }
}
