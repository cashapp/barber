package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.DocumentSpec
import kotlin.reflect.KClass

/**
 * An instance of [Barber]:
 * 1) Keeps the installed [CopyModel], [DocumentCopy], and [DocumentSpec] in memory
 * 2) Provides a render method to generate [DocumentSpec] from [DocumentCopy] template and [CopyModel] values
 */
interface Barber {
  /**
   * @return a [DocumentSpec] with the values of a [CopyModel] instance rendered in the [DocumentCopy] template
   */
  fun <D: DocumentSpec>render(copyModel: CopyModel, documentSpecClass: KClass<out D>): D

  /**
   * Consumes a [CopyModel] and corresponding [DocumentCopy] and persists in-memory
   * At boot, a service will call [installCopy] on all [CopyModel] and [DocumentCopy] to add to the in-memory Barber
   */
  fun installCopy(copyModel: KClass<out CopyModel>, documentCopy: DocumentCopy)

  /**
   * Consumes a [DocumentSpec] and persists in-memory
   * At boot, a service will call [installDocumentSpec] on all [DocumentSpec] to add to the in-memory Barber instance
   */
  fun installDocumentSpec(documentSpec: KClass<out DocumentSpec>)
}

inline fun <reified D : DocumentSpec> Barber.render(copyModel: CopyModel): D = render(copyModel, D::class)

inline fun <reified C : CopyModel> Barber.installCopy(documentCopy: DocumentCopy) = installCopy(C::class, documentCopy)

inline fun <reified D : DocumentSpec> Barber.installDocumentSpec() = installDocumentSpec(D::class)
