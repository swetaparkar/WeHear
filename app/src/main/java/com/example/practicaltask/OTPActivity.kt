package com.example.practicaltask

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.practicaltask.databinding.ActivityOtpBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.phone.SmsRetrieverStatusCodes
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var OTP: String
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleBar.textTitle.text = getString(R.string.we_hear_innovation)

        OTP = intent.getStringExtra("OTP").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        phoneNumber = intent.getStringExtra("phoneNumber")!!

        auth = FirebaseAuth.getInstance()
        binding.otpProgressBar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPTvVisibility()
        binding.etOne.requestFocus()
        startSmsRetriever()

        binding.txtResendCode.setOnClickListener {
            resendVerificationCode()
            resendOTPTvVisibility()
        }

        binding.btnSubmit.setOnClickListener {
            val typedOTP =
                (binding.etOne.text.toString() + binding.etTwo.text.toString() +
                        binding.etThree.text.toString() + binding.etFour.text.toString() +
                        binding.etFive.text.toString() + binding.etSix.text.toString())

            if (typedOTP.isNotEmpty()) {
                if (typedOTP.length == 6) {
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    showLoader(true)
                    binding.otpProgressBar.visibility =
                        View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                } else {
                    Toast.makeText(this, "Please Enter Correct OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showLoader(show: Boolean) {
        binding.otpLoader.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun startSmsRetriever() {
        val client: SmsRetrieverClient = SmsRetriever.getClient(this)

        val task: Task<Void> = client.startSmsRetriever()
        task.addOnSuccessListener {
            Log.d("OTPActivity", "SMS Retriever started")
        }
        task.addOnFailureListener {
            Log.e("OTPActivity", "Failed to start SMS Retriever: ${it.message}")
        }

        SmsRetriever.SMS_RETRIEVED_ACTION?.let { action ->
            registerReceiver(SmsBroadcastReceiver(), IntentFilter(action))
        }
    }

    private fun resendOTPTvVisibility() {
        binding.apply {
            etOne.setText("")
            etTwo.setText("")
            etThree.setText("")
            etFour.setText("")
            etFive.setText("")
            etSix.setText("")
            txtResendCode.visibility = View.INVISIBLE
            txtResendCode.isEnabled = false
        }

        lifecycleScope.launchWhenStarted {
            delay(60000L)
            binding.txtResendCode.visibility = View.VISIBLE
            binding.txtResendCode.isEnabled = true
        }
    }

    private fun resendVerificationCode() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("OTPActivity", "onVerificationFailed: ${e.toString()}")
            } else {
                Log.d("OTPActivity", "onVerificationFailed: ${e.toString()}")
            }
            binding.otpProgressBar.visibility = View.INVISIBLE
            Toast.makeText(this@OTPActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String, token: PhoneAuthProvider.ForceResendingToken
        ) {
            OTP = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_SHORT).show()
                sendToMain()
            } else {
                Log.d("OTPActivity", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
            binding.otpProgressBar.visibility = View.INVISIBLE
        }
    }

    private fun sendToMain() {
        startActivity(Intent(this, ContactListActivity::class.java))
        finish()
    }

    private fun addTextChangeListener() {
        binding.apply {
            etOne.addTextChangedListener(EditTextWatcher(etOne))
            etTwo.addTextChangedListener(EditTextWatcher(etTwo))
            etThree.addTextChangedListener(EditTextWatcher(etThree))
            etFour.addTextChangedListener(EditTextWatcher(etFour))
            etFive.addTextChangedListener(EditTextWatcher(etFive))
            etSix.addTextChangedListener(EditTextWatcher(etSix))
        }
    }

    inner class EditTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            val text = p0.toString()
            when (view.id) {
                R.id.etOne -> if (text.length == 1) binding.etTwo.requestFocus()
                R.id.etTwo -> if (text.length == 1) binding.etThree.requestFocus() else if (text.isEmpty()) binding.etOne.requestFocus()
                R.id.etThree -> if (text.length == 1) binding.etFour.requestFocus() else if (text.isEmpty()) binding.etTwo.requestFocus()
                R.id.etFour -> if (text.length == 1) binding.etFive.requestFocus() else if (text.isEmpty()) binding.etThree.requestFocus()
                R.id.etFive -> if (text.length == 1) binding.etSix.requestFocus() else if (text.isEmpty()) binding.etFour.requestFocus()
                R.id.etSix -> if (text.isEmpty()) binding.etFive.requestFocus()
            }
        }
    }

    inner class SmsBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val extras = intent.extras
                if (extras != null) {
                    val status = extras[SmsRetriever.EXTRA_STATUS] as Status?
                    when (status?.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?

                            if (message != null) {
                                val otp = extractOtpFromMessage(message)
                                fillOtpInEditText(otp)
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            Toast.makeText(this@OTPActivity, "Failed to Fetch OTP", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        private fun extractOtpFromMessage(message: String): String {
            return message.takeLast(6)
        }

        private fun fillOtpInEditText(otp: String) {
            binding.apply {
                etOne.setText(otp[0].toString())
                etTwo.setText(otp[1].toString())
                etThree.setText(otp[2].toString())
                etFour.setText(otp[3].toString())
                etFive.setText(otp[4].toString())
                etSix.setText(otp[5].toString())

                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    OTP, otp
                )
                otpProgressBar.visibility = View.VISIBLE
                signInWithPhoneAuthCredential(credential)
            }
        }
    }
}









