package faster.com.ec.fooddelivery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import faster.com.ec.Getset.myOrderGetSet;
import faster.com.ec.R;
import faster.com.ec.utils.sqliteHelper;

import static faster.com.ec.fooddelivery.MainActivity.changeStatsBarColor;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_medium;
import static faster.com.ec.fooddelivery.MainActivity.tf_opensense_regular;

public class MyOrderPage extends AppCompatActivity {
    private ListView list_notification;
    private ArrayList<myOrderGetSet> data;
    private notificationAdapter adapter;
//    AdRequest adRequest;
//    AdView mAdView;
    faster.com.ec.utils.sqliteHelper sqliteHelper;
//    private InterstitialAd mInterstitialAd;
    private String key;
    private static final String MY_PREFS_NAME = "Fooddelivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myorder);
        getSupportActionBar().hide();
        changeStatsBarColor(MyOrderPage.this);

        initialization();

        new GetDataAsyncTask().execute();

    }


    class GetDataAsyncTask extends AsyncTask<Void, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MyOrderPage.this);
            progressDialog.setMessage(getString(R.string.loading));
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            gettingDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(aVoid);

            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                if (data != null) {
                    adapter = new notificationAdapter(data);
                    list_notification.setAdapter(adapter);


                } else
                    Toast.makeText(MyOrderPage.this, R.string.noorder, Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void gettingDataFromDatabase() {
        sqliteHelper = new sqliteHelper(MyOrderPage.this);
        SQLiteDatabase db1 = sqliteHelper.getWritableDatabase();

        Cursor cur;
        try {
            cur = db1.rawQuery("SELECT * FROM order_detail order by orderId desc;", null);
            data = new ArrayList<>();
            Log.e("SIZWA", "" + cur.getCount());
            if (cur.getCount() != 0) {
                if (cur.moveToFirst()) {
                    do {
                        myOrderGetSet obj = new myOrderGetSet();
                        String txt_restaurantName = cur.getString(cur.getColumnIndex("restaurantName"));
                        String txt_restaurantAddress = cur.getString(cur.getColumnIndex("restaurantAddress"));
                        String txt_orderId = cur.getString(cur.getColumnIndex("orderId"));
                        String txt_orderAmount = cur.getString(cur.getColumnIndex("orderAmount"));
                        String txt_orderTime = cur.getString(cur.getColumnIndex("orderTime"));

                        obj.setResName(txt_restaurantName);
                        obj.setResAddress(txt_restaurantAddress);
                        obj.setOrder_id(txt_orderId);
                        obj.setOrder_dateTime(txt_orderTime);
                        obj.setOrder_total(txt_orderAmount);


                        data.add(obj);
                    } while (cur.moveToNext());
                }
            }
            cur.close();
            db1.close();
//            myDbHelper.close();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());

        }

    }

    private void initialization() {
        Intent i = getIntent();
       key = i.getStringExtra("key");

        list_notification = findViewById(R.id.list_notification);
        ((TextView) findViewById(R.id.txt_title)).setTypeface(tf_opensense_regular);
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(key.equals("main")){
                    Intent iv = new Intent(MyOrderPage.this, MenuActivity.class);
                    startActivity(iv);
//                } else{
//                    onBackPressed();
//                }

            }
        });


            list_notification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("key", "orderplace");
                editor.apply();

                Intent i = new Intent(MyOrderPage.this, CompleteOrder.class);
                i.putExtra("orderid", adapter.dat.get(position).getOrder_id());
                startActivity(i);

            }
        });

    }


    private class notificationAdapter extends BaseAdapter {
        final ArrayList<myOrderGetSet> dat;
        private LayoutInflater inflater = null;

        notificationAdapter(ArrayList<myOrderGetSet> dat) {
            this.dat = dat;
            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
            if (vi == null)
                vi = inflater.inflate(R.layout.cell_order, parent, false);

            TextView txt_name = vi.findViewById(R.id.txt_name);
            txt_name.setText(dat.get(position).getResName());
            txt_name.setTypeface(tf_opensense_medium);
            TextView txt_address = vi.findViewById(R.id.txt_address);
            txt_address.setText(dat.get(position).getResAddress());
            txt_address.setTypeface(tf_opensense_regular);

            TextView txt_order_date = vi.findViewById(R.id.txt_order_date);
            txt_order_date.setText("Fecha: " + dat.get(position).getOrder_dateTime());

            TextView txt_total_tittle = vi.findViewById(R.id.txt_total_tittle);
            TextView txt_total = vi.findViewById(R.id.txt_total);
            txt_total.setText(getString(R.string.currency) + " " + dat.get(position).getOrder_total());

            txt_order_date.setTypeface(tf_opensense_regular);
            txt_total_tittle.setTypeface(tf_opensense_regular);
            txt_total.setTypeface(tf_opensense_regular);


            return vi;
        }
    }

    @Override
    public void onBackPressed() {
        if(key.equals("main")){
            Intent iv = new Intent(MyOrderPage.this, MenuActivity.class);
            startActivity(iv);
        }
    }

}
