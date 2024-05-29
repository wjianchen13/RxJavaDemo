package com.example.rxjavademo.base;

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
 * RxJava 入门
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    /**
     * 入门测试
     * @param v
     */
    public void onTest1(View v) {
        //创建一个上游 Observable：
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("observable subscribe");
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });
        //创建一个下游 Observer
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Utils.log("observer subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Utils.log("" + value);
            }

            @Override
            public void onError(Throwable e) {
                Utils.log("error");
            }

            @Override
            public void onComplete() {
                Utils.log("complete");
            }
        };
        //建立连接
        observable.subscribe(observer);
    }

    /**
     * 入门链式形式
     * @param v
     */
    public void onTest2(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Utils.log("subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Utils.log("" + value);
            }

            @Override
            public void onError(Throwable e) {
                Utils.log("error");
            }

            @Override
            public void onComplete() {
                Utils.log("complete");
            }
        });
    }

    /**
     * onComplete和onError互斥测试
     * 上游可以发送无限个 onNext , 下游也可以接收无限个 onNext 。
     * 当上游发送了一个 onComplete 后, 上游 onComplete 之后的事件将会继续发送, 而下游收到 onComplete 事件之后将不再继续接收事件.
     * 当上游发送了一个 onError 后, 上游 onError 之后的事件将继续发送, 而下游收到 onError 事件之后将不再继续接收事件.
     * 上游可以不发送 onComplete 或 onError 。
     * 最为关键的是 onComplete 和 onError 必须唯一并且互斥, 即不能发多个 onComplete , 也不能发多个 onError ,
     * 也不能先发一个 onComplete , 然后再发一个 onError , 反之亦然
     * 注: 关于 onComplete 和 onError 唯一并且互斥这一点, 是需要自行在代码中进行控制, 如果你的代码逻辑中违背了这个规则,
     * 并不一定会导致程序崩溃. 比如发送多个 onComplete 是可以正常运行的, 依然是收到第一个 onComplete 就不再接收了,
     * 但若是发送多个 onError, 则收到第二个 onError 事件会导致程序会崩溃.
     * @param v
     */
    public void onTest3(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("observable subscribe");
//                emitter.onNext(1);
//                emitter.onNext(2);
//                emitter.onNext(3);
//                emitter.onComplete();
//                emitter.onNext(4);
//                emitter.onNext(5);

//                emitter.onNext(1);
//                emitter.onNext(2);
//                emitter.onNext(3);
//                emitter.onError(new Exception("hello"));
//                emitter.onNext(4);
//                emitter.onNext(5);

//                emitter.onNext(1);
//                emitter.onNext(2);
//                emitter.onNext(3);
//                emitter.onComplete();
//                emitter.onNext(4);
//                emitter.onComplete();
//                emitter.onNext(5);

                // 会崩溃
//                emitter.onNext(1);
//                emitter.onNext(2);
//                emitter.onNext(3);
//                emitter.onError(new Exception("hello"));
//                emitter.onNext(4);
//                emitter.onError(new Exception("hello"));
//                emitter.onNext(5);
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Utils.log("observer subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Utils.log("" + value);
            }

            @Override
            public void onError(Throwable e) {
                if(e != null)
                    Utils.log("error: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Utils.log("complete");
            }
        });
    }

    /**
     * 中断事件发送
     * @param v
     */
    public void onTest4(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit complete");
                emitter.onComplete();
                Utils.log("emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Observer<Integer>() {
            private Disposable mDisposable;
            private int i;

            @Override
            public void onSubscribe(Disposable d) {
                Utils.log("subscribe");
                mDisposable = d;
            }

            @Override
            public void onNext(Integer value) {
                Utils.log("onNext: " + value);
                i++;
                if (i == 2) {
                    Utils.log("dispose");
                    mDisposable.dispose();
                    Utils.log("isDisposed : " + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                Utils.log("error");
            }

            @Override
            public void onComplete() {
                Utils.log("complete");
            }
        });
    }

    /**
     * 只关心onNext事件
     * @param v
     */
    public void onTest5(View v) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit complete");
                emitter.onComplete();
                Utils.log("emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("onNext: " + integer);
            }
        });

    }

    /**
     * 关心onNext, onError事件
     * @param v
     */
    public void onTest6(View v) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit error");
                emitter.onError(new Exception("test6"));
                Utils.log("emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if(throwable != null) {
                    Utils.log("onError: " + throwable.getMessage());
                }
            }
        });
    }

    /**
     * 关心onNext, onError, onComplete事件
     * @param v
     */
    public void onTest7(View v) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
//                Utils.log("emit error");
//                emitter.onError(new Exception("test7"));

                Utils.log("emit complete");
                emitter.onComplete();
                Utils.log("emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable != null) {
                    Utils.log("onError: " + throwable.getMessage());
                }
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("onComplete: ");
            }
        });
    }

    /**
     * 关心onNext, onError, onComplete, onSubscribe事件
     * @param v
     */
    public void onTest8(View v) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit error");
                emitter.onError(new Exception("test7"));

//                Utils.log("emit complete");
//                emitter.onComplete();
                Utils.log("emit 4");
                emitter.onNext(4);
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("onNext: " + integer);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (throwable != null) {
                    Utils.log("onError: " + throwable.getMessage());
                }
            }
        }, new Action() {
            @Override
            public void run() throws Exception {
                Utils.log("onComplete: ");
            }
        }, new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                Utils.log("onSubscribe: ");
            }
        });
    }
    
}

    






