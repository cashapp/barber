package com.squareup.barber

/**
 * Renders a specific DocumentSpec from a DocumentCopy that has a specific source Copy Model
 * Pulls DocumentCopy from Barber's in-memory installed DocumentCopy
 */
interface SpecRenderer<C : CopyModel, D : DocumentSpec> {
  fun render(copyModel: C): D
}
