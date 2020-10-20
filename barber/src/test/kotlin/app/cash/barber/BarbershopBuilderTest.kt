package app.cash.barber

import app.cash.barber.examples.EmptyDocumentData
import app.cash.barber.examples.EncodingTestDocument
import app.cash.barber.examples.InvestmentPurchase
import app.cash.barber.examples.NoParametersDocument
import app.cash.barber.examples.RecipientReceipt
import app.cash.barber.examples.SenderReceipt
import app.cash.barber.examples.ShadowEncodingEverythingPlaintextTestDocument
import app.cash.barber.examples.ShadowPlaintextDocument
import app.cash.barber.examples.TransactionalEmailDocument
import app.cash.barber.examples.TransactionalSmsDocument
import app.cash.barber.examples.investmentPurchaseEncodingDocumentTemplateEN_US
import app.cash.barber.examples.investmentPurchaseShadowEncodingDocumentTemplateEN_US
import app.cash.barber.examples.mcDonaldsInvestmentPurchase
import app.cash.barber.examples.noParametersDocumentTemplate
import app.cash.barber.examples.plaintextDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_CA
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_GB
import app.cash.barber.examples.recipientReceiptSmsDocumentTemplateEN_US
import app.cash.barber.examples.recipientReceiptSmsEmailDocumentTemplateEN_US
import app.cash.barber.examples.senderReceiptEmailDocumentTemplateEN_US
import app.cash.barber.models.BarberFieldEncoding
import app.cash.barber.models.DocumentData
import app.cash.barber.models.DocumentTemplate
import app.cash.barber.models.Locale
import app.cash.barber.models.TemplateToken.Companion.getTemplateToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BarbershopBuilderTest {
  @Test
  fun `Install works`() {
    BarbershopBuilder()
        .installDocument<TransactionalSmsDocument>()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .build()
  }

  @Test
  fun `Install works regardless of order`() {
    BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocument<TransactionalSmsDocument>()
        .build()
  }

  @Test
  fun `Install multiple locales`() {
    BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_CA)
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_GB)
        .installDocument<TransactionalSmsDocument>()
        .build()
  }

  @Test
  fun `Install multiple documents`() {
    BarbershopBuilder()
        .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsEmailDocumentTemplateEN_US)
        .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
        .installDocumentTemplate<InvestmentPurchase>(
            investmentPurchaseEncodingDocumentTemplateEN_US)
        .installDocument<EncodingTestDocument>()
        .installDocument<TransactionalEmailDocument>()
        .installDocument<TransactionalSmsDocument>()
        .build()
  }

  @Test
  fun `Fails when DocumentTemplate target Documents are not installed`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
          .installDocumentTemplate<SenderReceipt>(senderReceiptEmailDocumentTemplateEN_US)
          .installDocument<TransactionalEmailDocument>()
          .build()
    }
    assertEquals("""
      |Errors
      |1) Attempted to install DocumentTemplate without the corresponding Document being installed.
      |Not installed DocumentTemplate.target_signatures:
      |[BarberSignature(signature=sms_body,1, fields={sms_body=STRING})]
      |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `Fails when DocumentTemplate installed with non-source DocumentData`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<SenderReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
          .installDocument<TransactionalSmsDocument>()
          .build()
    }
    assertEquals("""
      |Errors
      |1) Attempted to install DocumentTemplate with a DocumentData not specified in the DocumentTemplate source
      |DocumentTemplate.source: app.cash.barber.examples.RecipientReceipt
      |DocumentData: app.cash.barber.examples.SenderReceipt
      |
      """.trimMargin(), exception.toString())
  }

  @Test
  fun `Fails when Document has no parameters`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocument<NoParametersDocument>()
          .installDocumentTemplate<EmptyDocumentData>(noParametersDocumentTemplate)
          .build()
    }
    assertEquals("""
      |Errors
      |1) No fields included for Document [class app.cash.barber.examples.NoParametersDocument]
      |
      """.trimMargin(), exception.toString())
  }

  @Test
  fun `Fails on unused dangling installed Document`() {
    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocument<TransactionalEmailDocument>()
          .installDocumentTemplate<RecipientReceipt>(recipientReceiptSmsDocumentTemplateEN_US)
          .installDocument<TransactionalSmsDocument>()
          .setWarningsAsErrors()
          .build()
    }
    assertEquals("""
      |Warnings
      |1) Document installed that is not used in any installed DocumentTemplates
      |[app.cash.barber.examples.TransactionalEmailDocument]
      |
      """.trimMargin(), exception.toString())
  }

  @Test
  fun `Renders correctly Documents with shadow fields with different encoding`() {
    val barber = BarbershopBuilder()
        .installDocumentTemplate<InvestmentPurchase>(
            investmentPurchaseShadowEncodingDocumentTemplateEN_US)
        .installDocument<EncodingTestDocument>()
        .installDocument<ShadowEncodingEverythingPlaintextTestDocument>()
        .setWarningsAsErrors()
        .build()

    val specRegular = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
        .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(specRegular).isEqualTo(
        EncodingTestDocument(
            no_annotation_field = "You purchased 100 shares of McDonald&#39;s.",
            default_field = "You purchased 100 shares of McDonald&#39;s.",
            html_field = "You purchased 100 shares of McDonald&#39;s.",
            plaintext_field = "You purchased 100 shares of McDonald's."
        )
    )

    val specShadow =
        barber.getBarber<InvestmentPurchase, ShadowEncodingEverythingPlaintextTestDocument>()
            .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(specShadow).isEqualTo(
        ShadowEncodingEverythingPlaintextTestDocument(
            // Notice all shadowed fields are still rendered with the correct Plaintext encoding
            // even though they conflict in name with the above target document that has HTML encoding
            no_annotation_field = "You purchased 100 shares of McDonald's.",
            default_field = "You purchased 100 shares of McDonald's.",
            html_field = "You purchased 100 shares of McDonald's.",
            plaintext_field = "You purchased 100 shares of McDonald's.",
            non_shadow_field = "You purchased 100 shares of McDonald's."
        )
    )
  }

  @Test
  fun `Barbershop does not include documents as targets where template does not satisfy all fields`() {
    val barber = BarbershopBuilder()
        .installDocumentTemplate<InvestmentPurchase>(
            investmentPurchaseEncodingDocumentTemplateEN_US)
        .installDocumentTemplate<SenderReceipt>(plaintextDocumentTemplateEN_US)
        .installDocument<EncodingTestDocument>()
        .installDocument<ShadowPlaintextDocument>()
        .setWarningsAsErrors()
        .build()

    // investmentPurchaseEncodingDocumentTemplateEN_US only specifies EncodingTestDocument
    //    but can support ShadowPlaintextDocument so it is included
    assertEquals(setOf(EncodingTestDocument::class, ShadowPlaintextDocument::class),
        barber.getTargetDocuments(InvestmentPurchase::class.getTemplateToken()))
    assertEquals(setOf(ShadowPlaintextDocument::class),
        barber.getTargetDocuments(SenderReceipt::class.getTemplateToken()))
  }

  @Test
  fun `setDefaultBarberFieldEncoding changes non-annotated field render encoding`() {
    val barber = BarbershopBuilder()
        .installDocument<EncodingTestDocument>()
        .installDocumentTemplate<InvestmentPurchase>(
            investmentPurchaseEncodingDocumentTemplateEN_US)
        .setDefaultBarberFieldEncoding(BarberFieldEncoding.STRING_PLAINTEXT)
        .build()

    val spec = barber.getBarber<InvestmentPurchase, EncodingTestDocument>()
        .render(mcDonaldsInvestmentPurchase, Locale.EN_US)

    assertThat(spec).isEqualTo(
        EncodingTestDocument(
            // Using the default HTML encoding, the apostrophe in no_annotation_field have been escaped.
            no_annotation_field = "You purchased 100 shares of McDonald's.",
            default_field = "You purchased 100 shares of McDonald&#39;s.",
            html_field = "You purchased 100 shares of McDonald&#39;s.",
            plaintext_field = "You purchased 100 shares of McDonald's."
        )
    )
  }

  @Test
  fun `Fails when variable in field template is not in source DocumentData`() {
    data class TradeReceipt(
      val ticker: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
        fields = mapOf(
            "sms_body" to "You bought {{ shares }} shares of {{ ticker }}."
        ),
        source = TradeReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocument<TransactionalSmsDocument>()
          .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
          .build()
    }
    assertEquals(
        """
        |Errors
        |1) Missing variable [shares] for DocumentData with [templateToken=tradeReceipt] for DocumentTemplate field [Field{key=sms_body, template=You bought \{\{ shares \}\} shares of \{\{ ticker \}\}.}]
        |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `Fails when variable in data is not used in any field template`() {
    data class TradeReceipt(
      val ticker: String,
      val shares: String
    ) : DocumentData

    val tradeReceiptEN_US = DocumentTemplate(
        fields = mapOf(
            "sms_body" to "Welcome to the {{ ticker }} shareholder family!"
        ),
        source = TradeReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocument<TransactionalSmsDocument>()
          .installDocumentTemplate<TradeReceipt>(tradeReceiptEN_US)
          .setWarningsAsErrors()
          .build()
    }
    assertEquals(
        """
        |Warnings
        |1) Unused DocumentData variable [shares] in Source signature [shares,1;ticker,1] with no
        |usage in DocumentTemplate: [templateToken=tradeReceipt][locale=en-US][version=1] 
        |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `Fails when DocumentTemplate does not have sufficient fields for target Document`() {
    val recipientReceiptDocumentTemplate = DocumentTemplate(
        fields = mapOf(
            "subject" to "{{ sender }} sent {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
        ),
        source = RecipientReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentTemplate)
          .installDocument<TransactionalSmsDocument>()
          .build()
    }
    assertEquals(
        """
        |Errors
        |1) Installed DocumentTemplate: [templateToken=recipientReceipt][locale=en-US][version=1]
        |missing required fields for Document targets:
        |[document=app.cash.barber.examples.TransactionalSmsDocument] requires missing [field=sms_body]
        |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `DocumentTemplate has extra fields unused by target Documents`() {
    val recipientReceiptDocumentData = DocumentTemplate(
        fields = mapOf(
            "sms_body" to "{{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}",
            "extra_field" to "This field is unused in any target document"
        ),
        source = RecipientReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<RecipientReceipt>(recipientReceiptDocumentData)
          .installDocument<TransactionalSmsDocument>()
          .build()
    }
    assertEquals(
        """
        |Errors
        |1) Installed DocumentTemplate has additional fields that are not used in any target Document
        |Additional fields:
        |extra_field
        |
      """.trimMargin(),
        exception.toString())
  }

  @Test
  fun `Fails on duplicate install of DocumentTemplate`() {
    val first = DocumentTemplate(
        fields = mapOf(
            "sms_body" to "first {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
        ),
        source = RecipientReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )
    val second = DocumentTemplate(
        fields = mapOf(
            "sms_body" to "second {{ sender }} sent you {{ amount }} on {{ deposit_expected_at }}. Cancel here: {{ cancelUrl }}"
        ),
        source = RecipientReceipt::class,
        targets = setOf(TransactionalSmsDocument::class),
        locale = Locale.EN_US
    )

    val exception = assertFailsWith<BarberException> {
      BarbershopBuilder()
          .installDocumentTemplate<RecipientReceipt>(first)
          .installDocumentTemplate<RecipientReceipt>(second)
          .installDocument<TransactionalSmsDocument>()
          .build()
    }
    assertEquals(
        """
        |Errors
        |1) Attempted to install DocumentTemplate that will overwrite an already installed locale version
        |DocumentTemplate: [templateToken=recipientReceipt][locale=en-US][version=1]
        |
        |Already Installed DocumentTemplates
        |TemplateToken: recipientReceipt
        |Locales: [Locale=en-US]
        |Installed Versions: [1]
        |
      """.trimMargin(),
        exception.toString())
  }
}
