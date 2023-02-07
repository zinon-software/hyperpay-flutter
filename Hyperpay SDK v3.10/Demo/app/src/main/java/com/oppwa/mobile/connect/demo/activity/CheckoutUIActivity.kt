package com.oppwa.mobile.connect.demo.activity

import android.content.ComponentName
import android.os.Bundle
import com.oppwa.mobile.connect.checkout.dialog.CheckoutActivity
import com.oppwa.mobile.connect.demo.R
import com.oppwa.mobile.connect.demo.common.Constants
import com.oppwa.mobile.connect.demo.receiver.CheckoutBroadcastReceiver
import kotlinx.android.synthetic.main.activity_checkout_ui.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Represents an activity for making payments via {@link CheckoutActivity}.
 */
class CheckoutUIActivity : BasePaymentActivity() {

    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_checkout_ui)

        val amount = Constants.Config.AMOUNT + " " + Constants.Config.CURRENCY

        amount_text_view.text = amount
        progressBar = progress_bar_checkout_ui

        button_proceed_to_checkout.setOnClickListener {
            requestCheckoutId()
        }
    }

    override fun onCheckoutIdReceived(checkoutId: String?) {
        super.onCheckoutIdReceived(checkoutId)
        if (checkoutId != null) {
            openCheckoutUI(checkoutId)
        }
    }

    private fun openCheckoutUI(checkoutId: String) {
        val checkoutSettings = createCheckoutSettings(checkoutId, getString(R.string.checkout_ui_callback_scheme))

        /* Set componentName if you want to receive callbacks from the checkout */
        val componentName = ComponentName(packageName, CheckoutBroadcastReceiver::class.java.name)

        /* Set up the Intent and start the checkout activity */
        val intent = checkoutSettings.createCheckoutActivityIntent(this, componentName)

        startActivityForResult(intent, CheckoutActivity.REQUEST_CODE_CHECKOUT)
    }
}