package com.example.rxjavademo.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * RxJava 使用例子
 */
public class ExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
    }

    /**
     * 后台执行耗时操作，实时通知 UI 更新
     * @param v
     */
    public void onTest1(View v) {
        startActivity(new Intent(this, ExampleActivity1.class));
    }

    /**
     *
     * @param v
     */
    public void onTest2(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest3(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest4(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest5(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest6(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest7(View v) {

    }

    /**
     *
     * @param v
     */
    public void onTest8(View v) {

    }

    
}

    






