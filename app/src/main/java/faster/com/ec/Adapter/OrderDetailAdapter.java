package faster.com.ec.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import faster.com.ec.Getset.orderDetailGetSet;
import faster.com.ec.R;

import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_medium;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;


public class OrderDetailAdapter extends BaseAdapter {
    private final ArrayList<orderDetailGetSet> dat;
    private LayoutInflater inflater = null;


    public OrderDetailAdapter(ArrayList<orderDetailGetSet> dat, Context context) {
        this.dat = dat;
        Context context1 = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public int getCount() {
        return dat.size();
    }

    @Override
    public Object getItem(int position) {
        return dat.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.cell_delivery_order_detail, parent, false);
        }

        TextView item_name = vi.findViewById(R.id.item_name);
        item_name.setAllCaps(true);
        item_name.setTypeface(tf_opensense_medium);
        item_name.setText(dat.get(position).getItemName());

         TextView itemQuantity = vi.findViewById(R.id.itemQuantity);
        itemQuantity.setTypeface(tf_opensense_regular);
        itemQuantity.setText(dat.get(position).getItemQuantity());

        TextView itemPrice = vi.findViewById(R.id.itemPrice);
        itemPrice.setTypeface(tf_opensense_regular);
        itemPrice.setText(dat.get(position).getItemPrice());


        return vi;
    }
}
