package com.mobile.mobileplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mobile.mobileplayer.base.BasePager;

public class MyFragment extends Fragment {

    private static BasePager basePager;

    public MyFragment(){

    }

    public static final MyFragment newInstance(BasePager page) {
        MyFragment fragment = new MyFragment();
        Bundle bundle = new Bundle();
        basePager = page;
        fragment.setArguments(bundle);
        return fragment ;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (basePager != null){
            return basePager.rootView;
        }
        return null;
    }
}
