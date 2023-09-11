package com.example.rxjavademo.processor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;

import java.lang.ref.WeakReference;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
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
public class ProcessorActivity extends AppCompatActivity {

    private static final String TAG = ProcessorActivity.class.getName();
    PublishProcessor subject;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processor);
        Log.d(TAG, Thread.currentThread().getName());
    }

    /**
     * PublishProcessor
     * @param v
     */
    public void onTest1(View v) {
//        PublishProcessor subject = PublishProcessor.create();
        subject = PublishProcessor.create();
        subject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     *
     * @param v
     */
    public void onTest2(View v) {
        subject.onNext(0);
        subject.onNext(1);

        Disposable disposable = subject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                TextView tvTest = null;
                tvTest.setText("");
                Log.d(TAG, "accept: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {

            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Log.d(TAG, "run: onComplete");
            }
        });
        subject.onNext(3);
//        disposable.dispose();
        mCompositeDisposable.add(disposable);
        int size = mCompositeDisposable.size();
//        mCompositeDisposable.dispose();
        subject.onNext(4);

    }

    /**
     *
     * @param v
     */
    public void onTest3(View v) {
        PublishProcessor subject1 = subject;
    }

    /**
     *
     * @param v
     */
    public void onTest4(View v) {

//                PublishProcessor<Integer> processor = PublishProcessor.create();
//                WeakReference<Observer> weakReference = new WeakReference<>(new Observer());
//
//                processor.subscribe(
//                        value -> {
//                            Observer observer = weakReference.get();
//                            if (observer != null) {
//                                observer.onNext(value);
//                            }
//                        },
//                        error -> {
//                            Observer observer = weakReference.get();
//                            if (observer != null) {
//                                observer.onError(error);
//                            }
//                        },
//                        () -> {
//                            Observer observer = weakReference.get();
//                            if (observer != null) {
//                                observer.onComplete();
//                            }
//                        }
//                );
//
//                processor.onNext(1);
//                processor.onNext(2);
//
//                // 取消订阅后等待一段时间
//                Disposable disposable = processor.subscribe();
//                disposable.dispose();
//                Thread.sleep(5000);
//
//                // 检查观察者对象是否被回收
//                Observer observer = weakReference.get();
//                if (observer == null) {
//                    System.out.println("Observer has been garbage collected.");
//                } else {
//                    System.out.println("Observer is still reachable.");
//                }
//            }


    }
    
}

//static class Observer {
//    public void onNext(int value) {
//        System.out.println("Observer: " + value);
//    }
//
//    public void onError(Throwable error) {
//        System.err.println("Error: " + error);
//    }
//
//    public void onComplete() {
//        System.out.println("Observer completed");
//    }
//}

    






