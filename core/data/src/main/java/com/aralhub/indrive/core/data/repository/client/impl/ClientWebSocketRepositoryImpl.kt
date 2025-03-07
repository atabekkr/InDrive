package com.aralhub.indrive.core.data.repository.client.impl

import com.aralhub.indrive.core.data.model.client.ClientRide
import com.aralhub.indrive.core.data.model.client.ClientRideRequest
import com.aralhub.indrive.core.data.model.client.ClientRideResponseDistanceItemPoint
import com.aralhub.indrive.core.data.model.client.ClientRideResponseDistanceItemStartPointCoordinates
import com.aralhub.indrive.core.data.model.client.ClientRideResponseLocations
import com.aralhub.indrive.core.data.model.client.ClientRideResponseOptions
import com.aralhub.indrive.core.data.model.client.ClientRideResponseOptionsItem
import com.aralhub.indrive.core.data.model.client.ClientRideResponsePassenger
import com.aralhub.indrive.core.data.model.client.ClientRideResponsePaymentMethod
import com.aralhub.indrive.core.data.model.client.GeoPoint
import com.aralhub.indrive.core.data.model.client.RecommendedPrice
import com.aralhub.indrive.core.data.model.ride.ActiveRide
import com.aralhub.indrive.core.data.model.ride.SearchRide
import com.aralhub.indrive.core.data.repository.client.ClientWebSocketRepository
import com.aralhub.indrive.core.data.result.Result
import com.aralhub.network.WebSocketClientNetworkDataSource
import com.aralhub.network.models.NetworkResult
import com.aralhub.network.models.websocketclient.ClientRideRequestLocations
import com.aralhub.network.models.websocketclient.ClientRideRequestRecommendedAmount
import com.aralhub.network.models.websocketclient.NetworkGetRecommendedRidePricePoint
import com.aralhub.network.models.websocketclient.NetworkGetRecommendedRidePricePointCoordinates
import javax.inject.Inject

