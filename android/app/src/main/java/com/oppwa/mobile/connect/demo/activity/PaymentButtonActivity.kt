package com.oppwa.mobile.connect.demo.activity

import android.content.ComponentName
import android.graphics.Color
import android.os.Bundle
import com.oppwa.mobile.connect.checkout.dialog.PaymentButtonFragment
import com.oppwa.mobile.connect.demo.R
import com.oppwa.mobile.connect.demo.common.Constants
import com.oppwa.mobile.connect.demo.receiver.CheckoutBroadcastReceiver
import com.oppwa.mobile.connect.exception.PaymentException
import kotlinx.android.synthetic.main.activity_payment_button.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Represents an activity for making payments via {@link PaymentButtonSupportFragment}.
 */
class PaymentButtonActivity : BasePaymentActivity() {

    private lateinit var paymentButtonFragment: PaymentButtonFragment

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment_button)

        val amount = Constants.Config.AMOUNT + " " + Constants.Config.CURRENCY
        amount_text_view.text = amount

        progressBar = progress_bar_payment_button

        initPaymentButton()
    }

    override fun onCheckoutIdReceived(checkoutId: String?) {
        super.onCheckoutIdReceived(checkoutId)

        checkoutId?.let { pay(checkoutId) }
    }

    @ExperimentalCoroutinesApi
    private fun initPaymentButton() {
        paymentButtonFragment = payment_button_fragment as PaymentButtonFragment

        paymentButtonFragment.paymentBrand = Constants.Config.PAYMENT_BUTTON_BRAND
        paymentButtonFragment.paymentButton.apply {
            setOnClickListener {
                requestCheckoutId()
            }

            /* Customize the payment button (except Google Pay button) */
            setBackgroundResource(R.drawable.button_base_background)
            setColorFilter(Color.rgb(255, 255, 255))
        }
    }

    private fun pay(checkoutId: String) {
        val checkoutSettings = createCheckoutSettings(checkoutId, getString(R.string.payment_button_callback_scheme))

        /* Set componentName if you want to receive callbacks from the checkout */
        val componentName = ComponentName(packageName, CheckoutBroadcastReceiver::class.java.name)

        try {
            paymentButtonFragment.submitTransaction(checkoutSettings, componentName)
        } catch (e: PaymentException) {
            showAlertDialog(R.string.error_message)
        }
    }
}