package com.githubapi.search.searchgithubusers.di.data

import com.githubapi.search.searchgithubusers.data.api.GithubSearchUserApi
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class ApiModule {

    val baseUrl = "https://api.github.com"

    @Provides
    fun provideGson(): Gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .create()

    @Provides
    fun provideGsonFactory(gson: Gson): GsonConverterFactory = GsonConverterFactory.create(gson)

    @Provides
    fun provideRxFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor()
            .apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    fun provideInterceptor(): Interceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val request = requestBuilder.build()
        chain.proceed(request)
    }

    @Provides
    fun provideOkHttpClient(interceptor: Interceptor, httpLoggingInterceptor: HttpLoggingInterceptor)
    = OkHttpClient.Builder().apply {
        connectTimeout(30, TimeUnit.SECONDS)
        readTimeout(30, TimeUnit.SECONDS)
        interceptors().add(interceptor)
        interceptors().add(httpLoggingInterceptor)
    }

    @Provides
    fun provideRetorofit(gsonConverterFactory: GsonConverterFactory, rxJava2CallAdapterFactory: RxJava2CallAdapterFactory, okHttpClientBuilder: OkHttpClient.Builder)
    = Retrofit.Builder().apply {
        addConverterFactory(gsonConverterFactory)
        addCallAdapterFactory(rxJava2CallAdapterFactory)
        baseUrl(baseUrl)
        client(okHttpClientBuilder.build())
    }.build()

    @Provides
    fun provideGithubSearchUserApi(retrofit: Retrofit)
    = retrofit.create(GithubSearchUserApi::class.java)
}
