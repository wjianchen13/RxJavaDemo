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
import io.reactivex.functions.Consumer;

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
     * 中断事件发送
     * @param v
     */
    public void onTest3(View v) {
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
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("onNext: " + integer);
            }
        });

    }
    
}

    






