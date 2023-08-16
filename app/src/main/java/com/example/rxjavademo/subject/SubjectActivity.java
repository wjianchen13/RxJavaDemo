package com.example.rxjavademo.subject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * RxJava Subject
 * https://www.jianshu.com/p/d7efc29ec9d3
 * 在RxJava2.x中，官方一共为我们提供了以下几种Subject：
 *
 * ReplaySubject （释放接收到的所有数据）
 * BehaviorSubject （释放订阅前最后一个数据和订阅后接收到的所有数据）
 * PublishSubject （释放订阅后接收到的数据）
 * AsyncSubject （仅释放接收到的最后一个数据）
 * SerializedSubject（串行Subject）
 * UnicastSubject (仅支持订阅一次的Subject)
 * TestSubject（已废弃，在2.x中被TestScheduler和TestObserver替代）
 *
 */
public class SubjectActivity extends AppCompatActivity {

    private static final String TAG = SubjectActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        Log.d(TAG, Thread.currentThread().getName());
    }

    /**
     * PublishSubject
     * @param v
     */
    public void onTest1(View v) {
        PublishSubject<Object> subject = PublishSubject.create();
        subject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        // observer1 will receive all onNext and onComplete events
        subject.subscribe(observer1);
        subject.onNext("one");
        subject.onNext("two");

        // observer2 will only receive "three" and onComplete
        subject.subscribe(observer2);
        subject.onNext("three");
        subject.onComplete();

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
    
}

    






