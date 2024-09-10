package faster.com.ec.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import faster.com.ec.Getset.positionGetSet;
import faster.com.ec.R;
import faster.com.ec.deliveryData;

/**
 * Created by Redixbit 2 on 30-08-2016.
 */
public class positionadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    public final ArrayList<positionGetSet> positionList;
    private final Activity activity;


    public positionadapter(RecyclerView recyclerView, Activity a, ArrayList<positionGetSet> positionList) {
        activity = a;
        this.positionList = positionList;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }
        });

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_position, parent, false);
            return new MyViewHolder(itemView);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            positionGetSet data = positionList.get(position);
            ((MyViewHolder) holder).txt_delivery_caption.setText(data.getAlias() == ""? data.getAddress() : (data.getAlias() + " (" + data.getAddress() + ")"));
            ((MyViewHolder) holder).img_delivery_caption.setVisibility(deliveryData.delivery_id.equals(data.getId())? View.VISIBLE : View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return positionList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_delivery_caption;
        final ImageView img_delivery_caption;

        MyViewHolder(View view) {
            super(view);
            txt_delivery_caption = view.findViewById(R.id.delivery_caption);
            img_delivery_caption = view.findViewById(R.id.delivery_check);
        }
    }
}