package at.karrer.energymanagementsystem.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;

import at.karrer.energymanagementsystem.R;
import at.karrer.energymanagementsystem.adapters.ItemAdapter;
import at.karrer.energymanagementsystem.model.Item;
import at.karrer.energymanagementsystem.model.Sitemap;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ItemActivity extends AppCompatActivity implements ItemAdapter.OnItemListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    View progressOverlay;
    SwipeRefreshLayout swipeRefreshLayout;
    Context context;
    Sitemap sitemap;
    ItemActivity itemActivity;

    ArrayList<Item> items = new ArrayList<>();
    private static final String TAG = "ItemActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_activity);
        progressOverlay = findViewById(R.id.progress_overlay);
        sitemap = getIntent().getExtras().getParcelable("sitemap");
        setTitle(sitemap.getName());
        context = this;
        Log.d(TAG, "onCreate: ItemActivity");

        itemActivity = this;
        swipeRefreshLayout = findViewById(R.id.swipeItemsRefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        new SitemapLoadItemsTask(itemActivity).execute();
                    }
                }
        );

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        //RecyclerView setup
        recyclerView = findViewById(R.id.itemRecyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        new SitemapLoadItemsTask(itemActivity).execute();
        //Divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    @Override
    public void onItemClick(int position) {
        Log.d(TAG, "onItemClick: Clicked item");
        Intent intent = new Intent(this, DetailViewActivity.class);
        intent.putExtra("item", items.get(position));
        intent.putExtra("ip", sitemap.getIp());
        startActivity(intent);
    }

    private class SitemapLoadItemsTask extends AsyncTask<Void, Void, Void> {

        ItemActivity itemActivity;

        public SitemapLoadItemsTask(ItemActivity itemActivity) {
            this.itemActivity = itemActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ItemActivity.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ItemActivity.animateView(progressOverlay, View.GONE, 0, 200);
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Sitemap sitemap = getIntent().getExtras().getParcelable("sitemap");
            items = sitemap.getItems();
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://" + sitemap.getIp() + ":8080/ems/item/all").newBuilder();
            String url = urlBuilder.build().toString();
            Log.d(TAG, "doInBackground: URL: " + url);

            Request request = new Request.Builder().url(url).build();
            OkHttpClient client = new OkHttpClient();
            try {
                Response response = client.newCall(request).execute();
                final String stringResponse = response.body().string();
                Log.d(TAG, "onResponse: " + stringResponse);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Item[] itemArray = gson.fromJson(stringResponse, Item[].class);
                items.addAll(Arrays.asList(itemArray));
                sitemap.setItems(items);
                Log.d(TAG, "doInBackground: Items: " + items.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //set adapter for data.
            mAdapter = new ItemAdapter(items, itemActivity, context);
            //need to run setAdapter on UI Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setAdapter(mAdapter);
                }
            });


            return null;
        }
    }
}
