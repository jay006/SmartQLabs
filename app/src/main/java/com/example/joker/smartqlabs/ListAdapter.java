package com.example.joker.smartqlabs;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

/**
 * Created by joker on 22/9/17.
 */

public class ListAdapter extends BaseAdapter {

    private static final String TAG = ListAdapter.class.getSimpleName();
    private ArrayList<QueueModel> queues;
    private Context context;
    private Handler handler;
    private int lastPosition = -1;

    private EditContactDetailsCallBack callback;

    public ListAdapter(ArrayList<QueueModel> queues, Context context, Handler uihandler) {
        this.queues = queues;
        this.context = context;
        this.handler = uihandler;
    }

    @Override
    public int getCount() {
        return queues == null ? 0 : queues.size();
    }

    @Override
    public Object getItem(int position) {
        return queues.size() == 0 ? null : queues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return queues.get(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.id = (TextView) convertView.findViewById(R.id.idTV);
            viewHolder.time = (TextView) convertView.findViewById(R.id.timeTV);
            viewHolder.qno = (TextView) convertView.findViewById(R.id.qnoTV);
            viewHolder.shopImage = (ImageView) convertView.findViewById(R.id.shopImgae);
            viewHolder.shop_name = (TextView) convertView.findViewById(R.id.shopName);
            viewHolder.shop_info = (TextView) convertView.findViewById(R.id.shopInfo);
            viewHolder.navBtn = (Button) convertView.findViewById(R.id.navBtn);
            viewHolder.cancleBtn = (Button) convertView.findViewById(R.id.cancelBtn);
            viewHolder.otpTV = (TextView)convertView.findViewById(R.id.otp_codeTV);
            viewHolder.qCodeTV = (TextView)convertView.findViewById(R.id.q_codeTV);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //setting up the queue details to the card ,,,,,,
        viewHolder.id.setText("Counter No."+ queues.get(position).getCounter_no());
        viewHolder.time.setText(queues.get(position).getTime() +"min left");
        viewHolder.qno.setText(queues.get(position).getQueue_no()+"th position");
        viewHolder.shop_name.setText(queues.get(position).getShop_name());
        viewHolder.shop_info.setText(queues.get(position).getShop_info());
        viewHolder.otpTV.setText("OTP: "+queues.get(position).getOtp());
        viewHolder.qCodeTV.setText("Q-Code: "+queues.get(position).getqCode());
        Glide.with(parent.getContext())
                .load(queues.get(position).getUrl())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.shopImage);


        //on cancel button click
        viewHolder.cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (callback != null) {
                    callback.cancelQueue(queues.get(position));
                }

            }
        });

        viewHolder.navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(callback!=null){
                    callback.navPlace(queues.get(position));
                }

            }
        });


        //for the animation of the list items.
//        Animation animation = AnimationUtils.loadAnimation(parent.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        convertView.startAnimation(animation);
//        lastPosition = position;

        return convertView;

    }

    public void setCallback(EditContactDetailsCallBack callback) {
        this.callback = callback;
    }


    //making an interface to update the ListAdapter
    interface EditContactDetailsCallBack {
        void cancelQueue(QueueModel contact);
        void navPlace(QueueModel contact);
    }

    //ViewHolder class
    private class ViewHolder {
        TextView id = null, shop_name = null, shop_info = null, time = null, qno = null,otpTV=null,qCodeTV;
        Button cancleBtn = null, navBtn = null;
        ImageView shopImage = null;
    }

}
