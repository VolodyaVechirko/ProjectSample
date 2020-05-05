package com.vvechirko.projectsample.gpay

import android.app.Activity
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object PaymentConfig {

    const val MERCHANT_NAME = "Merchant"

    const val GATEWAY = "portmonecom"
    const val PAYEE_ID = "xxxxx"

    /**
     * Creates an instance of [PaymentsClient] for use in an [Activity]
     *
     * @param activity is the caller's activity.
     */
    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
            .build()

        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private val baseRequest = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    /**
     * Describe your app's support for the CARD payment method.
     *
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * @see [PaymentMethod]
     */
    // Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun baseCardPaymentMethod() = JSONObject().apply {
        put("type", "CARD")
        put("parameters", JSONObject().apply {
            // Card authentication methods supported by your app and your gateway
            put("allowedAuthMethods", JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS")))
            // Card networks supported by your app and your gateway.
            put("allowedCardNetworks", JSONArray(listOf("MASTERCARD", "VISA")))

//            put("billingAddressRequired", true)
//            put("billingAddressParameters", JSONObject().apply {
//                put("format", "FULL")
//            })
        })
    }

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * @see [IsReadyToPayRequest]
     */
    fun isReadyToPayRequest() = try {
        baseRequest.apply {
            put("allowedPaymentMethods", JSONArray().apply {
                put(baseCardPaymentMethod())
            })
        }
    } catch (e: JSONException) {
        null
    }

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     * @see [PaymentDataRequest]
     */
    fun getPaymentDataRequest(price: Float) = try {
        baseRequest.apply {
            // A CARD PaymentMethod describing accepted cards and optional fields
            put("allowedPaymentMethods", JSONArray().apply {
                put(baseCardPaymentMethod().apply {
                    // Payment data tokenization for the CARD payment method
                    put("tokenizationSpecification", JSONObject().apply {
                        put("type", "PAYMENT_GATEWAY")
                        put("parameters", JSONObject().apply {
                            put("gateway", GATEWAY)
                            put("gatewayMerchantId", PAYEE_ID)
                        })
                    })
                })
            })

            // Information about the requested payment
            put("transactionInfo", JSONObject().apply {
                put("totalPrice", price)
                put("totalPriceStatus", "FINAL")
                put("countryCode", "UA")
                put("currencyCode", "UAH")
            })

            // Information about the merchant
            put("merchantInfo", JSONObject().apply {
                put("merchantName", MERCHANT_NAME)
            })

            // An optional shipping address requirement is a top-level property of the
//            put("shippingAddressRequired", true)
//            put("shippingAddressParameters", JSONObject().apply {
//                put("phoneNumberRequired", false)
//                put("allowedCountryCodes", JSONArray(listOf("UA")))
//            })
        }
    } catch (e: JSONException) {
        null
    }
}