package com.example.broadcastreceiver_otp_auto_retrieve

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.broadcastreceiver_otp_auto_retrieve.SmsBroadcastReceiver.SmsBroadcastReceiverListener
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.textfield.TextInputEditText
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private val REQ_USER_CONSENT = 200
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    var etOTP: TextInputEditText? = null
    var verifyOtp: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etOTP = findViewById(R.id.otp_field)
        verifyOtp = findViewById(R.id.btn_verify)
        startSmartUserConsent()
    }

    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        /*If we know the number from which we receive OTP, specify that particular number.
         If OTP is from unknown number mark it as null*/
        client.startSmsUserConsent("+9177******16")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                if (message != null) {
                    getOtpFromMessage(message)
                }
            }
        }
    }

    // Getting the OTP code the from the message received by Broadcast Receiver
    private fun getOtpFromMessage(message: String?) {
        val otpPattern: Pattern = Pattern.compile("(|^)\\d{6}")
        val matcher = otpPattern.matcher(message!!)
        if (matcher.find()) {
            etOTP!!.setText(matcher.group())
            verifyOtp()
        }
    }

    private fun verifyOtp() {
        verifyOtp?.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
        }
    }

    //Registering the broadcast receiver here
    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        Toast.makeText(this, "Broacast Receiver is registered", Toast.LENGTH_LONG).show()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }

}