/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aralhub.network.di

import com.aralhub.network.AddressNetworkDataSource
import com.aralhub.network.CancelCauseNetworkDataSource
import com.aralhub.network.ClientOffersNetworkDataSource
import com.aralhub.network.DriverNetworkDataSource
import com.aralhub.network.PaymentMethodNetworkDataSource
import com.aralhub.network.ReviewsNetworkDataSource
import com.aralhub.network.RideOptionNetworkDataSource
import com.aralhub.network.UserNetworkDataSource
import com.aralhub.network.ClientNetworkDataSource
import com.aralhub.network.impl.AddressNetworkDataSourceImpl
import com.aralhub.network.impl.CancelCauseNetworkDataSourceImpl
import com.aralhub.network.impl.ClientOffersNetworkDataSourceImpl
import com.aralhub.network.impl.DriverNetworkDataSourceImpl
import com.aralhub.network.impl.PaymentMethodNetworkDataSourceImpl
import com.aralhub.network.impl.ReviewsNetworkDataSourceImpl
import com.aralhub.network.impl.RideOptionNetworkDataSourceImpl
import com.aralhub.network.impl.UserNetworkDataSourceImpl
import com.aralhub.network.impl.ClientNetworkDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkDataSourceModule {

    @[Binds Singleton]
    abstract fun bindClientNetworkDataSource(networkDataSource: UserNetworkDataSourceImpl): UserNetworkDataSource

    @[Binds Singleton]
    abstract fun bindDriverNetworkDataSource(driverNetworkDataSourceImpl: DriverNetworkDataSourceImpl): DriverNetworkDataSource

    @[Binds Singleton]
    abstract fun bindClientWebsocketNetworkDataSource(clientWebsocketNetworkDataSourceImpl: ClientNetworkDataSourceImpl): ClientNetworkDataSource

    @[Binds Singleton]
    abstract fun bindPaymentMethodDataSource(paymentMethodNetworkDataSourceImpl: PaymentMethodNetworkDataSourceImpl): PaymentMethodNetworkDataSource

    @[Binds Singleton]
    abstract fun bindRideOptionNetworkDataSource(rideOptionNetworkDataSourceImpl: RideOptionNetworkDataSourceImpl): RideOptionNetworkDataSource

    @[Binds Singleton]
    abstract fun bindCancelCauseNetworkDataSource(cancelCauseNetworkDataSourceImpl: CancelCauseNetworkDataSourceImpl): CancelCauseNetworkDataSource

    @[Binds Singleton]
    abstract fun bindAddressNetworkDataSource(addressNetworkDataSourceImpl: AddressNetworkDataSourceImpl): AddressNetworkDataSource

    @[Binds Singleton]
    abstract fun bindOffersNetworkDataSource(offersNetworkDataSourceImpl: ClientOffersNetworkDataSourceImpl): ClientOffersNetworkDataSource

    @[Binds Singleton]
    abstract fun bindReviewNetworkDataSource(networkReviewsNetworkDataSourceImpl: ReviewsNetworkDataSourceImpl): ReviewsNetworkDataSource

}