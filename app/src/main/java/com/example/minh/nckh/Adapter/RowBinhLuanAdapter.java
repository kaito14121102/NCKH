package com.example.minh.nckh.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.minh.nckh.R;
import com.github.library.bubbleview.BubbleTextView;

import java.util.ArrayList;

/**
 * Created by LaVanDuc on 4/17/2018.
 */

public class RowBinhLuanAdapter extends BaseAdapter {
    Context myContext;
    ArrayList<String> mang;

    public RowBinhLuanAdapter(Context myContext, ArrayList<String> mang) {
        this.myContext = myContext;
        this.mang = mang;
    }

    @Override
    public int getCount() {
        return mang.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView=inflater.inflate(R.layout.row_binh_luan,null);

        BubbleTextView txt_binh_luan=convertView.findViewById(R.id.txtbinhluan);

        String bl=mang.get(position);
        txt_binh_luan.setText(bl);
        return convertView;
    }
}
