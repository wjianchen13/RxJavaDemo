package com.example.rxjavademo.subject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subjects.UnicastSubject;

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
     * ReplaySubject
     * 该Subject会接收数据，当被订阅时，将所有接收到的数据全部发送给订阅者。
     * @param v
     */
    public void onTest1(View v) {
        ReplaySubject<String> subject = ReplaySubject.create();
        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");
        subject.onComplete();


        // both of the following will get the onNext/onComplete calls from above
        Disposable d1 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer1 onNext: " + integer);
            }
        });
        Disposable d2 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer2 onNext: " + integer);
            }
        });
    }

    /**
     * BehaviorSubject
     * 当Observer订阅了一个BehaviorSubject，它一开始就会释放Observable最近释放的一个数据对象，当还没有任
     * 何数据释放时，它则是一个默认值。接下来就会释放Observable释放的所有数据。如果Observable因异常终止，
     * BehaviorSubject将不会向后续的Observer释放数据，但是会向Observer传递一个异常通知。
     *
     * @param v
     */
    public void onTest2(View v) {
        // observer will receive all 4 events (including "default").
//        BehaviorSubject<String> subject = BehaviorSubject.createDefault("default");
//        Disposable d1 = subject.subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String integer) throws Exception {
//                Utils.log("observer1 onNext: " + integer);
//            }
//        });
//        subject.onNext("one");
//        subject.onNext("two");
//        subject.onNext("three");

        // observer will receive the "one", "two" and "three" events, but not "zero"
//        BehaviorSubject<String> subject2 = BehaviorSubject.create();
//        subject2.onNext("zero");
//        subject2.onNext("one");
//        Disposable d2 = subject2.subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String integer) throws Exception {
//                Utils.log("observer2 onNext: " + integer);
//            }
//        });
//        subject2.onNext("two");
//        subject2.onNext("three");

        // observer will receive only onComplete
//        BehaviorSubject<String> subject3 = BehaviorSubject.create();
//        subject3.onNext("zero");
//        subject3.onNext("one");
//        subject3.onComplete();
//        Disposable d3 = subject3.subscribe(new Consumer<String>() {
//            @Override
//            public void accept(String integer) throws Exception {
//                Utils.log("observer3 onNext: " + integer);
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//                if (throwable != null) {
//                    Utils.log("observer3 onError: " + throwable.getMessage());
//                }
//            }
//        }, new Action() {
//            @Override
//            public void run() throws Exception {
//                Utils.log("observer3 onComplete: ");
//            }
//        });

        // observer will receive only onError
        BehaviorSubject<String> subject4 = BehaviorSubject.create();
        subject4.onNext("zero");
        subject4.onNext("one");
        subject4.onError(new RuntimeException("error"));
        Disposable d4 = subject4.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer4 onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable != null) {
                    Utils.log("observer4 onError: " + throwable.getMessage());
                }
            }
        });
    }

    /**
     * PublishSubject
     * PublishSubject仅会向Observer释放在订阅之后Observable释放的数据。
     * @param v
     */
    public void onTest3(View v) {
        PublishSubject<String> subject = PublishSubject.create();
//        subject.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());
        // observer1 will receive all onNext and onComplete events
        Disposable d1 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log("observer1 onNext: " + s);
            }
        });
        subject.onNext("one");
        subject.onNext("two");

        // observer2 will only receive "three" and onComplete
        Disposable d2 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer2 onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable != null) {
                    Utils.log("observer2 onError: " + throwable.getMessage());
                }
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("observer2 onComplete: ");
            }
        });
        subject.onNext("three");
        subject.onComplete();

    }

    /**
     * AsyncSubject
     * AsyncSubject仅释放Observable释放的最后一个数据，并且仅在Observable完成之后。然而如果当Observable
     * 因为异常而终止，AsyncSubject将不会释放任何数据，但是会向Observer传递一个异常通知。
     *
     * @param v
     */
    public void onTest4(View v) {
        AsyncSubject<String> subject = AsyncSubject.create();
        subject.onNext("one");
        subject.onNext("two");

        // onComplete之后发送的数据无效，只有在onComplete之后才会发送最后一个数据·
        // 如果不发送onComplete事件也不会释放最后一个数据。
        // output number is 2
        // 如果因为异常而终止，不会发送任何数据，只会传递onError事件
        subject.onComplete();
        subject.onNext("three");

        Disposable d1 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log("observer1 onNext: " + s);
            }
        });
    }

    /**
     * SerializedSubject
     * RxJava之并发处理（SerializedSubject）
     * https://www.cnblogs.com/cmlblog/p/7663116.html
     * SerializedSubject非public，不能直接访问
     * 解决办法
     * 1. 不要直接实例化 SerializedSubject。
     * 2. 使用 RxJava 提供的 PublishSubject、BehaviorSubject 或 ReplaySubject 等Subject的具体实现，并在需要的时候通过 toSerialized() 方法来获取线程安全的版本。
     * @param v
     */
    public void onTest5(View v) {
        // 同一时间可以有多个线程同时调用onNext
//        final PublishSubject<Integer> subject = PublishSubject.create();
//        subject.subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Exception {
//                Utils.log("observer1 onNext value:" + integer + " ,threadId:" + Thread.currentThread().getId());
//                int a = 0;
//                for(int i = 0; i < 1000; i ++) {
//                    a = a + i;
//                }
//                Utils.log("observer2 onNext value:" + integer + " ,threadId:" + Thread.currentThread().getId() + "  a: " + a);
//            }
//        });
//
//        for (int i = 0; i < 20; i++) {
//            final int value = i;
//            new Thread() {
//                public void run() {
//                    subject.onNext((int) (value * 10000 + Thread.currentThread().getId()));
//                };
//            }.start();
//        }
//        try {
//            Thread.sleep(20000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // 同一时间只能有一个线程同时调用onNext
        PublishSubject<Integer> publishSubject = PublishSubject.create();
        final Subject<Integer> subject = publishSubject.toSerialized();
        subject.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("observer1 onNext value:" + integer + " ,threadId:" + Thread.currentThread().getId());
                int a = 0;
                for(int i = 0; i < 1000; i ++) {
                    a = a + i;
                }
                Utils.log("observer2 onNext value:" + integer + " ,threadId:" + Thread.currentThread().getId() + "  a: " + a);
            }
        });

        for (int i = 0; i < 20; i++) {
            final int value = i;
            new Thread() {
                public void run() {
                    subject.onNext((int) (value * 10000 + Thread.currentThread().getId()));
                };
            }.start();
        }
        try {
            Thread.sleep(20000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * UnicastSubject
     * 仅支持订阅一次的Subject,如果多个订阅者试图订阅这个Subject，若该subject未terminate，将会收到IllegalStateException，
     * 若已经terminate，那么只会执行onError或者onComplete方法。
     * @param v
     */
    public void onTest6(View v) {
        UnicastSubject<Integer> unicastSubject = UnicastSubject.create();
        unicastSubject.onNext(1);
        unicastSubject.onNext(2);
        unicastSubject.onComplete();
        unicastSubject.onNext(3);
        Disposable d1 = unicastSubject.subscribe(
                integer -> {
                    Utils.log("observer1 onNext: " + integer);
                },
                throwable -> {
                    Utils.log("observer1 onError: ");
                }
        );
        Disposable d2 = unicastSubject.subscribe(
                integer -> {
                    Utils.log("observer2 onNext: " + integer);
                },
                throwable -> {
                    Utils.log("observer2 onError: ");
                }
        );

    }
    
}

    






