package com.medikart.otp.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.medikart.otp.Interface.ItemClickListener;
import com.medikart.otp.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    public TextView food_name;
    public ImageView food_image,fav_image,quick_cart;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public FoodViewHolder(View itemView) {
        super(itemView);

        food_name = (TextView)itemView.findViewById(R.id.medicine_name);
        food_image = (ImageView)itemView.findViewById(R.id.medicine_image);
        fav_image = (ImageView)itemView.findViewById(R.id.fav);
        quick_cart=(ImageView)itemView.findViewById(R.id.btn_Quick_cart);


        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
