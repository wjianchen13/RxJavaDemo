package com.example.rxjavademo.flowable;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava Flowable
 */
public class FlowableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flowable);
    }

    /**
     * Flowable 基础用法
     * @param v
     */
    public void onTest1(View v) {
        Flowable<Integer> upstream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit complete");
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR); //增加了一个参数

        Subscriber<Integer> downstream = new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                Utils.log("onSubscribe");
                s.request(Long.MAX_VALUE);  //注意这句代码
            }

            @Override
            public void onNext(Integer integer) {
                Utils.log("onNext: " + integer);

            }

            @Override
            public void onError(Throwable t) {
                Utils.log("onError: " + t);
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete");
            }
        };
        upstream.subscribe(downstream);
    }

    /**
     * request 方法作用，同步
     * 去掉request()方法，这段代码会报错
     * io.reactivex.exceptions.MissingBackpressureException: create: could not emit value due to lack of requests
     * 因为下游没有调用request, 上游就认为下游没有处理事件的能力, 而这又是一个同步的订阅，所以直接抛出异常了
     * @param v
     */
    public void onTest2(View v) {
        Flowable<Integer> upstream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit complete");
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR); //增加了一个参数

        Subscriber<Integer> downstream = new Subscriber<Integer>() {

            @Override
            public void onSubscribe(Subscription s) {
                Utils.log("onSubscribe");
//                s.request(Long.MAX_VALUE);  //注意这句代码
            }

            @Override
            public void onNext(Integer integer) {
                Utils.log("onNext: " + integer);

            }

            @Override
            public void onError(Throwable t) {
                Utils.log("onError: " + t);
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete");
            }
        };
        upstream.subscribe(downstream);
    }

    /**
     * request 方法作用,异步
     * 没有报错，但是下游也不会收到事件，因为Flowable默认有一个大小是128的缓冲池，数据放到缓冲池了
     * @param v
     */
    public void onTest3(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        Utils.log("emit 1");
                        emitter.onNext(1);
                        Utils.log("emit 2");
                        emitter.onNext(2);
                        Utils.log("emit 3");
                        emitter.onNext(3);
                        Utils.log("emit complete");
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });
    }

    private Subscription mSubscription;

    /**
     * request 方法作用,异步，逐步取出数据
     * @param v
     */
    public void onTest4(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        Utils.log("emit 1");
                        emitter.onNext(1);
                        Utils.log("emit 2");
                        emitter.onNext(2);
                        Utils.log("emit 3");
                        emitter.onNext(3);
                        Utils.log("emit complete");
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;  //把Subscription保存起来
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * request 方法作用,异步，逐步取出数据
     * @param v
     */
    public void onTest5(View v) {
        if(mSubscription != null) {
            mSubscription.request(1); //在外部调用request请求上游
        } else {
            Utils.log("mSubscription == null");
        }
    }

    /**
     * Flowable缓冲池大小 128 测试，不超过128
     * @param v
     */
    public void onTest6(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 128; i++) {
                            Utils.log("emit " + i);
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable缓冲池大小 128 测试，超过128
     * 会报异常：onError: io.reactivex.exceptions.MissingBackpressureException: create: could not emit value due to lack of requests
     * @param v
     */
    public void onTest7(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 129; i++) {
                            Utils.log("emit " + i);
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.ERROR).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });
    }


    /**
     * Flowable BackpressureStrategy.BUFFER
     * 不丢弃数据的处理方式。把上游收到的全部缓存下来，等下游来请求再发给下游。相当于一个水库。但上游太快，水库（buffer）就会溢出
     * 效果和Observable一样
     * @param v
     */
    public void onTest8(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 1000; i++) {
                            Utils.log("emit " + i);
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }


    /**
     * Flowable BackpressureStrategy.BUFFER 无限发送数据
     * 之前使用Observable测试的时候内存增长非常迅速, 几秒钟就OOM，Flowable这种情况下内存也会一直增长，直到OOM
     * 但是Flowable内部做了一些优化处理，所以增长比较缓慢，但是性能有所丢失
     * @param v
     */
    public void onTest9(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; ; i++) {
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable BackpressureStrategy.DROP 无限发送事件
     * @param v
     */
    public void onTest10(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; ; i++) {
//                            try {
//                                Thread.sleep(10);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.DROP).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable BackpressureStrategy.DROP 发送10000事件
     * @param v
     */
    public void onTest11(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 10000; i++) {  //只发1w个事件
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.DROP).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                        s.request(128);  //一开始就处理掉128个事件
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable BackpressureStrategy.DROP 获取发送事件
     * @param v
     */
    public void onTest12(View v) {
        if(mSubscription != null) {
            mSubscription.request(128); //在外部调用request请求上游
        } else {
            Utils.log("mSubscription == null");
        }
    }

    /**
     * Flowable BackpressureStrategy.LATEST 无限发送事件
     * @param v
     */
    public void onTest13(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; ; i++) {
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable BackpressureStrategy.LATEST 发送10000事件
     * 第一次request(128)时，收到128个数据，第二次request(128)时，收到69个数据，gpt是这样解释的
     * 非常抱歉前面的回答仍然没有解决你的问题。现在我明白了你的意思。
     * 当使用 BackpressureStrategy.LATEST 策略时，下游通过 Subscription.request(128) 请求 128 个数据时，上游会发送尽可能多的数据来填充缓存，并保留最新的数据。然而，在你的情况下，你期望第二次请求 128 个数据时接收到 128 个数据，但只收到了 96 个数据。
     * 这可能是由于 Flowable 在处理请求时的内部机制导致的。Flowable 在接收到下游的 Subscription.request(n) 请求时，会根据当前缓存中的可用数据量和请求的数据量来决定发送多少数据。它可能会考虑一些优化因素，例如内部缓存的大小、数据处理的效率等，从而决定发送的数据量。
     * 在你的情况中，第一次请求 128 个数据时，上游可能发送了 128 个数据并填充了缓存。但是，当第二次请求 128 个数据时，上游可能根据内部机制决定只发送部分数据，以保持整体流程的平衡性。这就是为什么你只收到了 96 个数据而不是 128 个数据。
     * 这种行为是符合 Flowable 的设计和预期的，它通过 BackpressureStrategy.LATEST 策略来确保下游只接收到最新的数据，并在处理能力不足时保持整体平衡。如果你需要确保每次请求都能接收到完整的 128 个数据，你可以考虑使用其他的背压策略，如 BackpressureStrategy.BUFFER。
     * 请注意，在使用 BackpressureStrategy.BUFFER 策略时，如果上游发送的数据量超过了下游的处理能力，会导致缓存中积累大量的数据，可能会占用大量的内存。因此，在选择背压策略时，请根据你的具体需求和场景来权衡和选择合适的策略。
     * @param v
     */
    public void onTest14(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 1000; i++) {  //只发1w个事件
//                            try {
//                                Thread.sleep(50);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            Utils.log("subscribe i: " + i);
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.LATEST).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        Utils.log("onSubscribe");
                        mSubscription = s;
                        s.request(128);  //一开始就处理掉128个事件
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Utils.log("onNext: " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Utils.log("onError: " + t);
                    }

                    @Override
                    public void onComplete() {
                        Utils.log("onComplete");
                    }
                });

    }

    /**
     * Flowable BackpressureStrategy.LATEST 获取发送事件
     * @param v
     */
    public void onTest15(View v) {
        if(mSubscription != null) {
            mSubscription.request(128); //在外部调用request请求上游
        } else {
            Utils.log("mSubscription == null");
        }
    }

    /**
     *
     * @param v
     */
    public void onTest99(View v) {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                        for (int i = 0; i < 1000; i++) {
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            Utils.log("emitter=" + i );
                            emitter.onNext(i);
                        }
                    }
                }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        Utils.log("onNext=" + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }
}

    






