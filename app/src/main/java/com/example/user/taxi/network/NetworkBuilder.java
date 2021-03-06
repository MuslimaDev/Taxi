package com.example.user.taxi.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class NetworkBuilder {

    private static RetrofitService retrofitService = null;

    public static RetrofitService service() {
        if (retrofitService == null) {
            retrofitService = new Retrofit.Builder()
                    .baseUrl("http://openfreecabs.org")
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RetrofitService.class);
        }
        return retrofitService;
    }

    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder onGoing = chain.request()
                                .newBuilder()
                                .addHeader("Accept", "application/json;versions=1");
                        return chain.proceed(onGoing.build());
                    }
                }).build();
    }
}