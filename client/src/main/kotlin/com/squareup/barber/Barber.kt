package com.squareup.barber

import kotlin.reflect.KClass

/**
 * An instance of Barber
 * 1) Keeps the installed DocumentCopy in memory
 * 2) Provides specRenderers from a passed in CopyModel to a DocumentSpec
 */
class Barber {
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
   * Consumes a DocumentCopy and persists in memory
   */
  @Suppress("UNUSED_PARAMETER")
  fun installCopy(documentCopy: DocumentCopy) {
    // DO NOTHING YET!
  }
}