package com.squareup.barber

import com.squareup.barber.models.CopyModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BarberRenderTest {
  private lateinit var barberRender: BarberRender

  @BeforeEach
  fun before() {
    barberRender = BarberRender()
  }

  @Test
  fun basic() {
    val person = Person(
      name = "World"
    )
    val template = "Hello {{name}}!"
    assertEquals("Hello World!", barberRender.render(template, person))
  }

  data class Person (
    val name: String
  ): CopyModel
}