package com.example.rxjavademo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.rxjavademo.base.BaseActivity;
import com.example.rxjavademo.operator.OperatorActivity;
import com.example.rxjavademo.processor.ProcessorActivity;
import com.example.rxjavademo.subject.SubjectActivity;
import com.example.rxjavademo.thread.ThreadActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * RxJava 入门
     * @param v
     */
    public void onTest1(View v) {
        startActivity(new Intent(this, BaseActivity.class));
    }

    /**
     * RxJava线程
     * @param v
     */
    public void onTest2(View v) {
        startActivity(new Intent(this, ThreadActivity.class));
    }

    /**
     * RxJava 操作符
     * @param v
     */
    public void onTest3(View v) {
        startActivity(new Intent(this, OperatorActivity.class));
    }


    public void onSubject(View v) {
        startActivity(new Intent(this, SubjectActivity.class));
    }

    public void onProcessor(View v) {
        startActivity(new Intent(this, ProcessorActivity.class));
    }

}