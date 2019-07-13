package com.mobile.mobileplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import com.mobile.mobileplayer.utils.LogUtil;

public class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.e("=========onCreate=========");

        TextView textView = new TextView(this);
        textView.setText("我是测试页面");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(50);
        setContentView(textView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("=========onStart=========");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("=========onResume=========");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("=========onRestart=========");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("=========onPause=========");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("=========onStop=========");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e("=========onDestroy=========");
    }
}
