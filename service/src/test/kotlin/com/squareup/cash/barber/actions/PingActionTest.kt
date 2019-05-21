package com.squareup.cash.barber.actions

import com.google.inject.Module
import com.squareup.protos.cash.barber.service.PingRequest
import misk.testing.MiskTest
import misk.testing.MiskTestModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MiskTest(startService = true)
class PingActionTest {
  @Suppress("unused")
  @MiskTestModule
  val module: Module = BarberTestingModule()

  @Inject lateinit var pingAction: PingAction

  @Test
  fun ping() {
    val pong = pingAction.ping(PingRequest.Builder()
      .message("hi")
      .build())

    assertThat(pong.message).isEqualTo("pong hi")
  }
}
