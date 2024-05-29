package com.example.rxjavademo.operator;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.BuildConfig;
import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;
import com.example.rxjavademo.thread.Api;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RxJava 操作符
 */
public class OperatorActivity extends AppCompatActivity {
    
    private static final String TAG = OperatorActivity.class.getSimpleName();
    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);
        Utils.log(Thread.currentThread().getName());
    }

    /**
     * map
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest1(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log(s);
            }
        });

    }

    /**
     * flatMap
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest2(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i ++) {
                    list.add("I am value " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
//                return Observable.fromIterable(list);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log(s);
            }
        });
    }

    /**
     * concatMap
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest3(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i ++) {
                    list.add("I am value " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Utils.log(s);
            }
        });
    }

    /**
     * 嵌套网络请求
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest4(View v) {
        Api api = create().create(Api.class);
        api.register(new HashMap<>())            //发起注册请求
                .subscribeOn(Schedulers.io())               //在IO线程进行网络请求
                .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求注册结果
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody registerResponse) throws Exception {
                        //先根据注册的响应结果去做一些操作
                    }
                })
                .observeOn(Schedulers.io())                 //回到IO线程去发起登录请求
                .flatMap(new Function<ResponseBody, ObservableSource<ResponseBody>>() {
                    @Override
                    public ObservableSource<ResponseBody> apply(ResponseBody registerResponse) throws Exception {
                        System.out.println("=========> response: " + registerResponse.toString());
                        String strBack = doJson(registerResponse);
                        System.out.println("请求成功, 返回的数据如下:\n" + doJson(registerResponse));

                        return api.login(new HashMap<>());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求登录的结果
                .subscribe(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody loginResponse) throws Exception {
                        System.out.println("=========> response: " + loginResponse.toString());
                        String strBack = doJson(loginResponse);
                        System.out.println("请求成功, 返回的数据如下:\n" + doJson(loginResponse));
                        Toast.makeText(OperatorActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(OperatorActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
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
     * zip
     * @param v
     * 因为数量少的Observable发送完onComplete()之后，Observer就已经结束了，所以下一次在发送的时候就会报错，
     * 解决办法是少发事件的Observable不要调用onComplete()，后面发送完成的Observable才调用onComplete()方法.
     */
    @SuppressLint("CheckResult")
    public void onTest5(View v) {
//        setRxJavaErrorHandler();
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {

                Utils.log("After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                Utils.log("emit 1");
                emitter.onNext(1);
                Thread.sleep(1000);

                Utils.log("emit 2");
                emitter.onNext(2);
                Thread.sleep(1000);

                Utils.log("emit 3");
                emitter.onNext(3);
                Thread.sleep(1000);

                Utils.log("emit 4");
                emitter.onNext(4);
                Thread.sleep(1000);

                Utils.log("emit complete1");
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Utils.log("After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                Utils.log("emit A");
                emitter.onNext("A");
                Thread.sleep(1000);

                Utils.log("emit B");
                emitter.onNext("B");
                Thread.sleep(1000);

                Utils.log("emit C");
                emitter.onNext("C");
                Thread.sleep(1000);

                Utils.log("emit complete2");
//                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Utils.log("onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Utils.log("After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
                Utils.log("onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Utils.log("onError");
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete");
            }
        });

    }

    /**
     * 处理问题：
     * io.reactivex.exceptions.UndeliverableException: The exception could not be delivered to the consumer because it has already canceled/disposed the flow or the exception has nowhere to go to begin with. Further reading: https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling | java.lang.InterruptedException
     * https://blog.csdn.net/Mr_theSun/article/details/123223874
     */
    private void setRxJavaErrorHandler() {
        if (RxJavaPlugins.getErrorHandler() != null || RxJavaPlugins.isLockdown()) {
            Utils.log("setRxJavaErrorHandler getErrorHandler()!=null||isLockdown()");
            return;
        }
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) {
                if (e instanceof UndeliverableException) {
                    e = e.getCause();
                    Utils.log("setRxJavaErrorHandler UndeliverableException=" + e);
                    return;
                } else if ((e instanceof IOException)) {
                    // fine, irrelevant network problem or API that throws on cancellation
                    return;
                } else if (e instanceof InterruptedException) {
                    // fine, some blocking code was interrupted by a dispose call
                    return;
                } else if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                    // that's likely a bug in the application
                    Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                            Thread.currentThread().getUncaughtExceptionHandler();
                    if (uncaughtExceptionHandler != null) {
                        uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
                    }
                    return;
                } else if (e instanceof IllegalStateException) {
                    // that's a bug in RxJava or in a custom operator
                    Thread.UncaughtExceptionHandler uncaughtExceptionHandler =
                            Thread.currentThread().getUncaughtExceptionHandler();
                    if (uncaughtExceptionHandler != null) {
                        uncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e);
                    }
                    return;
                }
                Utils.log("setRxJavaErrorHandler unknown exception=" + e);
            }
        });
    }

    /**
     * zip 模拟需要从2个接口下载数据进行显示
     */
    @SuppressLint("CheckResult")
    public void onTest6(View v) {
        Api api = create().create(Api.class);
        Observable<ResponseBody> observable1 =
                api.register(new HashMap<>()).subscribeOn(Schedulers.io());

        Observable<ResponseBody> observable2 =
                api.login(new HashMap<>()).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2,
                        new BiFunction<ResponseBody, ResponseBody, UserInfo>() {
                            @Override
                            public UserInfo apply(ResponseBody baseInfo,
                                                  ResponseBody extraInfo) throws Exception {
                                System.out.println("=========> baseInfo: " + baseInfo.toString());
                                System.out.println("=========> extraInfo: " + extraInfo.toString());
                                return new UserInfo( 10, "test");
                            }
                        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<UserInfo>() {
                    @Override
                    public void accept(UserInfo userInfo) throws Exception {
                        //do something;
                        Utils.log("success sex: " + userInfo.getSex() + "   name: " + userInfo.getName());
                    }
                });

    }
    
}









    






