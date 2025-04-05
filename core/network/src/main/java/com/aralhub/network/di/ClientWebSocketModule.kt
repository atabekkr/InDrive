package com.aralhub.network.di

import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.network.ClientRideNetworkDataSource
import com.aralhub.network.WebSocketClientOffersNetworkDataSource
import com.aralhub.network.impl.ClientRideNetworkDataSourceImpl
import com.aralhub.network.impl.WebSocketClientOffersNetworkDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientWebSocketModule {

    @Singleton
    @Provides
    @Named("ClientHttpClient")
    fun provideHttpClientForClientPart(localStorage: ClientSharedPreference): HttpClient {
        return HttpClient(CIO) {
            install(Logging)
            install(WebSockets) {
                pingInterval = 20_000
            }
            defaultRequest {
                header(
                    "Authorization",
                    "Bearer ${localStorage.access}"
                )
            }
        }
    }

    @Singleton
    @Provides
    fun provideClientOffersNetworkDataSource(@Named("ClientHttpClient") httpClient: HttpClient): WebSocketClientOffersNetworkDataSource {
        return WebSocketClientOffersNetworkDataSourceImpl(httpClient)
    }

    @Singleton
    @Provides
    fun provideClientRideNetworkDataSource(@Named("ClientHttpClient") httpClient: HttpClient): ClientRideNetworkDataSource {
        return ClientRideNetworkDataSourceImpl(httpClient)
    }
}