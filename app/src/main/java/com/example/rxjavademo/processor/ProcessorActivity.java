package com.example.rxjavademo.processor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.AsyncProcessor;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.processors.ReplayProcessor;
import io.reactivex.processors.UnicastProcessor;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava Processor
 * Processor和Subject用法一样，只是Processor支持被压
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
     * 异常之后后续的数据不会继续发送
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
                Utils.log("onError: " + throwable.getMessage());
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
     * 创建一个PublishProcessor，分开设置,切换不到主线程，参考test3和test4
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

        // 下面的写法可以
//        mPublishProcessor = PublishProcessor.create();
//        Flowable f = mPublishProcessor.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread());
//
//        Disposable disposable = f.subscribe(new Consumer<Integer>() {
//            @Override
//            public void accept(Integer integer) throws Exception {
//                Utils.log("accept thread name: " + Utils.getThreadName() + "   accept: " + integer);
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) {
//
//            }
//        }, new Action() {
//            @Override
//            public void run() throws Exception {
//                Utils.log("run: onComplete");
//            }
//        });
//        mCompositeDisposable.add(disposable);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Utils.log("onNext thread name: " + Utils.getThreadName());
//                mPublishProcessor.onNext(3);
//                mPublishProcessor.onNext(4);
//            }
//        }).start();
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
     * 子线程发送数据,主线程接收
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
     * 线程发送数据,主线程接收
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
                            Utils.log("Processing data: " + data);
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

    /**
     * ReplayProcessor
     *
     * 该Subject会接收数据，当被订阅时，将所有接收到的数据全部发送给订阅者。
     * @param v
     */
    public void onTest8(View v) {
        ReplayProcessor<String> subject = ReplayProcessor.create();
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
     * BehaviorProcessor
     * 当Observer订阅了一个BehaviorProcessor，它一开始就会释放Observable最近释放的一个数据对象，当还没有任
     * 何数据释放时，它则是一个默认值。接下来就会释放Observable释放的所有数据。如果Observable因异常终止，
     * BehaviorProcessor将不会向后续的Observer释放数据，但是会向Observer传递一个异常通知。
     * @param v
     */
    public void onTest9(View v) {
        // observer will receive all 4 events (including "default").
        BehaviorProcessor<String> subject = BehaviorProcessor.createDefault("default");
        Disposable d1 = subject.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer1 onNext: " + integer);
            }
        });
        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");

        // observer will receive the "one", "two" and "three" events, but not "zero"
        BehaviorProcessor<String> subject2 = BehaviorProcessor.create();
        subject2.onNext("zero");
        subject2.onNext("one");
        Disposable d2 = subject2.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer2 onNext: " + integer);
            }
        });
        subject2.onNext("two");
        subject2.onNext("three");

        // observer will receive only onComplete
        BehaviorProcessor<String> subject3 = BehaviorProcessor.create();
        subject3.onNext("zero");
        subject3.onNext("one");
        subject3.onComplete();
        Disposable d3 = subject3.subscribe(new Consumer<String>() {
            @Override
            public void accept(String integer) throws Exception {
                Utils.log("observer3 onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable != null) {
                    Utils.log("observer3 onError: " + throwable.getMessage());
                }
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("observer3 onComplete: ");
            }
        });

        // observer will receive only onError
        BehaviorProcessor<String> subject4 = BehaviorProcessor.create();
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
     * PublishProcessor
     * PublishProcessor仅会向Observer释放在订阅之后Observable释放的数据。
     * @param v
     */
    public void onTest10(View v) {
        PublishProcessor<String> subject = PublishProcessor.create();

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
     * AsyncProcessor
     * AsyncProcessor仅释放Observable释放的最后一个数据，并且仅在Observable完成之后。然而如果当Observable
     * 因为异常而终止，AsyncProcessor将不会释放任何数据，但是会向Observer传递一个异常通知。
     * @param v
     */
    public void onTest11(View v) {
        AsyncProcessor<String> subject = AsyncProcessor.create();
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
     * SerializedProcessor
     * RxJava之并发处理（SerializedSubject）
     * https://www.cnblogs.com/cmlblog/p/7663116.html
     * SerializedProcessor非public，不能直接访问
     * 解决办法
     * 1. 不要直接实例化 SerializedProcessor。
     * 2. 使用 RxJava 提供的 PublishProcessor、BehaviorProcessor 或 ReplayProcessor 等Subject的具体实现，并在需要的时候通过 toSerialized() 方法来获取线程安全的版本。
     * @param v
     */
    public void onTest12(View v) {
        // 同一时间可以有多个线程同时调用onNext
//        final PublishProcessor<Integer> subject = PublishProcessor.create();
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
        PublishProcessor<Integer> publishSubject = PublishProcessor.create();
        final FlowableProcessor<Integer> subject = publishSubject.toSerialized();
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
     * UnicastProcessor
     * 仅支持订阅一次的Subject,如果多个订阅者试图订阅这个Subject，若该subject未terminate，将会收到IllegalStateException，
     * 若已经terminate，那么只会执行onError或者onComplete方法。
     * @param v
     */
    public void onTest13(View v) {
        UnicastProcessor<Integer> unicastSubject = UnicastProcessor.create();
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

    /**
     * PublishProcessor 线程切换
     * @param v
     */
    public void onTest14(View v) {
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
}



    






