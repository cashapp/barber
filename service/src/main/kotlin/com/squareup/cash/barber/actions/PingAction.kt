package com.squareup.cash.barber.actions

import com.squareup.protos.cash.barber.service.PingRequest
import com.squareup.protos.cash.barber.service.PingResponse
import misk.security.authz.Unauthenticated
import misk.web.Post
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import javax.inject.Inject
import javax.inject.Singleton

/** A ping web action. */
@Singleton
class PingAction @Inject constructor() : WebAction {
  @Post("/ping")
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  @Unauthenticated
  fun ping(@RequestBody requestBody: PingRequest): PingResponse {
    return PingResponse.Builder()
      .message("pong ${requestBody.message}")
      .build()
  }

}