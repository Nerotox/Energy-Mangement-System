package at.karrer.energymanagementsystem.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import at.karrer.energymanagementsystem.R;
import at.karrer.energymanagementsystem.model.Sitemap;

public class SitemapAdapter extends RecyclerView.Adapter<SitemapAdapter.SitemapViewHolder>{

    private ArrayList<Sitemap> mSitemaps;
    Context context;
    Sitemap mRecentlyDeletedItem;
    int mRecentlyDeletedItemPosition;
    SharedPreferences mPrefs;

    private OnSitemapListener mOnSitemapListener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class SitemapViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView textView;
        Context context;
        OnSitemapListener onSitemapListener;
        public SitemapViewHolder(View v, OnSitemapListener onSitemapListener, Context context) {
            super(v);
            this.context = context;
            textView = itemView.findViewById(R.id.sitemapName);
            this.onSitemapListener = onSitemapListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSitemapListener.onSitemapClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public SitemapAdapter(ArrayList<Sitemap> sitemaps, OnSitemapListener onSitemapListener, Context context, SharedPreferences mPrefs) {
        mSitemaps = sitemaps;
        mOnSitemapListener = onSitemapListener;
        this.mPrefs = mPrefs;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SitemapViewHolder onCreateViewHolder(ViewGroup parent,
                                                int viewType) {
        // create a new view
        View v = (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sitemap_list_row, parent, false));

        SitemapViewHolder vh = new SitemapViewHolder(v, mOnSitemapListener, context);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(SitemapViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mSitemaps.get(position).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mSitemaps.size();
    }

    public interface OnSitemapListener {
        void onSitemapClick(int position);
    }
    public Context getContext() {
        return context;
    }

    public void deleteItem(int position) {
        mRecentlyDeletedItem = mSitemaps.get(position);
        mRecentlyDeletedItemPosition = position;
        mSitemaps.remove(position);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mSitemaps.toArray());
        prefsEditor.putString("sitemaps", json);
        prefsEditor.apply();
        notifyItemRemoved(position);
    }
}
