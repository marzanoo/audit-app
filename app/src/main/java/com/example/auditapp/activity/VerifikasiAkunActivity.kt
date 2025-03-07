package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.model.OtpRequest
import com.example.auditapp.model.OtpResponse
import com.example.auditapp.model.VerifikasiOtpRequest
import com.example.auditapp.model.VerifikasiOtpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifikasiAkunActivity : AppCompatActivity() {
    private lateinit var resendButton : Button
    private lateinit var countdownText : TextView
    private lateinit var otpEditText : EditText
    private lateinit var verifikasiButton : Button
    private var countDownTimer: CountDownTimer? = null
    private val cooldownTime = 60000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi_akun)

        resendButton = findViewById(R.id.resendButton)
        countdownText = findViewById(R.id.countdownText)
        otpEditText = findViewById(R.id.otpVerifikasiET)
        verifikasiButton = findViewById(R.id.verifikasiBtn)

        val sharedPref = getSharedPreferences("RegisterPref", MODE_PRIVATE)
        val email = sharedPref.getString("email", null)

        verifikasiButton.setOnClickListener {
            val otp = otpEditText.text.toString()

            if (otp.isNotEmpty() || otp.length != 6) {
                val verifikasiRequest = VerifikasiOtpRequest(email.toString(), otp)
                val apiServices = NetworkConfig().getServices()
                apiServices.verifyOtp(verifikasiRequest).enqueue(object : Callback<VerifikasiOtpResponse> {
                    override fun onResponse(call: Call<VerifikasiOtpResponse>, response: Response<VerifikasiOtpResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerifikasiAkunActivity, "Verifikasi Email Berhasil", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@VerifikasiAkunActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            if (errorMessage?.contains("OTP salah atau kadaluarsa") == true) {
                                Toast.makeText(this@VerifikasiAkunActivity, "OTP salah atau kadaluarsa", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@VerifikasiAkunActivity, "Verifikasi gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    override fun onFailure(call: Call<VerifikasiOtpResponse>, t: Throwable) {
                        Toast.makeText(this@VerifikasiAkunActivity, "Verifikasi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "OTP tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        resendButton.setOnClickListener {
            email?.let {
                resendOtp(it)
                startCountdown()
            } ?: Toast.makeText(this, "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resendOtp(email: String) {
        val apiServices = NetworkConfig().getServices()
        apiServices.resendOtp(OtpRequest(email)).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful) {
                    Log.d("RESEND_OTP", "Verifikasi berhasil dikirim ke $email")
                    Toast.makeText(this@VerifikasiAkunActivity, "Verifikasi berhasil dikirim ke $email", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("RESEND_OTP", "Gagal mengirim Email: $errorBody")
                    Toast.makeText(this@VerifikasiAkunActivity, "Gagal mengirim Email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Log.e("RESEND_OTP", "Terjadi kesalahan: ${t.message}", t)
                Toast.makeText(this@VerifikasiAkunActivity, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun startCountdown() {
        resendButton.isEnabled = false
        countDownTimer = object : CountDownTimer(cooldownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                countdownText.text = "$secondsRemaining detik"
            }

            override fun onFinish() {
                resendButton.isEnabled = true
                countdownText.text = "Silahkan tekan tombol di atas"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}