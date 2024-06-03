package com.example.rxjavademo.processor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

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
    private CompositeDisposable mCompositeDisposable;
    private PublishProcessor mPublishProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompositeDisposable = new CompositeDisposable();
        setContentView(R.layout.activity_processor);
        Utils.log(Thread.currentThread().getName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
    }

    /**
     * PublishProcessor 模拟接收数据异常
     * 在LambdaSubscriber 第62行onNext方法会把异常捕获掉。
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest1(View v) {
        PublishProcessor publishProcessor = PublishProcessor.create();
        publishProcessor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        publishProcessor.onNext(0);
        publishProcessor.onNext(1);

        Disposable disposable = publishProcessor.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                TextView tvTest = null;
                tvTest.setText("");
                Utils.log("accept: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {

            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("run: onComplete");
            }
        });
        publishProcessor.onNext(3);
        mCompositeDisposable.add(disposable);
        int size = mCompositeDisposable.size();
        publishProcessor.onNext(4);
    }

    /**
     * PublishProcessor基础使用
     * 哪个时刻开始订阅，就从哪个时刻开始接收数据
     * @param v
     */
    public void onTest2(View v) {
        PublishProcessor publishProcessor = PublishProcessor.create();
        publishProcessor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        publishProcessor.onNext(0);
        publishProcessor.onNext(1);

        Disposable disposable = publishProcessor.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("accept: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {

            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("run: onComplete");
            }
        });
        publishProcessor.onNext(3);
        mCompositeDisposable.add(disposable);
        int size = mCompositeDisposable.size();
        publishProcessor.onNext(4);
    }

    /**
     * 创建一个PublishProcessor，分开设置
     * mPublishProcessor先设置observeOn(AndroidSchedulers.mainThread())，然后在调用subscribe()切换线程会失败，切换不到主线程
     * 应该要在subscribe()再调用observeOn(AndroidSchedulers.mainThread())才能切换到主线程
     * @param v
     */
    public void onTest3(View v) {
        mPublishProcessor = PublishProcessor.create();
        mPublishProcessor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Disposable disposable = mPublishProcessor.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("accept thread name: " + Utils.getThreadName() + "   accept: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {

            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("run: onComplete");
            }
        });
        mCompositeDisposable.add(disposable);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.log("onNext thread name: " + Utils.getThreadName());
                mPublishProcessor.onNext(3);
                mPublishProcessor.onNext(4);
            }
        }).start();
    }

    /**
     * 创建一个PublishProcessor，链式调用
     * mPublishProcessor先设置observeOn(AndroidSchedulers.mainThread())，然后在调用subscribe()切换线程会失败，切换不到主线程
     * 应该要在subscribe()再调用observeOn(AndroidSchedulers.mainThread())才能切换到主线程
     * @param v
     */
    public void onTest4(View v) {
        PublishProcessor<String>  publishProcessor = PublishProcessor.create();
        Disposable disposable = publishProcessor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String str) throws Exception {
                        Utils.log("accept thread name: " + Utils.getThreadName() + "   accept: " + str);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {

                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Utils.log("run: onComplete");
                    }
                });


        mCompositeDisposable.add(disposable);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.log("onNext thread name: " + Utils.getThreadName());
                publishProcessor.onNext("3");
                publishProcessor.onNext("4");
            }
        }).start();
    }

    /**
     * 主线程发送数据,主线程接收
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest5(View v) {
        // 创建PublishProcessor
        PublishProcessor<String> processor = PublishProcessor.create();

        processor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        // 在主线程处理数据
        Disposable disposable = processor.observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Processing data: " + data);
                        },
                        error -> error.printStackTrace(),
                        () -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Done processing data.");
                        }
                );
        mCompositeDisposable.add(disposable);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.log("onNext thread name: " + Utils.getThreadName());
                processor.onNext("3");
                processor.onNext("4");
            }
        }).start();
    }

    /**
     * 主线程发送数据,主线程接收
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest6(View v) {
        PublishProcessor<String> processor = PublishProcessor.create();

        processor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        // 在主线程处理数据
        Disposable disposable =  processor.observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Processing data: " + data);
                        },
                        error -> error.printStackTrace(),
                        () -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Done processing data.");
                        }
                );
        mCompositeDisposable.add(disposable);

        io.reactivex.schedulers.Schedulers.io().scheduleDirect(() -> {
            Utils.log("onNext thread name: " + Utils.getThreadName());
            processor.onNext("Data from child thread");
            processor.onComplete();
        });
    }

    /**
     * 主线程发送数据
     * @param v
     */
    public void onTest7(View v) {
        PublishProcessor<String> processor = PublishProcessor.create();

        processor.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        // 在主线程处理数据
        Disposable disposable =  processor.observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Processing data: " + data);
                        },
                        error -> error.printStackTrace(),
                        () -> {
                            Utils.log("subscribe thread name: " + Utils.getThreadName());
                            System.out.println("Done processing data.");
                        }
                );
        mCompositeDisposable.add(disposable);

        processor.onNext("Data from child thread");
    }

    
}



    






