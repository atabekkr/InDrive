package com.aralhub.indrive.core.data.repository.client.impl

import com.aralhub.indrive.core.data.model.client.ClientRide
import com.aralhub.indrive.core.data.model.client.ClientRideRequest
import com.aralhub.indrive.core.data.model.client.ClientRideResponseDistanceItemPoint
import com.aralhub.indrive.core.data.model.client.ClientRideResponseDistanceItemStartPointCoordinates
import com.aralhub.indrive.core.data.model.client.ClientRideResponseLocations
import com.aralhub.indrive.core.data.model.client.ClientRideResponseLocationsItemsCoordinates
import com.aralhub.indrive.core.data.model.client.ClientRideResponseOptions
import com.aralhub.indrive.core.data.model.client.ClientRideResponseOptionsItem
import com.aralhub.indrive.core.data.model.client.ClientRideResponsePassenger
import com.aralhub.indrive.core.data.model.client.ClientRideResponsePaymentMethod
import com.aralhub.indrive.core.data.model.client.ClientRideResponseRecommendedAmount
import com.aralhub.indrive.core.data.model.client.GeoPoint
import com.aralhub.indrive.core.data.model.client.RecommendedPrice
import com.aralhub.indrive.core.data.model.driver.DriverCard
import com.aralhub.indrive.core.data.model.payment.toDomain
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.ActiveRideDriver
import com.aralhub.indrive.core.data.model.ride.ActiveRideOptions
import com.aralhub.indrive.core.data.model.ride.Distance
import com.aralhub.indrive.core.data.model.ride.DistanceSegment
import com.aralhub.indrive.core.data.model.ride.LocationPoint
import com.aralhub.indrive.core.data.model.ride.LocationPointCoordinates
import com.aralhub.indrive.core.data.model.ride.RecommendedAmount
import com.aralhub.indrive.core.data.model.ride.SearchRide
import com.aralhub.indrive.core.data.model.ride.SearchRideDriver
import com.aralhub.indrive.core.data.model.ride.SearchRideLocations
import com.aralhub.indrive.core.data.model.ride.StandardPrice
import com.aralhub.indrive.core.data.model.ride.WaitAmount
import com.aralhub.indrive.core.data.model.ride.toDomain
import com.aralhub.indrive.core.data.repository.client.ClientWebSocketRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.WebSocketClientNetworkDataSource
import com.aralhub.araltaxi.core.common.sharedpreference.ClientSharedPreference
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.location.NetworkLocationPoint
import com.aralhub.network.models.location.NetworkLocationPointCoordinates
import com.aralhub.network.models.location.NetworkLocations
import com.aralhub.network.models.price.NetworkRecommendedPrice
import com.aralhub.network.requests.ride.NetworkClientRideRequest
import javax.inject.Inject

