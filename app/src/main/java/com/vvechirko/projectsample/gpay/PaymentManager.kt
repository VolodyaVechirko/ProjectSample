package com.vvechirko.projectsample.gpay

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import org.json.JSONException
import org.json.JSONObject

/**
 * Checkout implementation for the app
 */
class PaymentManager(activity: Activity, val availableListener: ((Boolean) -> Unit)? = null) {

    /**
     * A client for interacting with the Google Pay API.
     *
     * @see [PaymentsClient]
     */
    private val paymentsClient = PaymentConfig.createPaymentsClient(activity)

    init {
        possiblyShowGooglePayButton()
    }

    /**
     * Determine the viewer's ability to pay with a payment method supported by your app and display a
     * Google Pay payment button.
     *
     * @see [IsReadyToPayRequest]
     */
    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson = PaymentConfig.isReadyToPayRequest() ?: return
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString()) ?: return

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener { completedTask ->
            try {
                completedTask.getResult(ApiException::class.java)?.let {
                    availableListener?.invoke(it)
                }
            } catch (exception: ApiException) {
                // Process error
                availableListener?.invoke(false)
                Log.w("isReadyToPay failed", exception)
            }
        }
    }

    fun requestPayment(activity: Activity, code: Int, price: Float) {
        // The price provided to the API should include taxes and shipping.
        val paymentDataRequestJson = PaymentConfig.getPaymentDataRequest(price)
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request), activity, code
            )
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result from an Activity]
     */
    fun payRequestResult(resultCode: Int, data: Intent?): PaymentResult {
        // value passed in AutoResolveHelper
        when (resultCode) {
            Activity.RESULT_OK ->
                data?.let { intent ->
                    PaymentData.getFromIntent(intent)?.let {
                        return handlePaymentSuccess(it)
                    }
                }
            Activity.RESULT_CANCELED -> {
                // Nothing to do here normally - the user simply cancelled without selecting a
                // payment method.
            }

            AutoResolveHelper.RESULT_ERROR -> {
                AutoResolveHelper.getStatusFromIntent(data)?.let {
                    Log.w("loadPaymentData failed", "Error code: ${it.statusCode}")
                    return PaymentResult.Error(it.statusMessage.toString())
                }
            }
        }
        // Re-enables the Google Pay payment button.
        return PaymentResult.Empty
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [PaymentData]
     */
    private fun handlePaymentSuccess(paymentData: PaymentData): PaymentResult {
        val paymentInformation = paymentData.toJson() ?: return PaymentResult.Empty

        return try {
            // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
            val data = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            Log.d("paymentMethodData", data.toString())
            val token = data.getJSONObject("tokenizationData").getString("token")
            PaymentResult.Success(token)
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
            PaymentResult.Error("Error: $e")
        }

    }

    sealed class PaymentResult {
        class Success(val token: String) : PaymentResult()
        class Error(val error: String) : PaymentResult()
        object Empty : PaymentResult()
    }
}