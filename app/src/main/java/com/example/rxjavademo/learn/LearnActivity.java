package com.example.rxjavademo.learn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rxjavademo.BuildConfig;
import com.example.rxjavademo.R;
import com.example.rxjavademo.Utils;
import com.example.rxjavademo.operator.OperatorActivity;
import com.example.rxjavademo.operator.UserInfo;
import com.example.rxjavademo.thread.Api;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RxJava 学习指南
 */
public class LearnActivity extends AppCompatActivity {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
    }

    /**
     * Observable使用
     * @param v
     */
    public void onTest1(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() { // 第一步：初始化Observable
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                Utils.log("Observable emit 1" + "\n");
                e.onNext(1);
                Utils.log("Observable emit 2" + "\n");
                e.onNext(2);
                Utils.log("Observable emit 3" + "\n");
                e.onNext(3);
                e.onComplete();
                Utils.log("Observable emit 4" + "\n" );
                e.onNext(4);
            }
        }).subscribe(new Observer<Integer>() { // 第三步：订阅

            // 第二步：初始化Observer
            private int i;
            private Disposable mDisposable;

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Utils.log("onNext value: " + integer );
                i++;
                if (i == 2) {
                    // 在RxJava 2.x 中，新增的Disposable可以做到切断的操作，让Observer观察者不再接收上游事件
                    mDisposable.dispose();
                    Utils.log("onNext isDisposed: " + mDisposable.isDisposed() );
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Utils.log("onError : value : " + e.getMessage() + "\n" );
            }

            @Override
            public void onComplete() {
                Utils.log("onComplete" + "\n" );
            }
        });

    }

    /**
     * 线程调度
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest2(View v) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                        Utils.log("Observable thread is : " + Thread.currentThread().getName());
                        e.onNext(1);
                        e.onComplete();
                    }
                }).subscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Utils.log("After observeOn(mainThread)，Current thread is " + Thread.currentThread().getName());
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {
                        Utils.log("After observeOn(io)，Current thread is " + Thread.currentThread().getName());
                    }
                });
    }

    /**
     * map 操作符
     * 1）通过 Observable.create() 方法，调用 OkHttp 网络请求；
     * 2）通过 map 操作符集合 gson，将 Response 转换为 bean 类；
     * 3）通过 doOnNext() 方法，解析 bean 中的数据，并进行数据库存储等操作；
     * 4）调度线程，在子线程中进行耗时操作任务，在主线程中更新 UI ；
     * 5）通过 subscribe()，根据请求成功或者失败来更新 UI 。
     * doNoNext()方法可以在链式调用中间插入，调用按照插入顺序，调用线程也是按照线程当前所在的位置，例如切换到
     * io线程，调用doOnNext()方法，就在io线程，切换到main线程，调用doOnNext()方法，就在main线程
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest3(View v) {
        Observable.create(new ObservableOnSubscribe<Response>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<Response> e) throws Exception {
                        Utils.log("ObservableOnSubscribe subscribe thread is : " + Thread.currentThread().getName());
                        Request.Builder builder = new Request.Builder()
                                .url("https://www.wanandroid.com/banner/json")
                                .get();
                        Request request = builder.build();
                        Call call = new OkHttpClient().newCall(request);
                        Response response = call.execute();
                        e.onNext(response);
                    }
                })
                .doOnNext(new Consumer<Response>() {
                    @Override
                    public void accept(@NonNull Response s) throws Exception {
                        Utils.log("doOnNext11 accept thread is : " + Thread.currentThread().getName());
                    }
                }).map(new Function<Response, MobileAddress>() {
                    @Override
                    public MobileAddress apply(@NonNull Response response) throws Exception {
                        Utils.log("map apply thread is : " + Thread.currentThread().getName());
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            if (body != null) {
                                Utils.log("请求成功, 返回的数据如下:\n" + doJson(response.body()));
                                MobileAddress address = new MobileAddress("test map");
                                return address;
                            }
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress s) throws Exception {
                        Utils.log("doOnNext accept thread is : " + Thread.currentThread().getName());
                        Utils.log("doOnNext: 保存成功：" + s.getAddress() + "\n");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress s) throws Exception {
                        Utils.log("doOnNext1 accept thread is : " + Thread.currentThread().getName());
                        Utils.log("doOnNext1: 保存成功：" + s.getAddress() + "\n");
                    }
                })
                .subscribe(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(@NonNull MobileAddress data) throws Exception {
                        Utils.log("subscribe accept thread is : " + Thread.currentThread().getName());
                       Utils.log("成功:" + data.toString() + "\n");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Utils.log("失败：" + throwable.getMessage() + "\n");
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

    private SpannableStringBuilder mRxOperatorsText;
    private boolean isFromNet;
    private boolean isCache;

    /**
     * concat 操作符
     * concat 操作符必须调用 onComplete 后才能订阅下一个 Observable 的特性
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest4(View v) {
        mRxOperatorsText = new SpannableStringBuilder();
        Observable<MobileAddress> cache = Observable.create(new ObservableOnSubscribe<MobileAddress>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<MobileAddress> e) throws Exception {
                Utils.log("cache subscribe thread is: " + Thread.currentThread().getName() );
                MobileAddress data = null;
                if(isCache) {
                    data = new MobileAddress("cache");
                }

                // 在操作符 concat 中，只有调用 onComplete 之后才会执行下一个 Observable
                if (data != null){ // 如果缓存数据不为空，则直接读取缓存数据，而不读取网络数据
                    isFromNet = false;
                    Utils.log("cache subscribe: 读取缓存数据成功:" );
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Utils.log("cache subscribe: 读取缓存数据成功" );
//                        }
//                    });

                    e.onNext(data);
                }else {
                    isFromNet = true;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mRxOperatorsText.append("cache subscribe: 读取网络数据:");
//                        }
//                    });
                    Utils.log("cache subscribe: 读取网络数据:" );
                    e.onComplete();
                }
            }
        });

        Observable<MobileAddress> network = Observable.create(new ObservableOnSubscribe<MobileAddress>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<MobileAddress> e) throws Exception {
                Utils.log("network subscribe thread is: " + Thread.currentThread().getName());
                Request.Builder builder = new Request.Builder()
                        .url("https://www.wanandroid.com/banner/json")
                        .get();
                Request request = builder.build();
                Call call = new OkHttpClient().newCall(request);
                Response response = call.execute();
                MobileAddress address = new MobileAddress("network");
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        Utils.log("请求成功, 返回的数据如下:\n" + doJson(response.body()));
                    }
                }
                e.onNext(address);
            }
        });

        // 两个 Observable 的泛型应当保持一致
        Observable.concat(cache, network)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MobileAddress>() {

                    @Override
                    public void accept(@NonNull MobileAddress tngouBeen) throws Exception {
                        Utils.log("subscribe 成功:"+Thread.currentThread().getName() );
                        if (isFromNet){
//                            CacheManager.getInstance().setMobileAddressData(tngouBeen);
                            isCache = true;
                        }
                        Utils.log("accept : 网络获取数据设置缓存: "+ tngouBeen.getAddress() );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Utils.log("subscribe 失败:"+Thread.currentThread().getName() );
                        Utils.log("accept: 读取数据失败："+throwable.getMessage() );
                    }
                });

    }

    /**
     * flatmap
     * 实现多个网络请求依次依赖
     * 例如用户注册成功后需要自动登录，我们只需要先通过注册接口注册用户信息，注册成功后马上调用登录接口进行自动登录即可。
     * @param v
     */
    @SuppressLint("CheckResult")
    public void onTest5(View v) {
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
                        Toast.makeText(LearnActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(LearnActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    }
                });
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
     * zip 操作符
     * 实现多个接口数据共同更新 UI，一个页面显示的数据来源于多个接口
     * @param v
     */
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

    private Disposable mDisposable;

    /**
     * interval 操作符
     * @param v
     */
    public void onTest7(View v) {
        mDisposable = Flowable.interval(1, TimeUnit.SECONDS)
            .doOnNext(new Consumer<Long>() {
                @Override
                public void accept(@NonNull Long aLong) throws Exception {
                    Utils.log("accept: doOnNext : "+aLong );
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(@NonNull Long aLong) throws Exception {
                    Utils.log("accept: 设置文本 ："+aLong );
                }
            });
    }

    /**
     * 销毁时停止心跳
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null){
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    /**
     * 停止 interval
     * @param v
     */
    public void onTest8(View v) {
        if (mDisposable != null){
            mDisposable.dispose();
            mDisposable = null;
        }
    }
    
}

    