class ClientWebSocketRepositoryImpl @Inject constructor(private val localStorage: ClientSharedPreference, private val dataSource: WebSocketClientNetworkDataSource) :
    ClientWebSocketRepository {
    override suspend fun getRecommendedPrice(points: List<GeoPoint>): Result<RecommendedPrice> {
       return dataSource.getRecommendedPrice(points.map { NetworkLocationPoint(
           coordinates = NetworkLocationPointCoordinates(it.longitude, it.latitude),
           name = "point"
       ) }).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(RecommendedPrice(
                    minAmount = it.data.minAmount,
                    maxAmount = it.data.maxAmount,
                    recommendedAmount = it.data.recommendedAmount
                ))
            }
        }
    }

    override suspend fun createRide(clientRideRequest: ClientRideRequest): Result<ClientRide> {
        return dataSource.clientRide(NetworkClientRideRequest(
            passengerId = localStorage.userId,
            baseAmount = clientRideRequest.baseAmount,
            recommendedAmount = NetworkRecommendedPrice(
                minAmount = clientRideRequest.recommendedAmount.minAmount,
                maxAmount = clientRideRequest.recommendedAmount.maxAmount,
                recommendedAmount = clientRideRequest.recommendedAmount.recommendedAmount
            ),
            locations = NetworkLocations(
                points = clientRideRequest.locations.points.map { point ->
                    NetworkLocationPoint(
                        coordinates = NetworkLocationPointCoordinates(
                            longitude = point.coordinates.longitude,
                            latitude = point.coordinates.latitude
                        ),
                        name = point.name
                    )
                }
            ),
            comment = clientRideRequest.comment,
            autoTake = clientRideRequest.autoTake,
            paymentId = clientRideRequest.paymentId,
            optionIds = clientRideRequest.optionIds,
            cancelCauseId = clientRideRequest.cancelCauseId
        )).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(ClientRide(
                    uuid = it.data.uuid,
                    passenger = ClientRideResponsePassenger(
                        userId = it.data.passenger.userId,
                        userFullName = it.data.passenger.userFullName,
                        userRating = it.data.passenger.userRating,
                    ),
                    baseAmount = it.data.baseAmount,
                    updatedAmount = it.data.updatedAmount,
                    recommendedAmount = ClientRideResponseRecommendedAmount(
                        minAmount = it.data.recommendedAmount.minAmount,
                        maxAmount = it.data.recommendedAmount.maxAmount,
                        recommendedAmount = it.data.recommendedAmount.recommendedAmount
                    ),
                    locations = ClientRideResponseLocations(
                        points = it.data.locations.points.map { point ->
                            com.aralhub.indrive.core.data.model.client.ClientRideResponseLocationsItems(
                                coordinates = ClientRideResponseLocationsItemsCoordinates(
                                    longitude = point.coordinates.longitude,
                                    latitude = point.coordinates.latitude
                                ),
                                name = point.name
                            )
                        }
                    ),
                    comment = it.data.comment,
                    paymentMethod = ClientRideResponsePaymentMethod(
                        id = it.data.paymentMethod.id,
                        name = it.data.paymentMethod.name,
                        isActive = it.data.paymentMethod.isActive
                    ),
                    options = ClientRideResponseOptions(
                        options = it.data.options.options.map { option ->
                            ClientRideResponseOptionsItem(
                                id = option.id,
                                name = option.name
                            )
                        }
                    ),
                    autoTake = it.data.autoTake,
                    distance = com.aralhub.indrive.core.data.model.client.ClientRideResponseDistance(
                        segments = it.data.distance.segments.map { segment ->
                            com.aralhub.indrive.core.data.model.client.ClientRideResponseDistanceItem(
                                distance = segment.distance,
                                duration = segment.duration,
                                startPoint = ClientRideResponseDistanceItemPoint(
                                    coordinates = ClientRideResponseDistanceItemStartPointCoordinates(
                                        longitude = segment.startPoint.coordinates.longitude,
                                        latitude = segment.startPoint.coordinates.latitude
                                    ),
                                    name = segment.startPoint.name
                                ),
                                endPoint = ClientRideResponseDistanceItemPoint(
                                    coordinates = ClientRideResponseDistanceItemStartPointCoordinates(
                                        longitude = segment.startPoint.coordinates.longitude,
                                        latitude = segment.startPoint.coordinates.latitude
                                    ),
                                    name = segment.endPoint.name
                                )
                            )
                        },
                        totalDistance = it.data.distance.totalDistance,
                        totalDuration = it.data.distance.totalDuration
                    ),
                    cancelCauseId = it.data.cancelCauseId
                ))
            }
        }
    }

    override suspend fun getActiveRideByPassenger(): Result<ActiveRide> {
        return dataSource.getActiveRideByPassenger(localStorage.userId).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(ActiveRide(
                    id = it.data.id,
                    uuid = it.data.uuid,
                    status = it.data.status,
                    amount = it.data.amount,
                    waitAmount = it.data.waitAmount,
                    distance = it.data.distance,
                    locations = it.data.locations.toDomain(),
                    isActive = it.data.isActive,
                    createdAt = it.data.createdAt,
                    driver = ActiveRideDriver(
                        driverId = it.data.driver.driverId,
                        fullName = it.data.driver.fullName,
                        rating = it.data.driver.rating.toString(),
                        vehicleColor = it.data.driver.vehicleColor.kk,
                        vehicleType = it.data.driver.vehicleType.kk,
                        vehicleNumber = it.data.driver.vehicleType.kk,
                        phoneNumber = it.data.driver.phoneNumber ?: "",
                        photoUrl = it.data.driver.photoUrl
                    ),
                    paymentMethod = it.data.paymentMethod.toDomain(),
                    options = it.data.options.map { option ->
                        ActiveRideOptions(
                            id = option.id,
                            name = option.name
                        )
                    }
                ))
            }
        }
    }

    override suspend fun getSearchRideByPassengerId(): Result<SearchRide> {
        return dataSource.getSearchRideByPassengerId(localStorage.userId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(
                    SearchRide(
                        uuid = it.data.uuid,
                        passenger = SearchRideDriver(
                            userId = it.data.passenger.userId,
                            userFullName = it.data.passenger.userFullName,
                            userRating = it.data.passenger.userRating
                        ),
                        baseAmount = it.data.baseAmount,
                        updatedAmount = it.data.updatedAmount ?: 0,
                        recommendedAmount = RecommendedAmount(
                            minAmount = it.data.recommendedAmount.minAmount,
                            maxAmount = it.data.recommendedAmount.maxAmount,
                            recommendedAmount = it.data.recommendedAmount.recommendedAmount
                        ),
                        locations = SearchRideLocations(
                            points = it.data.locations.points.map { point ->
                                LocationPoint(
                                    coordinates = LocationPointCoordinates(
                                        longitude = point.coordinates.longitude,
                                        latitude = point.coordinates.latitude
                                    ),
                                    name = point.name
                                )
                            }
                        ),
                        comment = it.data.comment,
                        paymentMethod = com.aralhub.indrive.core.data.model.ride.PaymentMethod(
                            id = it.data.paymentMethod.id,
                            name = it.data.paymentMethod.name,
                            isActive = it.data.paymentMethod.isActive
                        ),
                        options = it.data.options.options.map { option ->
                            com.aralhub.indrive.core.data.model.ride.RideOption(
                                id = option.id,
                                name = option.name
                            )
                        },
                        autoTake = it.data.autoTake,
                        distance = Distance(
                            segments = it.data.distance.segments.map { segment ->
                                DistanceSegment(
                                    distance = segment.distance,
                                    duration = segment.duration,
                                    startPoint = LocationPoint(
                                        coordinates = LocationPointCoordinates(
                                            longitude = segment.startPoint.coordinates.longitude,
                                            latitude = segment.startPoint.coordinates.latitude
                                        ),
                                        name = segment.startPoint.name
                                    ),
                                    endPoint = LocationPoint(
                                        coordinates = LocationPointCoordinates(
                                            longitude = segment.endPoint.coordinates.longitude,
                                            latitude = segment.endPoint.coordinates.latitude
                                        ),
                                        name = segment.endPoint.name
                                    )
                                )
                            },
                            totalDistance = it.data.distance.totalDistance,
                            totalDuration = it.data.distance.totalDuration
                        ),
                        cancelCauseId = it.data.cancelCauseId ?: 0
                ))
            }
        }
    }

    override suspend fun cancelSearchRide(rideId: String): Result<Boolean> {
        return dataSource.clientCancelSearchRide(rideId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(true)
            }
        }
    }

    override suspend fun updateSearchRideAmount(rideId: String, amount: Number): Result<Boolean> {
        return dataSource.updateSearchRideAmount(rideId, amount).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(true)
            }
        }
    }

    override suspend fun updateAutoTake(rideId: String, autoTake: Boolean): Result<Boolean> {
        return dataSource.updateAutoTake(rideId, autoTake).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(true)
            }
        }
    }

    override suspend fun cancelRide(rideId: Int, cancelCauseId: Int): Result<Boolean> {
        return dataSource.cancelRide(rideId, cancelCauseId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(true)
            }
        }
    }

    override suspend fun cancelRideByPassenger(rideId: Int): Result<Boolean> {
        return dataSource.cancelRideByPassenger(rideId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(true)
            }
        }
    }

    override suspend fun getWaitAmount(rideId: Int): Result<WaitAmount> {
        return dataSource.getWaitTime(rideId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(WaitAmount(
                    waitAmount = it.data.waitAmount,
                    waitStartTime = it.data.waitStartTime,
                    paidWaitingTime = it.data.paidWaitingTime
                ))
            }
        }
    }

    override suspend fun getStandardPrice(): Result<StandardPrice> {
        return dataSource.getStandard().let {
            when(it){
                is NetworkResult.Success -> Result.Success(StandardPrice(
                    freeWaitMinutes = it.data.freeWaitMinutes,
                    waitPricePerMinute = it.data.waitPricePerMinute,
                    commissionPercent = it.data.commissionPercent,
                    cashbackPercent = it.data.cashbackPercent
                ))
                is NetworkResult.Error -> Result.Error(it.message)
            }
        }
    }

    override suspend fun getDriverCard(driverId: Int): Result<DriverCard> {
        return dataSource.getDriverCard(driverId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(DriverCard(
                    it.data.cardNumber,
                    it.data.nameOnCard
                ))
            }
        }
    }

}