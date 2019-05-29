package com.squareup.barber

import com.squareup.barber.models.CopyModel
import com.squareup.barber.models.DocumentCopy
import com.squareup.barber.models.DocumentSpec
import kotlin.reflect.KClass

/**
 * 1) Keeps the installed [CopyModel], [DocumentCopy], and [DocumentSpec] in memory
 * 2) Provides a render method to generate [DocumentSpec] from [DocumentCopy] template and [CopyModel] values
 */
interface Barber {
  fun <C : CopyModel, D : DocumentSpec> newRenderer(
    copyModelClass: KClass<out C>,
    documentSpecClass: KClass<out D>
  ): Renderer<C, D>

  class Builder {
    private val installedCopyModel: MutableSet<KClass<out CopyModel>> = mutableSetOf()
    private val installedDocumentCopy: MutableMap<KClass<out CopyModel>, DocumentCopy> = mutableMapOf()
    private val installedDocumentSpec: MutableSet<KClass<out DocumentSpec>> = mutableSetOf()

    /**
     * Consumes a [CopyModel] and corresponding [DocumentCopy] and persists in-memory
     * At boot, a service will call [installCopy] on all [CopyModel] and [DocumentCopy] to add to the in-memory Barber
     */
    fun installCopy(copyModel: KClass<out CopyModel>, documentCopy: DocumentCopy) = apply {
      if (documentCopy.source != copyModel) {
        throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy with a CopyModel not specific in the DocumentCopy source.
        |DocumentCopy.source: ${documentCopy.source}
        |CopyModel: $copyModel
        """.trimMargin()))
      }
      val notInstalledDocumentSpec = documentCopy.targets.filter {
        !installedDocumentSpec.contains(it)
      }
      // TODO move all these error checking to build() so order of install functions doesn't matter
      if (notInstalledDocumentSpec.isNotEmpty()) {
        throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy without the corresponding DocumentSpec being installed.
        |Not installed DocumentCopy.targets:
        |$notInstalledDocumentSpec
        """.trimMargin()))
      }
      if (installedDocumentCopy.containsKey(copyModel) && installedDocumentCopy[copyModel] != documentCopy) {
        throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy that matches an already installed CopyModel.
        |Already Installed
        |CopyModel: $copyModel
        |DocumentCopy: $installedDocumentCopy[copyModel]
        |
        |Attempted to Install
        |$documentCopy
      """.trimMargin()))
      }
      installedCopyModel.add(copyModel)
      installedDocumentCopy[copyModel] = documentCopy
    }

    inline fun <reified C : CopyModel> installCopy(documentCopy: DocumentCopy) = installCopy(C::class, documentCopy)

    /**
     * Consumes a [DocumentSpec] and persists in-memory
     * At boot, a service will call [installDocumentSpec] on all [DocumentSpec] to add to the in-memory Barber instance
     */
    fun installDocumentSpec(documentSpec: KClass<out DocumentSpec>) = apply {
      installedDocumentSpec.add(documentSpec)
    }

    inline fun <reified D : DocumentSpec> installDocumentSpec() = installDocumentSpec(D::class)

    fun build(): Barber = RealBarber(installedDocumentCopy.toMap())
  }
}

inline fun <reified C : CopyModel, reified D : DocumentSpec> Barber.newRenderer() = newRenderer(C::class, D::class)