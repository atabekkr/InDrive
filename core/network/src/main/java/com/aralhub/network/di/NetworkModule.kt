package com.aralhub.network.di

import android.content.Context
import com.aralhub.network.api.AddressNetworkApi
import com.aralhub.network.api.CancelCauseNetworkApi
import com.aralhub.network.api.DriverNetworkApi
import com.aralhub.network.api.PaymentMethodsNetworkApi
import com.aralhub.network.api.ReviewsNetworkApi
import com.aralhub.network.api.RideOptionNetworkApi
import com.aralhub.network.api.UserNetworkApi
import com.aralhub.network.api.WebSocketClientNetworkApi
import com.aralhub.network.utils.AuthInterceptor
import com.aralhub.network.utils.DriverAuthInterceptor
import com.aralhub.network.utils.NetworkErrorInterceptor
import com.aralhub.network.utils.TokenAuthenticator
import com.chuckerteam.chucker.api.ChuckerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @[Provides Singleton]
    fun provideChucker(
        @ApplicationContext context: Context,
    ): ChuckerInterceptor = ChuckerInterceptor.Builder(context).build()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @[Provides Singleton]
    @Named("Client")
    fun provideMainOkHttpClient(
        chucker: ChuckerInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        networkErrorInterceptor: NetworkErrorInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(chucker)
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(networkErrorInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    @[Provides Singleton]
    @Named("Driver")
    fun provideDriverOkHttpClient(
        chucker: ChuckerInterceptor,
        httpLoggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: DriverAuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        networkErrorInterceptor: NetworkErrorInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(chucker)
        .addInterceptor(httpLoggingInterceptor)
        .addInterceptor(networkErrorInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .build()

    @[Provides Singleton]
    @Named("Client")
    fun provideMainRetrofit(
        @Named("Client") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://araltaxi.aralhub.uz/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    @[Provides Singleton]
    @Named("Driver")
    fun provideDriverRetrofit(
        @Named("Driver") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://araltaxi.aralhub.uz/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    @[Provides Singleton]
    fun provideUserNetworkApi(@Named("Client") retrofit: Retrofit): UserNetworkApi =
        retrofit.create(UserNetworkApi::class.java)

    @[Provides Singleton]
    fun provideDriverNetworkApi(@Named("Driver")retrofit: Retrofit): DriverNetworkApi =
        retrofit.create(DriverNetworkApi::class.java)

    @[Provides Singleton]
    fun provideWebsocketClientNetworkApi(@Named("Client")retrofit: Retrofit): WebSocketClientNetworkApi =
        retrofit.create(WebSocketClientNetworkApi::class.java)

    @[Provides Singleton]
    fun providePaymentMethodNetworkApi(@Named("Client")retrofit: Retrofit): PaymentMethodsNetworkApi =
        retrofit.create(PaymentMethodsNetworkApi::class.java)

    @[Provides Singleton]
    fun provideRideOptionNetworkApi(@Named("Client")retrofit: Retrofit): RideOptionNetworkApi =
        retrofit.create(RideOptionNetworkApi::class.java)

    @[Provides Singleton]
    fun provideCancelCauseNetworkApi(@Named("Client")retrofit: Retrofit): CancelCauseNetworkApi =
        retrofit.create(CancelCauseNetworkApi::class.java)

    @[Provides Singleton]
    fun provideAddressNetworkApi(@Named("Client")retrofit: Retrofit): AddressNetworkApi =
        retrofit.create(AddressNetworkApi::class.java)

    @[Provides Singleton]
    fun provideReviewNetworkApi(@Named("Client")retrofit: Retrofit): ReviewsNetworkApi =
        retrofit.create(ReviewsNetworkApi::class.java)

}