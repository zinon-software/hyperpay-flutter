package com.oppwa.mobile.connect.demo.activity

import android.content.Intent
import android.os.Bundle
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.WalletConstants
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity
import com.oppwa.mobile.connect.checkout.dialog.GooglePayHelper
import com.oppwa.mobile.connect.checkout.meta.CheckoutSettings
import com.oppwa.mobile.connect.checkout.meta.CheckoutSkipCVVMode
import com.oppwa.mobile.connect.demo.R
import com.oppwa.mobile.connect.demo.common.Constants
import com.oppwa.mobile.connect.demo.task.RequestManager
import com.oppwa.mobile.connect.exception.PaymentError
import com.oppwa.mobile.connect.provider.Connect
import com.oppwa.mobile.connect.provider.Transaction
import com.oppwa.mobile.connect.provider.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

private const val STATE_RESOURCE_PATH = "STATE_RESOURCE_PATH"

/**
 * Represents a base activity for making the payments with mobile sdk.
 * This activity handles payment callbacks.
 */
open class BasePaymentActivity : BaseActivity() {

    protected var resourcePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            resourcePath = savedInstanceState.getString(STATE_RESOURCE_PATH)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        /* Check of the intent contains the callback scheme */
        if (resourcePath != null && hasCallBackScheme(intent)) {
            requestPaymentStatus(resourcePath!!)
        }
    }

    @ExperimentalCoroutinesApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /* Override onActivityResult to get notified when the checkout process is done */
        if (requestCode == CheckoutActivity.REQUEST_CODE_CHECKOUT) {
            when (resultCode) {
                CheckoutActivity.RESULT_OK -> {
                    /* Transaction completed */
                    val transaction: Transaction = data!!.getParcelableExtra(
                            CheckoutActivity.CHECKOUT_RESULT_TRANSACTION)!!

                    resourcePath = data.getStringExtra(CheckoutActivity.CHECKOUT_RESULT_RESOURCE_PATH)

                    /* Check the transaction type */
                    if (transaction.transactionType == TransactionType.SYNC) {
                        /* Check the status of synchronous transaction */
                        requestPaymentStatus(resourcePath!!)
                    } else {
                        /* Asynchronous transaction is processed in the onNewIntent() */
                        hideProgressBar()
                    }
                }
                CheckoutActivity.RESULT_CANCELED -> hideProgressBar()
                CheckoutActivity.RESULT_ERROR -> {
                    hideProgressBar()

                    val error: PaymentError = data!!.getParcelableExtra(
                            CheckoutActivity.CHECKOUT_RESULT_ERROR)!!
                    showAlertDialog(error.errorMessage)
                }
            }
        }
    }

    open fun onCheckoutIdReceived(checkoutId: String?) {
        if (checkoutId == null) {
            hideProgressBar()
            showAlertDialog(R.string.error_message)
        }
    }

    @ExperimentalCoroutinesApi
    protected fun requestCheckoutId() {
        CoroutineScope(Dispatchers.Main).launch {
            showProgressBar()
            val checkoutId = RequestManager().requestCheckoutId(Constants.Config.AMOUNT, Constants.Config.CURRENCY)
            onCheckoutIdReceived(checkoutId)
        }
    }

    @ExperimentalCoroutinesApi
    protected fun requestPaymentStatus(resourcePath: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val paymentStatus = RequestManager().requestPaymentStatus(resourcePath)
            onPaymentStatusReceived(paymentStatus)
        }
    }

    protected fun createCheckoutSettings(checkoutId: String, callbackScheme: String): CheckoutSettings {
        return CheckoutSettings(checkoutId, Constants.Config.PAYMENT_BRANDS,
                Connect.ProviderMode.TEST)
                .setSkipCVVMode(CheckoutSkipCVVMode.FOR_STORED_CARDS)
                .setShopperResultUrl("$callbackScheme://callback")
                .setGooglePayPaymentDataRequest(getGooglePayRequest())
    }

    private fun hasCallBackScheme(intent: Intent): Boolean {
        return intent.scheme == getString(R.string.checkout_ui_callback_scheme) ||
                intent.scheme == getString(R.string.payment_button_callback_scheme) ||
                intent.scheme == getString(R.string.custom_ui_callback_scheme)
    }

    private fun onPaymentStatusReceived(paymentStatus: String?) {
        hideProgressBar()
        val message = if (paymentStatus == "OK") {
            R.string.message_successful_payment
        } else {
            R.string.message_unsuccessful_payment
        }

        showAlertDialog(message)
    }

    private fun getGooglePayRequest(): PaymentDataRequest {
        return GooglePayHelper.preparePaymentDataRequestBuilder(
                Constants.Config.AMOUNT,
                Constants.Config.CURRENCY,
                Constants.MERCHANT_ID,
                getPaymentMethodsForGooglePay(),
                getDefaultCardNetworksForGooglePay()
        ).build()
    }

    private fun getPaymentMethodsForGooglePay(): Array<Int> {
        return arrayOf(
                WalletConstants.PAYMENT_METHOD_CARD,
                WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
        )
    }

    private fun getDefaultCardNetworksForGooglePay(): Array<Int> {
        return arrayOf(
                WalletConstants.CARD_NETWORK_VISA,
                WalletConstants.CARD_NETWORK_MASTERCARD,
                WalletConstants.CARD_NETWORK_AMEX
        )
    }
}