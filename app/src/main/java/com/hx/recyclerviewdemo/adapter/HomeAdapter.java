package com.hx.recyclerviewdemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hx.recyclerviewdemo.R;
import com.hx.recyclerviewdemo.interfaces.OnViewOnClickListener;

import java.util.List;

/**
 * Created by hx on 2016/12/13.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>
{

    private final Context context;
    private final List<String> datas;

    private OnViewOnClickListener onViewOnClickListener;

    public void setOnViewOnClickListener(OnViewOnClickListener onViewOnClickListener) {
        this.onViewOnClickListener = onViewOnClickListener;
    }

    public HomeAdapter(Context context, List<String> datas){
        this.context = context;
        this.datas = datas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_home, parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position)
    {
        holder.tv.setText(datas.get(position));
        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onViewOnClickListener.onViewClick();
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return datas.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView tv;

        public MyViewHolder(View view)
        {
            super(view);
            tv = (TextView) view.findViewById(R.id.id_num);
        }
    }
}