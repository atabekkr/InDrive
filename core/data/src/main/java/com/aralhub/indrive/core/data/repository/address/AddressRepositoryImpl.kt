package com.aralhub.indrive.core.data.repository.address

import com.aralhub.indrive.core.data.model.address.Address
import com.aralhub.indrive.core.data.model.address.CreateAddressRequest
import com.aralhub.indrive.core.data.model.address.toDomain
import com.aralhub.indrive.core.data.model.address.toNetwork
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.indrive.core.data.result.asResult
import com.aralhub.network.AddressNetworkDataSource
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import javax.inject.Inject

class AddressRepositoryImpl @Inject constructor(private val addressNetworkDataSource: AddressNetworkDataSource,
    private val localStorage: ClientSharedPreference
) :
    AddressRepository {
    override suspend fun createAddress(createAddressRequest: CreateAddressRequest): Result<Address> =
        addressNetworkDataSource.address(createAddressRequest.toNetwork(localStorage.userId)).asResult {
            Result.Success(it.toDomain())
        }

    override suspend fun getAllAddresses(): Result<List<Address>> =
        addressNetworkDataSource.getAddressByUserId(localStorage.userId).asResult {
            Result.Success(it.map { networkAddress -> networkAddress.toDomain() })
        }

    override suspend fun getAddressById(addressId: Int): Result<Address> =
        addressNetworkDataSource.getAddressById(addressId).asResult { Result.Success(it.toDomain()) }

    override suspend fun updateAddress(
        addressId: Int,
        createAddressRequest: CreateAddressRequest
    ): Result<Address> = addressNetworkDataSource.updateAddress(addressId, createAddressRequest.toNetwork(localStorage.userId)).asResult { Result.Success(it.toDomain()) }

    override suspend fun deleteAddress(addressId: Int): Result<Boolean> = addressNetworkDataSource.deleteAddress(addressId).asResult { Result.Success(true) }

}