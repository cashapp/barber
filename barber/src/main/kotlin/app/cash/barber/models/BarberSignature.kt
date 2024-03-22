package app.cash.barber.models

import app.cash.protos.barber.api.BarberSignature.Type
import app.cash.protos.barber.api.DocumentData
import app.cash.protos.barber.api.DocumentTemplate
import com.github.mustachejava.MustacheFactory
import java.io.StringReader
import java.time.Duration
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

/** Deterministic signature of the required fields and respective types */
data class BarberSignature(
  val signature: String,
  val fields: Map<String, Type>
) {
  constructor(
    signature: String
  ) : this(signature, decode(signature))

  constructor(
    fields: Map<String, Type>
  ) : this(encode(fields), fields.toSortedMap())

  /** Support recursive, primitive types in [DocumentData] protos */

  /** Return true if this signature can satisfy the target signature including types (ie. superset of fields) */
  fun canSatisfy(target: BarberSignature) = fields.entries.containsAll(target.fields.entries)

  /** Return true if this signature can satisfy the target signature ignorant of types (ie. superset of fields) */
  fun canSatisfyNaively(target: BarberSignature) = fields.keys.containsAll(target.fields.keys)

  companion object {
    private const val NAME_TYPE_SEPARATOR = ','
    private const val FIELD_SEPARATOR = ';'

    private fun encode(fields: Map<String, Type>): String = buildString {
      fields.toSortedMap().entries.mapIndexed { index, (name, type) ->
        if (index > 0) {
          append(FIELD_SEPARATOR)
        }
        append(name)
        append(NAME_TYPE_SEPARATOR)
        append(type.value)
      }
    }

    private fun decode(signature: String): Map<String, Type> = if (signature.isBlank()) {
      mapOf()
    } else {
      signature.split(FIELD_SEPARATOR)
          .associate { raw ->
            val fieldType = raw.split(NAME_TYPE_SEPARATOR)
            fieldType.first() to Type.values()[fieldType.last().toInt()]
          }.toSortedMap()
    }

    private fun DocumentData.asFieldsMap() = this.fields.associate { field ->
      val key = field.key!!
      val value = when {
        field.value_string != null -> Type.STRING
        field.value_long != null -> Type.LONG
        field.value_duration != null -> Type.DURATION
        field.value_instant != null -> Type.INSTANT
        field.value_null_type != null -> field.value_null_type!!
        // For cases where a DocumentData field in the template can be null, default to STRING
        else -> Type.STRING
      }
      key to value
    }

    fun DocumentData.getBarberSignature() = BarberSignature(asFieldsMap())

    private fun KClass<*>.asFieldsMap(includeNullableFields: Boolean = true): Map<String, Type> = memberProperties
      .filter { includeNullableFields || !it.returnType.isMarkedNullable }
      .mapToMaps { kProperty ->
      val name = kProperty.name
      // Only recursively define Signature for nested data classes
      val type = kProperty.returnType.classifier as? KClass<*>
          ?: error(
              "Failure to generate signature because of non-KClass<*> [memberProperty=$name] with [returnType=${kProperty.returnType}]"
          )
      when (type) {
        String::class -> mapOf(name to Type.STRING)
        Long::class -> mapOf(name to Type.LONG)
        Duration::class -> mapOf(name to Type.DURATION)
        Instant::class -> mapOf(name to Type.INSTANT)
        else -> {
          require(type.isData) {
            "Failure to generate signature because of [memberProperty=$name] which must be a data class [returnType=${kProperty.returnType}]"
          }
          type.asFieldsMap().mapKeys { "${name}.${it.key}" }
        }
      }
    }.toSortedMap()

    fun KClass<*>.getBarberSignature() = BarberSignature(asFieldsMap())
    fun app.cash.barber.models.DocumentData.getBarberSignature() = BarberSignature(
        this::class.asFieldsMap())

    /** BarberSignature is designated as naive since no type can be parsed from the code, so it is always String */
    fun DocumentTemplate.getNaiveSourceBarberSignature(mustacheFactory: MustacheFactory) = BarberSignature(
        fields.mapToMaps { field ->
          mustacheFactory
              .compile(StringReader(field.template!!), field.template)
              .codes.filterNot { it.name == null }.associate { code ->
                code.name to Type.STRING
              }
        }
    )

    fun Document.getBarberSignature() = BarberSignature(this::class.asFieldsMap())

    fun KClass<*>.getMinimumRequiredSignatureToSatisfy() =  BarberSignature(asFieldsMap(includeNullableFields = false))

  }
}
