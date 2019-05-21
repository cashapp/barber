package com.squareup.cash.barber.client

import com.squareup.moshi.Moshi
import com.squareup.protos.cash.barber.service.PingRequest
import com.squareup.protos.cash.barber.service.PingResponse
import com.squareup.wire.WireJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/** The api exposed by barber. */
interface BarberApi {
  companion object {
    fun create(hostname: String, okHttpClient: OkHttpClient): BarberApi {
      val moshi = Moshi.Builder()
          .add(WireJsonAdapterFactory())
          .build()

      val retrofit = Retrofit.Builder()
          .baseUrl(hostname)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .client(okHttpClient)
          .build()

      return retrofit.create(BarberApi::class.java)
    }
  }

  @POST("/ping")
  @Headers(value = [
    "accept: application/json"
  ])
  fun ping(
    @Body request: PingRequest
  ): Call<PingResponse>
}