class ClientWebSocketRepositoryImpl @Inject constructor(private val dataSource: WebSocketClientNetworkDataSource) :
    ClientWebSocketRepository {
    override suspend fun getRecommendedPrice(points: List<GeoPoint>): Result<RecommendedPrice> {
       return dataSource.getRecommendedPrice(points.map { NetworkGetRecommendedRidePricePoint(
           coordinates = NetworkGetRecommendedRidePricePointCoordinates(it.longitude, it.latitude),
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
        return dataSource.clientRide(com.aralhub.network.models.websocketclient.ClientRideRequest(
            passengerId = clientRideRequest.passengerId,
            baseAmount = clientRideRequest.baseAmount,
            recommendedAmount = ClientRideRequestRecommendedAmount(
                minAmount = clientRideRequest.recommendedAmount.minAmount,
                maxAmount = clientRideRequest.recommendedAmount.maxAmount,
                recommendedAmount = clientRideRequest.recommendedAmount.recommendedAmount
            ),
            locations = ClientRideRequestLocations(
                points = clientRideRequest.locations.points.map { point ->
                    com.aralhub.network.models.websocketclient.ClientRideRequestLocationsItems(
                        coordinates = com.aralhub.network.models.websocketclient.ClientRideRequestLocationsItemsCoordinates(
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
                    recommendedAmount = com.aralhub.indrive.core.data.model.client.ClientRideResponseRecommendedAmount(
                        minAmount = it.data.recommendedAmount.minAmount,
                        maxAmount = it.data.recommendedAmount.maxAmount,
                        recommendedAmount = it.data.recommendedAmount.recommendedAmount
                    ),
                    locations = ClientRideResponseLocations(
                        points = it.data.locations.points.map { point ->
                            com.aralhub.indrive.core.data.model.client.ClientRideResponseLocationsItems(
                                coordinates = com.aralhub.indrive.core.data.model.client.ClientRideResponseLocationsItemsCoordinates(
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
                                endPoint =ClientRideResponseDistanceItemPoint(
                                    coordinates = ClientRideResponseDistanceItemStartPointCoordinates(
                                        longitude = segment.startPoint.coordinates.longitude,
                                        latitude = segment.startPoint.coordinates.latitude
                                    ),
                                    name = segment.endPoint.name
                                ) ?: ClientRideResponseDistanceItemPoint(
                                    coordinates = ClientRideResponseDistanceItemStartPointCoordinates(
                                        longitude = segment.startPoint.coordinates.longitude,
                                        latitude = segment.startPoint.coordinates.latitude
                                    ),
                                    name = segment.startPoint.name))
                        },
                        totalDistance = it.data.distance.totalDistance,
                        totalDuration = it.data.distance.totalDuration
                    ),
                    cancelCauseId = it.data.cancelCauseId
                ))
            }
        }
    }

    override suspend fun getActiveRideByPassenger(userId: Int): Result<ActiveRide> {
        return dataSource.getActiveRideByPassenger(userId).let {
            when (it) {
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(ActiveRide(
                    id = it.data.id,
                    uuid = it.data.uuid,
                    status = it.data.status,
                    amount = it.data.amount,
                    waitAmount = it.data.waitAmount,
                    distance = it.data.distance,
                    locations = it.data.locations,
                    isActive = it.data.isActive,
                    createdAt = it.data.createdAt,
                    driver = com.aralhub.indrive.core.data.model.ride.ActiveRideDriver(
                        driverId = it.data.driver.driverId,
                        fullName = it.data.driver.fullName,
                        rating = it.data.driver.rating,
                        vehicleColor = com.aralhub.indrive.core.data.model.ride.ActiveRideVehicleColor(
                            ru = it.data.driver.vehicleColor.ru,
                            en = it.data.driver.vehicleColor.en,
                            kk = it.data.driver.vehicleColor.kk
                        ),
                        vehicleType = com.aralhub.indrive.core.data.model.ride.ActiveRideVehicleType(
                            ru = it.data.driver.vehicleType.ru,
                            en = it.data.driver.vehicleType.en,
                            kk = it.data.driver.vehicleType.kk
                        ),
                        vehicleNumber = it.data.driver.vehicleNumber
                    ),
                    paymentMethod = com.aralhub.indrive.core.data.model.ride.ActiveRidePaymentMethod(
                        id = it.data.paymentMethod.id,
                        name = it.data.paymentMethod.name,
                        isActive = it.data.paymentMethod.isActive
                    ),
                    options = it.data.options.map { option ->
                        com.aralhub.indrive.core.data.model.ride.ActiveRideOptions(
                            id = option.id,
                            name = option.name
                        )
                    }
                ))
            }
        }
    }

    override suspend fun getSearchRideByPassengerId(userId: Int): Result<SearchRide> {
        return dataSource.getSearchRideByPassengerId(userId).let {
            when(it){
                is NetworkResult.Error -> Result.Error(it.message)
                is NetworkResult.Success -> Result.Success(
                    SearchRide(
                        uuid = it.data.uuid,
                        passenger = com.aralhub.indrive.core.data.model.ride.SearchRideDriver(
                            userId = it.data.passenger.userId,
                            userFullName = it.data.passenger.userFullName,
                            userRating = it.data.passenger.userRating
                        ),
                        baseAmount = it.data.baseAmount,
                        updatedAmount = it.data.updatedAmount ?: 0,
                        recommendedAmount = com.aralhub.indrive.core.data.model.ride.RecommendedAmount(
                            minAmount = it.data.recommendedAmount.minAmount,
                            maxAmount = it.data.recommendedAmount.maxAmount,
                            recommendedAmount = it.data.recommendedAmount.recommendedAmount
                        ),
                        locations = com.aralhub.indrive.core.data.model.ride.SearchRideLocations(
                            points = it.data.locations.points.map { point ->
                                com.aralhub.indrive.core.data.model.ride.LocationPoint(
                                    coordinates = com.aralhub.indrive.core.data.model.ride.LocationPointCoordinates(
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
                        distance = com.aralhub.indrive.core.data.model.ride.Distance(
                            segments = it.data.distance.segments.map { segment ->
                                com.aralhub.indrive.core.data.model.ride.DistanceSegment(
                                    distance = segment.distance,
                                    duration = segment.duration,
                                    startPoint = com.aralhub.indrive.core.data.model.ride.LocationPoint(
                                        coordinates = com.aralhub.indrive.core.data.model.ride.LocationPointCoordinates(
                                            longitude = segment.startPoint.coordinates.longitude,
                                            latitude = segment.startPoint.coordinates.latitude
                                        ),
                                        name = segment.startPoint.name
                                    ),
                                    endPoint = com.aralhub.indrive.core.data.model.ride.LocationPoint(
                                        coordinates = com.aralhub.indrive.core.data.model.ride.LocationPointCoordinates(
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
}