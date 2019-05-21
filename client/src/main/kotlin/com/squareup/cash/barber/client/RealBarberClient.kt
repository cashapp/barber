package com.squareup.cash.barber.client

import com.google.common.base.Preconditions.checkState
import com.squareup.protos.cash.barber.service.PingRequest
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


/** A real client that uses Retrofit to make HTTP calls and Moshi for JSON serialization/de-serialization. */
@Singleton class RealBarberClient @Inject constructor(
    @Named("barber") private val api: BarberApi) : BarberClient {
  companion object {
    fun create(hostname: String, okHttpClient: OkHttpClient) : BarberClient {
      return RealBarberClient(BarberApi.create(hostname, okHttpClient))
    }
  }

  override fun ping(message: String): String {
    val request = PingRequest.Builder().message(message).build()
    val response = api.ping(request).execute()

    checkState(response.isSuccessful)
    return response.body()!!.message
  }
}