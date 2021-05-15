package com.project.carousalrecycler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;

import com.project.carousalrecycler.adapter.DataAdapter;
import com.project.carousalrecycler.carousal_recycler.CenterScrollListener;
import com.project.carousalrecycler.carousal_recycler.CustomCarouselLayoutManager;
import com.project.carousalrecycler.carousal_recycler.CustomZoomPostLayoutListener;
import com.project.carousalrecycler.model.ItemModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    RecyclerView recycleritems;
    private ArrayList<ItemModel> data = new ArrayList<>();
    private DataAdapter dataAdapter;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitUI();
        context = this;
        calldata();
    }

    private void InitUI() {
        final CustomCarouselLayoutManager layoutManager = new CustomCarouselLayoutManager(CustomCarouselLayoutManager.VERTICAL,true);
        layoutManager.setPostLayoutListener(new CustomZoomPostLayoutListener());
        layoutManager.isSmoothScrolling();
        recycleritems = findViewById(R.id.recycleritems);
        recycleritems.setLayoutManager(layoutManager);
        recycleritems.setHasFixedSize(true);

    }

    private void calldata() {
        Call<List<ItemModel>> call = RetrofitClient.getInstance().getMyApi().getmodeldetail();
        call.enqueue(new Callback<List<ItemModel>>() {
            @Override
            public void onResponse(Call<List<ItemModel>> call, Response<List<ItemModel>> response) {
                data = new ArrayList<>();
                if (response.body() != null) {
                    data = new ArrayList<>(response.body());
                    dataAdapter = new DataAdapter(data, MainActivity.this);
                    recycleritems.setAdapter(dataAdapter);
                    recycleritems.addOnScrollListener(new CenterScrollListener());

                }

            }

            @Override
            public void onFailure(Call<List<ItemModel>> call, Throwable t) {

            }
        });
    }
}