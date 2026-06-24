package com.maalsaathi.app.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApiService {
    @POST("api/users/register")
    suspend fun register(@Body body: JsonObject): Response<JsonObject>

    @POST("api/users/{userId}/trips")
    suspend fun createTrip(@Path("userId") userId: String, @Body body: JsonObject): Response<JsonObject>

    @GET("api/users/{userId}/trips")
    suspend fun listTrips(@Path("userId") userId: String, @Query("status") status: String? = null): Response<List<JsonObject>>

    @GET("api/users/{userId}/trips/active")
    suspend fun getActiveTrip(@Path("userId") userId: String): Response<JsonObject>

    @GET("api/trips/{tripId}")
    suspend fun getTripById(@Path("tripId") tripId: String): Response<JsonObject>

    @PUT("api/trips/{tripId}/complete")
    suspend fun completeTrip(@Path("tripId") tripId: String): Response<JsonObject>

    @PUT("api/trips/{tripId}/cancel")
    suspend fun cancelTrip(@Path("tripId") tripId: String): Response<JsonObject>

    @POST("api/users/{userId}/expenses")
    suspend fun addExpense(@Path("userId") userId: String, @Body body: JsonObject): Response<JsonObject>

    @GET("api/users/{userId}/profit")
    suspend fun getProfit(@Path("userId") userId: String, @Query("period") period: String): Response<JsonObject>
}
