package com.example.rxjavademo.thread;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.BuildConfig;
import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;
import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RxJava线程
 */
public class ThreadActivity extends AppCompatActivity {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String TAG = ThreadActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread);
        Utils.log("Thread: " + Thread.currentThread().getName());
    }

    /**
     * 默认线程
     * @param v
     */
    public void onTest1(View v) {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("Observable thread is : " + Thread.currentThread().getName());
                Utils.log("emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("Observer thread is :" + Thread.currentThread().getName());
                Utils.log("onNext: " + integer);
            }
        };
        observable.subscribe(consumer);
    }

    /**
     * 线程切换
     * @param v
     */
    public void onTest2(View v) {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("Observable thread is : " + Thread.currentThread().getName());
                Utils.log("emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("Observer thread is :" + Thread.currentThread().getName());
                Utils.log("onNext: " + integer);
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    /**
     * 多次切换线程
     * 在RxJava中, 已经内置了很多线程选项供我们选择, 例如：
     * Schedulers.io() 代表io操作的线程, 通常用于网络,读写文件等io密集型的操作
     * Schedulers.computation() 代表CPU计算密集型的操作, 例如需要大量计算的操作
     * Schedulers.newThread() 代表一个常规的新线程
     * AndroidSchedulers.mainThread() 代表Android的主线程
     * RxNewThreadScheduler 是常规线程池中的一个
     * RxCachedThreadScheduler 是IO线程池中的一个
     * @param v
     */
    public void onTest3(View v) {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("Observable thread is : " + Thread.currentThread().getName());
                Utils.log("emit 1");
                emitter.onNext(1);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("Observer thread is :" + Thread.currentThread().getName());
                Utils.log("onNext: " + integer);
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "After observeOn(io), current thread is : " + Thread.currentThread().getName());
                    }
                })
                .subscribe(consumer);
    }

    /**
     * RxJava和Retrofit结合完成网络请求
     * @param v
     */
    public void onTest4(View v) {
        Api api = create().create(Api.class);
        api.login(new HashMap<>())
                .subscribeOn(Schedulers.io())               //在IO线程进行网络请求
                .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求结果
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseBody response) {
                        try {
                            System.out.println("=========> response: " + response.toString());
                            String strBack = doJson(response);
                            System.out.println("请求成功, 返回的数据如下:\n" + doJson(response));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(ThreadActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(ThreadActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * ResponseBody 处理成 Json
     */
    private String doJson(ResponseBody responseBody) {
        long contentLength = responseBody.contentLength();
        BufferedSource source = responseBody.source();
        try {
            source.request(Long.MAX_VALUE); // Buffer the entire body.
        } catch (IOException e) {
            e.printStackTrace();
        }
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            try {
                charset = contentType.charset(UTF8);
            } catch (UnsupportedCharsetException e) {
                e.printStackTrace();
            }
        }
        String result = "";
        if (contentLength != 0) {
            result = buffer.clone().readString(charset);
        }
        return result;
    }

    private Retrofit create() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.readTimeout(10, TimeUnit.SECONDS);
        builder.connectTimeout(9, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        return new Retrofit.Builder()
                .baseUrl("https://www.wanandroid.com/")
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    /**
     * RxJava doOnNext doOnSubscribe
     * doOnNext 允许我们在每次输出一个元素之前做一些额外的事情
     * doOnNext 如果写在.subscribeOn(Schedulers.io())之后，则Observable subscribe()方法里面每次调用emitter.onNext()方法之前都会执行doOnNext里面的代码
     * doOnNext 如果写在.observeOn(AndroidSchedulers.mainThread())之后，则consumer的accept()方法，每次收到数据之前都会调用doOnNext里面的代码
     * doOnSubscribe 是事件被订阅之前(也就是事件源发起之前)会调用的方法，这个方法一般用于修改、添加或者删除事件源的数据流。运行在主线程
     */
    public void onTest5(View v) {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>(){
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("Observable thread is : " + Thread.currentThread().getName());
                Utils.log("emit 1");
                emitter.onNext(1);
                Utils.log("emit 11");
                Utils.log("emit 2");
                emitter.onNext(2);
                Utils.log("emit 21");
                Utils.log("emit 3");
                emitter.onNext(3);
                Utils.log("emit 31");
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("Observer thread is :" + Thread.currentThread().getName());
                Utils.log("onNext: " + integer);
            }
        };

        observable.subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        Utils.log("doOnSubscribe thread is :" + Thread.currentThread().getName());
                    }
                })
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Utils.log("Observable thread1 is :" + Thread.currentThread().getName());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Utils.log("Observer thread1 is :" + Thread.currentThread().getName());
                    }
                })
                .subscribe(consumer);
    }

    /**
     * RxJava线程完成耗时操作
     * doOnNext 允许我们在每次输出一个元素之前做一些额外的事情
     * doOnNext 如果写在.subscribeOn(Schedulers.io())之后，则Observable subscribe()方法里面每次调用emitter.onNext()方法之前都会执行doOnNext里面的代码
     * doOnNext 如果写在.observeOn(AndroidSchedulers.mainThread())之后，则consumer的accept()方法，每次收到数据之前都会调用doOnNext里面的代码
     * doOnSubscribe 是事件被订阅之前(也就是事件源发起之前)会调用的方法，这个方法一般用于修改、添加或者删除事件源的数据流。运行在主线程
     */
    public void onTest6(View v) {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>(){
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Utils.log("Observable thread is : " + Thread.currentThread().getName());
                int count = 0;
                for(int i = 0; i < 10000; i ++) {
                    count += 2;
                }
                emitter.onNext(count);
            }
        });

        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Utils.log("Observer thread is :" + Thread.currentThread().getName());
                Utils.log("onNext: " + integer);
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

}

    






