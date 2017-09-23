package com.example.joker.sqltest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

public class SuggestionAdapeter extends BaseAdapter {

    private ArrayList<SuggestionModel> suggestions = new ArrayList<>();
    private Context context;

    int lastPosition = -1;
    private SuggestionCallBack callback;

    public SuggestionAdapeter(ArrayList<SuggestionModel> suggestions, Context context) {
        this.suggestions = suggestions;
        this.context = context;
    }

    @Override
    public int getCount() {
        return suggestions == null ? 0 : suggestions.size();
    }

    @Override
    public Object getItem(int position) {
        return suggestions.size() == 0 ? null : suggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return suggestions.get(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.shop_name = (TextView) convertView.findViewById(R.id.shopName);
            viewHolder.shop_info = (TextView) convertView.findViewById(R.id.shopInfo);
            viewHolder.shopImage = (ImageView) convertView.findViewById(R.id.shopImgae);
            viewHolder.shop_type = (TextView) convertView.findViewById(R.id.shopType);
            viewHolder.navBtn = (Button) convertView.findViewById(R.id.navigateBtn);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.shop_name.setText(suggestions.get(position).getShop_name());
        viewHolder.shop_info.setText(suggestions.get(position).getShop_info());
        viewHolder.shop_type.setText(suggestions.get(position).getType());

        Glide.with(parent.getContext())
                .load(suggestions.get(position).getUrl())
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.shopImage);

        viewHolder.navBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback!=null){
                    callback.openGmap(suggestions.get(position));
                }
            }
        });

        Animation animation = AnimationUtils.loadAnimation(parent.getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;

        return convertView;

    }


    public void setCallback(SuggestionCallBack callback) {
        this.callback = callback;
    }

    //making an interface to update to call StartMap()
    interface SuggestionCallBack {
        void openGmap(SuggestionModel suggestion);
    }



    class ViewHolder {
        TextView shop_info = null, shop_name = null, shop_type = null;
        ImageView shopImage = null;
        Button navBtn = null;

    }
}
