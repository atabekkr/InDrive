package com.aralhub.indrive.core.data.di

import com.aralhub.indrive.core.data.repository.address.AddressRepository
import com.aralhub.indrive.core.data.repository.address.AddressRepositoryImpl
import com.aralhub.indrive.core.data.repository.cancel.CancelRepository
import com.aralhub.indrive.core.data.repository.cancel.impl.CancelRepositoryImpl
import com.aralhub.indrive.core.data.repository.client.ClientAuthRepository
import com.aralhub.indrive.core.data.repository.client.ClientOffersRepository
import com.aralhub.indrive.core.data.repository.client.ClientRideRepository
import com.aralhub.indrive.core.data.repository.client.ClientRepository
import com.aralhub.indrive.core.data.repository.client.impl.ClientAuthRepositoryImpl
import com.aralhub.indrive.core.data.repository.client.impl.ClientOffersRepositoryImpl
import com.aralhub.indrive.core.data.repository.client.impl.ClientRideRepositoryImpl
import com.aralhub.indrive.core.data.repository.client.impl.ClientRepositoryImpl
import com.aralhub.indrive.core.data.repository.driver.DriverAuthRepository
import com.aralhub.indrive.core.data.repository.driver.DriverOfferRepository
import com.aralhub.indrive.core.data.repository.driver.DriverRepository
import com.aralhub.indrive.core.data.repository.driver.impl.DriverAuthRepositoryImpl
import com.aralhub.indrive.core.data.repository.driver.impl.DriverOfferRepositoryImpl
import com.aralhub.indrive.core.data.repository.driver.impl.DriverRepositoryImpl
import com.aralhub.indrive.core.data.repository.payment.PaymentRepository
import com.aralhub.indrive.core.data.repository.payment.impl.PaymentRepositoryImpl
import com.aralhub.indrive.core.data.repository.review.ReviewRepository
import com.aralhub.indrive.core.data.repository.review.impl.ReviewRepositoryImpl
import com.aralhub.indrive.core.data.repository.rideoption.RideOptionRepository
import com.aralhub.indrive.core.data.repository.rideoption.impl.RideOptionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindsClientAuthRepository(
        topicsRepository: ClientAuthRepositoryImpl,
    ): ClientAuthRepository

    @Binds
    internal abstract fun bindsDriverAuthRepository(
        topicsRepository: DriverAuthRepositoryImpl,
    ): DriverAuthRepository

    @Binds
    internal abstract fun bindsClientWebSocketRepository(
        topicsRepository: ClientRepositoryImpl,
    ): ClientRepository

    @Binds
    internal abstract fun bindsPaymentRepository(
        topicsRepository: PaymentRepositoryImpl,
    ): PaymentRepository

    @Binds
    internal abstract fun bindsRideOptionRepository(
        topicsRepository: RideOptionRepositoryImpl,
    ): RideOptionRepository

    @Binds
    internal abstract fun bindsCancelRepository(
        topicsRepository: CancelRepositoryImpl,
    ): CancelRepository

    @Binds
    internal abstract fun bindsAddressRepository(
        topicsRepository: AddressRepositoryImpl,
    ): AddressRepository

    @Binds
    internal abstract fun bindsDriverOfferRepository(
        repo: DriverOfferRepositoryImpl,
    ): DriverOfferRepository

    @Binds
    internal abstract fun bindsDriverRepository(
        repo: DriverRepositoryImpl,
    ): DriverRepository

    @Binds
    internal abstract fun bindsOffersRepository(
        repo: ClientOffersRepositoryImpl,
    ): ClientOffersRepository

    @Binds
    internal abstract fun bindsRideRepository(
        repository: ClientRideRepositoryImpl
    ): ClientRideRepository

    @Binds
    internal abstract fun bindsReviewRepository(
        repository: ReviewRepositoryImpl
    ): ReviewRepository
}
