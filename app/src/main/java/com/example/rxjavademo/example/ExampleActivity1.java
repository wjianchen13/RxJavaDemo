package com.example.rxjavademo.example;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * 后台执行耗时操作，实时通知 UI 更新
 */
public class ExampleActivity1 extends AppCompatActivity {

    private static final String TAG = ExampleActivity1.class.getSimpleName();

    private Button mTvDownload;
    private TextView mTvResult;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example1);
        mTvDownload = findViewById(R.id.btn_download);
        mTvResult = findViewById(R.id.tv_download_result);
    }
    
    public void onTest1(View v) {
        startDownload();
    }

    private void startDownload() {
        final Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; i < 100; i++) {
                    if (i % 20 == 0) {
                        try {
                            Thread.sleep(500); //模拟下载的操作。
                        } catch (InterruptedException exception) {
                            if (!e.isDisposed()) {
                                e.onError(exception);
                            }
                        }
                        e.onNext(i);
                    }
                }
                e.onComplete();
            }

        });
        DisposableObserver<Integer> disposableObserver = new DisposableObserver<Integer>() {

            @Override
            public void onNext(Integer value) {
                Utils.log("onNext=" + value);
                mTvResult.setText("当前下载进度：" + value);
            }

            @Override
            public void onError(Throwable e) {
                Utils.log("onError=" + e);
                mTvResult.setText("下载失败");
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete");
                mTvResult.setText("下载成功");
            }
        };
        observable.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(disposableObserver);
        mCompositeDisposable.add(disposableObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

}

    






