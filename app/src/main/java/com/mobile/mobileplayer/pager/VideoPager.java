package com.mobile.mobileplayer.pager;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobile.mobileplayer.R;
import com.mobile.mobileplayer.SystemVideoPlayer;
import com.mobile.mobileplayer.base.BasePager;
import com.mobile.mobileplayer.domain.MediaItem;
import com.mobile.mobileplayer.utils.Utils;

import java.util.ArrayList;

public class VideoPager extends BasePager {

    //    private TextView textView;
    private ListView lv_video_pager;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;

    private ArrayList<MediaItem> mediaItems;

    private Utils utils;

    public VideoPager(Context context) {
        super(context);
        utils = new Utils();
    }

    @Override
    public View initView() {
//        textView = new TextView(context);
//        textView.setText("我是本地视频");
//        textView.setTextColor(Color.RED);
//        textView.setGravity(Gravity.CENTER);
//        textView.setTextSize(30);
        View view = View.inflate(context, R.layout.video_pager,null);
        lv_video_pager = view.findViewById(R.id.lv_video_page);
        tv_nomedia = view.findViewById(R.id.tv_nomedia);
        pb_loading = view.findViewById(R.id.pb_loading);
        //设置点击事件
        lv_video_pager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaItem mediaItem = mediaItems.get(position);

//                Intent intent = new Intent();

//                Intent intent = new Intent(context, SystemVideoPlayer.class);
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
//                context.startActivity(intent);

                //传视频列表
                Intent intent = new Intent(context, SystemVideoPlayer.class);
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");

                Bundle bundle = new Bundle();
                bundle.putSerializable("videolist",mediaItems);
                intent.putExtra("position",position);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void initData() {
        super.initData();
        getData();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //主线程
            if(mediaItems != null && mediaItems.size() > 0){
                tv_nomedia.setVisibility(View.GONE);
                pb_loading.setVisibility(View.GONE);
                //设置适配器
                lv_video_pager.setAdapter(new VideoPagerAdapter());
            }else{
                tv_nomedia.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);
            }
        }
    };

    class VideoPagerAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mediaItems.size();
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
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = View.inflate(context,R.layout.item_video,null);
                viewHolder = new ViewHolder();
                viewHolder.tv_name = convertView.findViewById(R.id.tv_name);
                viewHolder.tv_duration = convertView.findViewById(R.id.tv_duration);
                viewHolder.tv_size = convertView.findViewById(R.id.tv_size);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            MediaItem mediaItem = mediaItems.get(position);
            viewHolder.tv_name.setText(mediaItem.getName());
            viewHolder.tv_duration.setText(utils.stringForTime((int) mediaItem.getDuration()));
            viewHolder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
            return convertView;
        }
    }

    static class ViewHolder{
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }


    private void getData() {
        new Thread(){
            @Override
            public void run() {
                super.run();

//                SystemClock.sleep(2000);
                mediaItems = new ArrayList<>();
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Video.Media.DISPLAY_NAME,//在SD卡显示的名称
                        MediaStore.Video.Media.DURATION,//视频的长度
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA//视频的绝对地址
                };
                Cursor cursor = contentResolver.query(uri,objects,null,null,null);
                if(cursor != null){
                    while(cursor.moveToNext()){
                        MediaItem mediaItem = new MediaItem();
                        String name = cursor.getString(0);
                        mediaItem.setName(name);
                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);
                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);
                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        //把视频添加到列表中
                        mediaItems.add(mediaItem);
                    }
                }
                cursor.close();
                handler.sendEmptyMessage(0);
            }
        }.start();
    }
}
