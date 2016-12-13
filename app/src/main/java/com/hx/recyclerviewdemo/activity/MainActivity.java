package com.hx.recyclerviewdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aspsine.irecyclerview.OnLoadMoreListener;
import com.aspsine.irecyclerview.OnRefreshListener;
import com.hx.recyclerviewdemo.R;
import com.hx.recyclerviewdemo.adapter.HomeAdapter;
import com.hx.recyclerviewdemo.interfaces.OnViewOnClickListener;
import com.hx.recyclerviewdemo.recyclerview.DividerItemDecoration;
import com.hx.recyclerviewdemo.recyclerview.HeaderRefreshView;
import com.hx.recyclerviewdemo.recyclerview.RecyclerViewWrap;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnRefreshListener, OnLoadMoreListener, OnViewOnClickListener {


    private List<String> mDatas;
    private RecyclerViewWrap mRecyclerView;
    private HomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        mAdapter = new HomeAdapter(this,mDatas);
        mRecyclerView = (RecyclerViewWrap) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setIAdapter(mAdapter);
        setRefreshLister(mRecyclerView);
        mAdapter.setOnViewOnClickListener(this);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        View header = LayoutInflater.from(this).inflate(R.layout.layout_header,null);
        mRecyclerView.addHeaderView(header);

    }

    protected void initData()
    {
        mDatas = new ArrayList<>();
        for (int i = 'A'; i < 'z'; i++)
        {
            mDatas.add("" + (char) i);
        }
    }

    /**
     * 设置 recyclerview 刷新
     */
    protected void setRefreshLister(RecyclerViewWrap recyclerview) {
        HeaderRefreshView header = new HeaderRefreshView(this);
        header.setLayoutParams(new RelativeLayout.LayoutParams(-1, 200));

        recyclerview.setRefreshHeaderView(header);

        //设置 刷新监听
        recyclerview.setOnRefreshListener(this);
        recyclerview.setOnLoadMoreListener(this);

        //禁止头部刷新  底部加载更多
        recyclerview.setLoadMoreEnabled(false);
        recyclerview.setRefreshEnabled(true);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore(View view) {

    }

    @Override
    public void onViewClick() {
        mRecyclerView.setRefreshing(false);
    }
}
