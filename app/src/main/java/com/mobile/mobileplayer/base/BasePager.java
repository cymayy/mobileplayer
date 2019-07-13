package com.mobile.mobileplayer.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * 基类--本地视频、本地音乐、网络视频、网络音乐的基类
 */
public abstract class BasePager {

    //上下文
    public Context context;

    //视图，有各个子页面实例化的结果
    public View rootView;

    public boolean isInitData = false;

    public BasePager(Context context) {
        this.context = context;
        rootView = initView();
        isInitData = false;
    }

    /**
     * 强制子类实现该方法，实现特定的效果
     * @return
     */
    public abstract View initView();

    /**
     * 当子类需要初始化数据的时候，重写该方法，用于请求数据，或者显示数据
     */
    public void initData(){

    }
}
