package com.squareup.barber

import kotlin.reflect.KClass

/**
 * An instance of Barber
 * 1) Keeps the installed DocumentCopy in memory
 * 2) Provides specRenderers from a passed in CopyModel to a DocumentSpec
 */
class Barber {
  private val installedCopyModel: MutableSet<KClass<out CopyModel>> = mutableSetOf()
  private val installedDocumentCopy: MutableSet<DocumentCopy> = mutableSetOf()
  private val installedDocumentSpec: MutableSet<KClass<out DocumentSpec>> = mutableSetOf()

  /**
   * @return a SpecRenderer from a specific CopyModel to a specific DocumentSpec
   */
  @Suppress("UNUSED_PARAMETER")
  fun <C : CopyModel, D : DocumentSpec> newSpecRenderer(
    copyModelClass: KClass<C>,
    documentSpecClass: KClass<D>
  ): SpecRenderer<C, D> {
    // TODO
    return object : SpecRenderer<C, D> {
      @Suppress("UNCHECKED_CAST") // Give me a break I'm testing yo.
      override fun render(copyModel: C): D {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

//        return TransactionalEmailDocumentSpec(
//          subject = "Sandy Winchester sent you $50",
//          headline = HtmlString("You received $50"),
//          short_description = "You’ve received a payment from Sandy Winchester! The money will be in your bank account ",
//          primary_button = "Cancel this payment",
//          primary_button_url = "https://cash.app/cancel/123",
//          secondary_button = null,
//          secondary_button_url = null
//        ) as D
      }
    }
  }

  /**
   * Consumes a [CopyModel] and corresponding [DocumentCopy] and persists in-memory
   * At boot, a service will call [installCopy] on all [CopyModel] and [DocumentCopy] to add to the in-memory Barber
   */
  fun installCopy(copyModel: KClass<out CopyModel>, documentCopy: DocumentCopy) {
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
    if (notInstalledDocumentSpec.isNotEmpty()) {
      throw BarberException(problems = listOf("""
        |Attempted to install DocumentCopy without the corresponding DocumentSpec being installed.
        |Not installed DocumentCopy.targets:
        |$notInstalledDocumentSpec
        """.trimMargin()))
    }
    installedCopyModel.add(copyModel)
    installedDocumentCopy.add(documentCopy)
  }

  inline fun <reified C : CopyModel> installCopy(documentCopy: DocumentCopy) = installCopy(C::class, documentCopy)

  /**
   * Consumes a [DocumentSpec] and persists in-memory
   * At boot, a service will call [installDocumentSpec] on all [DocumentSpec] to add to the in-memory Barber instance
   */
  fun installDocumentSpec(documentSpec: KClass<out DocumentSpec>) {
    installedDocumentSpec.add(documentSpec)
  }

  inline fun <reified D : DocumentSpec> installDocumentSpec() = installDocumentSpec(D::class)
}
