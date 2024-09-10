package faster.com.ec.Adapter;

import android.app.Activity;
import android.graphics.Typeface;
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

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import faster.com.ec.Getset.advertiseGetSet;
import faster.com.ec.R;

/**
 * Created by Redixbit 2 on 30-08-2016.
 */
public class advertiseadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    public final ArrayList<advertiseGetSet> advertiseList;
    private final Activity activity;


    public advertiseadapter(RecyclerView recyclerView, Activity a, ArrayList<advertiseGetSet> advertiseList) {
        activity = a;
        this.advertiseList = advertiseList;
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_advertise, parent, false);
            return new MyViewHolder(itemView);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyViewHolder) {
            advertiseGetSet data = advertiseList.get(position);
            Typeface openSansSemiBold = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Semibold.ttf");
            ((MyViewHolder) holder).txt_phone.setTypeface(openSansSemiBold);
            ((MyViewHolder) holder).txt_address.setTypeface(openSansSemiBold);
//            ((MyViewHolder) holder).txt_address.setTypeface(tf_opensense_regular);

            ((MyViewHolder) holder).txt_address.setText(data.getAddress());
            ((MyViewHolder) holder).txt_phone.setText(data.getPhone());
            ((MyViewHolder) holder).txt_open_time.setText(data.getOpen_time());
            ((MyViewHolder) holder).txt_close_time.setText(data.getClose_time());
            ((MyViewHolder) holder).advertiseCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            String image = data.getImage().replace(" ", "%20");

            AlphaAnimation anim = new AlphaAnimation(0, 1);
            anim.setInterpolator(new LinearInterpolator());
            anim.setRepeatCount(Animation.ABSOLUTE);
            anim.setDuration(5000);
            Picasso.get().load(activity.getResources().getString(R.string.link) + activity.getString(R.string.imagepath) + image)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .resize(640, 360)
                    .into(((MyViewHolder) holder).imageview);
            ((MyViewHolder) holder).imageview.startAnimation(anim);
        }


    }

    @Override
    public int getItemCount() {
        return advertiseList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        final TextView txt_address;
        final TextView txt_phone;
        final TextView txt_open_time;
        final TextView txt_close_time;
        final ImageView imageview;
        final CardView advertiseCardView;


        MyViewHolder(View view) {
            super(view);
            txt_address = view.findViewById(R.id.txt_address);
            txt_phone = view.findViewById(R.id.txt_phone);
            txt_open_time = view.findViewById(R.id.txt_open_time);
            txt_close_time = view.findViewById(R.id.txt_close_time);
            imageview = view.findViewById(R.id.image);
            advertiseCardView = view.findViewById(R.id.advertiseCardView);
        }
    }
}