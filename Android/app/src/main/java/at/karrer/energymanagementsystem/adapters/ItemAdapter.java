package at.karrer.energymanagementsystem.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import at.karrer.energymanagementsystem.R;
import at.karrer.energymanagementsystem.model.Item;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{

    Context context;
    private ArrayList<Item> mItems;
    private OnItemListener mOnItemListener;

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        Context context;
        public TextView textView;
        public ImageView categoryIcon;
        OnItemListener onItemListener;
        public ItemViewHolder(View v, OnItemListener onItemListener, Context context) {
            super(v);
            textView = itemView.findViewById(R.id.itemName);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            this.context = context;
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ItemAdapter(ArrayList<Item> items, OnItemListener onItemListener, Context context) {
        mItems = items;
        mOnItemListener = onItemListener;
        this.context = context;
    }

    public ItemAdapter(ArrayList<Item> items) {
        mItems = items;
    }

    public void setmOnItemListener(OnItemListener mOnItemListener) {
        this.mOnItemListener = mOnItemListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        // create a new view
        View v = (LayoutInflater.from(parent.getContext())
                .inflate(R.layout.items_list_row, parent, false));

        ItemViewHolder vh = new ItemViewHolder(v, mOnItemListener, context);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mItems.get(position).getLabel());

        switch (mItems.get(position).getCategory()){
            case "washingmachine":
                holder.categoryIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_category_washing, null));
                break;
            case "heating":
                holder.categoryIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_category_heating, null));
                break;
            case "poweroutlet":
                holder.categoryIcon.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_category_outlet, null));
                break;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnItemListener {
        void onItemClick(int position);
    }
}
