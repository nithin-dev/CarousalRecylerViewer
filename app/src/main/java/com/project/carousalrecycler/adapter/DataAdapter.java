package com.project.carousalrecycler.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.project.carousalrecycler.model.ItemModel;
import com.project.carousalrecycler.R;
import com.project.carousalrecycler.custom_swipedialog.CustomSwipeDismissDialog;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {
    private ArrayList<ItemModel> datalist;
    private final Context context;

    public DataAdapter(ArrayList<ItemModel> datalist, Context context) {
        this.context = context;
        this.datalist = datalist;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, final int i) {

        Glide.with(this.context)
                .load(datalist.get(i).getImageurl()).fitCenter().into(viewHolder.image);

        viewHolder.image.setOnClickListener(v -> {
            @SuppressLint("InflateParams") View dialog = LayoutInflater.from(context).inflate(R.layout.dialogscreenlayout, null);
            ImageView selectedimage;
            LinearLayout layoutdialog;


            Snackbar snackbar = Snackbar
                    .make(viewHolder.linear_layout, datalist.get(i).getName(), Snackbar.LENGTH_SHORT);
            View snackbarview = snackbar.getView();
            FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)snackbarview.getLayoutParams();
            params.gravity = Gravity.TOP;
            snackbarview.setLayoutParams(params);
            snackbarview.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            snackbar.setTextColor(ContextCompat.getColor(context,R.color.black));

            snackbar.show();

            new CustomSwipeDismissDialog.Builder(context)
                    .setOnSwipeDismissListener((view, direction) -> Toast.makeText(context, "Swiped: " + direction, Toast.LENGTH_SHORT).show())

                    .setView(dialog)
                    .build()
                    .show();
            layoutdialog = dialog.findViewById(R.id.layoutdialog);
            selectedimage = dialog.findViewById(R.id.selectedimage);
            Glide.with(this.context).load(datalist.get(i).getImageurl()).into(selectedimage);

        });

    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        LinearLayout linear_layout;


        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.image);
            linear_layout = view.findViewById(R.id.linear_layout);

        }
    }

}
