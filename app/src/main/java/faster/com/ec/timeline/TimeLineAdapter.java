package faster.com.ec.timeline;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;
import java.util.Objects;

import faster.com.ec.R;
import faster.com.ec.fooddelivery.MainActivity;

/**
 * Created by RedixbitUser on 3/22/2018.
 */

public class TimeLineAdapter extends RecyclerView.Adapter<TimeLineViewHolder> {

    private final List<TimeLineModel> mFeedList;
    private Context mContext;
    private final boolean mWithLinePadding;

    public TimeLineAdapter(List<TimeLineModel> feedList, boolean withLinePadding) {
        mFeedList = feedList;
        mWithLinePadding = withLinePadding;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position,getItemCount());
    }

    @NonNull
    @Override
    public TimeLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        View view;

        view = mLayoutInflater.inflate(mWithLinePadding ? R.layout.item_timeline_line_padding : R.layout.item_timeline, parent, false);


        return new TimeLineViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeLineViewHolder holder, int position) {
        //for (i = 0; i < menulist.size(); i++) { en caso de un nuevo error
        try {
            TimeLineModel timeLineModel = mFeedList.get(position);

            if(timeLineModel.getStatus() == OrderStatus.INACTIVE) {
                holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_inactive, android.R.color.darker_gray));
                holder.mMessage.setTextColor(mContext.getResources().getColor(R.color.lightGrey));

            } else if(timeLineModel.getStatus() == OrderStatus.ACTIVE) {
                holder.mTimelineView.setMarker(VectorDrawableUtils.getDrawable(mContext, R.drawable.ic_marker_active, R.color.colormark));
                holder.mMessage.setTextColor(mContext.getResources().getColor(R.color.black));

            } else {
                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_marker), ContextCompat.getColor(mContext, R.color.red));
            }

            if(!timeLineModel.getDate().isEmpty()) {
                holder.mDate.setVisibility(View.VISIBLE);
                holder.mDate.setTypeface(MainActivity.tf_opensense_regular);
                holder.mDate.setText(DateTimeUtils.parseDateTime(timeLineModel.getDate(), "dd-MM-yyyy HH:mm", "hh:mm a, dd-MMM-yyyy"));
            } else {
                holder.mDate.setVisibility(View.GONE);
            }
            holder.mMessage.setText(timeLineModel.getMessage());
            holder.mMessage.setTypeface(MainActivity.tf_opensense_regular);

        } catch(Exception e) {
            Log.e("Error_timeLineModel", Objects.requireNonNull(e.getMessage()));
        }
    }

    @Override
    public int getItemCount() {
        return (mFeedList!=null? mFeedList.size():0);
    }

}
