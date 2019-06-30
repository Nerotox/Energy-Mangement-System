package at.karrer.energymanagementsystem.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import at.karrer.energymanagementsystem.R;
import at.karrer.energymanagementsystem.adapters.SitemapAdapter;
import at.karrer.energymanagementsystem.callbacks.SwipeToDeleteCallback;
import at.karrer.energymanagementsystem.fragments.NewSitemapDialogFragment;
import at.karrer.energymanagementsystem.model.Item;
import at.karrer.energymanagementsystem.model.Sitemap;

public class SitemapActivity extends AppCompatActivity implements SitemapAdapter.OnSitemapListener, NewSitemapDialogFragment.NewSitemapDialogListener {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Sitemap> sitemaps = new ArrayList<>();
    FloatingActionButton floatingActionButton;

    private static final String TAG = "SitemapActivity";
    Context context;
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        mPrefs = getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("sitemaps", "");
        Sitemap[] sitemapArray = gson.fromJson(json, Sitemap[].class);
        setTitle("Energy Management System");
        if(sitemapArray != null) {
            sitemaps.addAll(Arrays.asList(sitemapArray));
        }
        //sitemaps.add(new Sitemap("Weichselgasse 1", "10.84.245.179"));


        recyclerView = findViewById(R.id.sitemapRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SitemapAdapter(sitemaps, this, this, mPrefs);
        recyclerView.setAdapter(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback((SitemapAdapter) mAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newSitemapDialogFragment = new NewSitemapDialogFragment();
                newSitemapDialogFragment.show(getSupportFragmentManager(), "Neue Sitemap");
                newSitemapDialogFragment.setCancelable(false);
        }});
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        EditText sitemapLabel = dialog.getDialog().findViewById(R.id.newSitemapLabel);
        EditText ipAddrLabel = dialog.getDialog().findViewById(R.id.newSitemap_ip_addrLabel);
        Sitemap sitemap = new Sitemap(sitemapLabel.getText().toString(), ipAddrLabel.getText().toString());

        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        sitemaps.add(sitemap);
        String json = gson.toJson(sitemaps.toArray());
        prefsEditor.putString("sitemaps", json);
        prefsEditor.apply();
        recyclerView.invalidate();
    }

    @Override
    public void onSitemapClick(int position) {
        Log.d(TAG, "onSitemapClick: " + position);
        Sitemap sitemap = sitemaps.get(position);
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("sitemap", sitemap);
        startActivity(intent);
    }
}
