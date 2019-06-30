package at.karrer.energymanagementsystem.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import at.karrer.energymanagementsystem.R;
import at.karrer.energymanagementsystem.adapters.ItemAdapter;
import at.karrer.energymanagementsystem.model.Energydata;
import at.karrer.energymanagementsystem.model.Item;
import at.karrer.energymanagementsystem.model.Sitemap;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DetailViewActivity extends AppCompatActivity {

    private static final String TAG = "DetailViewActivity";
    LineChart lineChart;
    String ip;
    View progressOverlay;
    Energydata[] energydata;
    Item item;
    Drawable drawable;
    Context context;
    TextView currentPowerTextView;
    TextView maximumLabel;
    TextView minimumLabel;
    TextView averageLabel;
    double totalValue = 0;
    double average = 0;
    double maximum = 0;
    double minimum = Integer.MAX_VALUE;
    SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);
        item = getIntent().getExtras().getParcelable("item");
        setTitle(item.getLabel());
        context = this;

        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);   //show back button

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        new LoadItemEnergydataTask().execute();
                    }
                }
        );

        currentPowerTextView = findViewById(R.id.currentPowerkWhLabel);
        maximumLabel = findViewById(R.id.maximumkWhLabel);
        minimumLabel = findViewById(R.id.minimumkWhLabel);
        averageLabel = findViewById(R.id.averagekWhLabel);

        lineChart = findViewById(R.id.lineChart);
        lineChart.setPinchZoom(true);
        lineChart.setDrawBorders(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setExtraOffsets(10, 0, 30, 0);
        //lineChart.animateX(2500, Easing.EaseOutQuad);
        lineChart.setHighlightPerDragEnabled(false);
        lineChart.setScaleYEnabled(false);

        final YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setGridColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        yAxis.setAxisLineColor(Color.TRANSPARENT);
        yAxis.setTextColor(Color.rgb(0,0,0));

        final XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawLimitLinesBehindData(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.argb(150,0,0,0));
        xAxis.disableGridDashedLine();
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        xAxis.setAxisLineColor(Color.TRANSPARENT);
        xAxis.setLabelCount(4, true);

        //xAxis.setAvoidFirstLastClipping(true);
        //xAxis.setSpaceMin(15f);


        //TODO
        //add last day, last week, last month, all time choice via setmaximum - recalc max min and avg
        //add pop up for new sitemap
        //change intent animation to swipe from right to left

        drawable = ContextCompat.getDrawable(this, R.drawable.color_fade);
        drawable.setAlpha(180);

        progressOverlay = findViewById(R.id.progress_overlay);

        ip = getIntent().getExtras().getString("ip");

        new LoadItemEnergydataTask().execute();
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

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

    private class LoadItemEnergydataTask extends AsyncTask<Void, Void, Void> {

        public LoadItemEnergydataTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //DetailViewActivity.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
            Log.d(TAG, "onPreExecute: network call");
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
           // DetailViewActivity.animateView(progressOverlay, View.GONE, 0, 200);
            Log.d(TAG, "onPostExecute: network call");
            swipeRefreshLayout.setRefreshing(false);
            lineChart.animateX(2500, Easing.EaseOutQuad);
        }
        @SuppressLint({"SimpleDateFormat"})
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground: network call");

            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://" + ip + ":8080/ems/item/" + item.getName()).newBuilder();
            String url = urlBuilder.build().toString();

            Log.d(TAG, "doInBackground: URL: " + url);

            Request request = new Request.Builder().url(url).build();

            OkHttpClient client = new OkHttpClient();
            try {
                Response response = client.newCall(request).execute();
                final String stringResponse = response.body().string();
                Log.d(TAG, "onResponse: " + stringResponse);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                energydata = gson.fromJson(stringResponse, Energydata[].class);

                Log.d(TAG, "doInBackground: Energydata: " + energydata.length);

                ArrayList<Entry> values = new ArrayList<>();
                final long referenceTS = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(energydata[0].getDate()).getTime()).getTime();
                int i = 0;
                for (Energydata energyd: energydata){
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(energyd.getDate());
                    Timestamp ts = new Timestamp(date.getTime());
                    values.add(new Entry( ts.getTime() - referenceTS,(float)energyd.getValue()));
                    i++;
                    totalValue+=energyd.getValue();
                    average = totalValue/i;
                    if(maximum < energyd.getValue()){
                        maximum = energyd.getValue();
                    }
                    if(minimum > energyd.getValue()){
                        minimum = energyd.getValue();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ValueAnimator animator = ValueAnimator.ofInt(0, (int)energydata[energydata.length-1].getValue());
                        animator.setDuration(2500);
                        animator.setInterpolator(new DecelerateInterpolator());
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @SuppressLint("SetTextI18n")
                            public void onAnimationUpdate(ValueAnimator animation) {
                                currentPowerTextView.setText(animation.getAnimatedValue().toString() + " kWh");
                            }
                        });
                        animator.start();
                        animator.addListener(new AnimatorListenerAdapter(){
                            @SuppressLint({"SetTextI18n", "DefaultLocale"})
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                currentPowerTextView.setText(String.format("%.2f", energydata[energydata.length-1].getValue())+" kWh");
                            }
                        });

                        ValueAnimator animatorMaximum = ValueAnimator.ofInt(0, (int)maximum);
                        animatorMaximum.setDuration(2500);
                        animatorMaximum.setInterpolator(new DecelerateInterpolator());
                        animatorMaximum.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                maximumLabel.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animatorMaximum.start();
                        animatorMaximum.addListener(new AnimatorListenerAdapter(){
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                maximumLabel.setText(String.format("%.2f", maximum));
                            }
                        });

                        ValueAnimator animatorMinimum = ValueAnimator.ofInt(0, (int)minimum);
                        animatorMinimum.setDuration(2500);
                        animatorMinimum.setInterpolator(new DecelerateInterpolator());
                        animatorMinimum.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                minimumLabel.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animatorMinimum.start();
                        animatorMinimum.addListener(new AnimatorListenerAdapter(){
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                minimumLabel.setText(String.format("%.2f", minimum));
                            }
                        });

                        ValueAnimator animatorAverage = ValueAnimator.ofInt(0, (int)average);
                        animatorAverage.setDuration(2500);
                        animatorAverage.setInterpolator(new DecelerateInterpolator());
                        animatorAverage.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                averageLabel.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animatorAverage.start();
                        animatorAverage.addListener(new AnimatorListenerAdapter(){

                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                averageLabel.setText(String.format("%.2f", average));
                            }
                        });

                        //currentPowerTextView.setText(energydata[energydata.length-1].getValue()+" kWh");
                    }
                });

                LineDataSet set1;
                if (lineChart.getData() != null &&
                        lineChart.getData().getDataSetCount() > 0) {
                    Log.d(TAG, "doInBackground: in if");
                    set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
                    set1.setValues(values);
                    lineChart.getData().notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "doInBackground: in else");
                    set1 = new LineDataSet(values, "Power Consumption in kWh");
                    set1.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                    set1.setCircleColor(Color.BLACK);
                    set1.setLineWidth(3f);
                    set1.setDrawCircles(false);
                    set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    set1.setCubicIntensity(0.1f);
                    set1.setValueTextSize(9f);
                    set1.setDrawValues(false);
                    set1.setDrawFilled(true);
                    set1.setFormLineWidth(1f);
                    set1.setFormSize(15.f);
                    set1.setFillDrawable(drawable);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(set1);
                    final LineData data = new LineData(dataSets);
                    lineChart.setData(data);
                    lineChart.getXAxis().getValueFormatter();
                    ValueFormatter formatter = new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            long convertedTimestamp = (long) value;
                            long originalTimestamp = referenceTS + convertedTimestamp;
                            Date d = new Date(originalTimestamp);
                            DateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm");
                            return dateFormat.format(d);
                        }
                    };
                    lineChart.getXAxis().setValueFormatter(formatter);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

    }
}
