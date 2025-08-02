
package com.example.yumurtazamanlaycs

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Arayüz Elemanları
    private lateinit var anaBaslik: TextView
    private lateinit var textViewBoyutLabel: TextView
    private lateinit var radioGroupBoyut: RadioGroup
    private lateinit var radioButtonS: RadioButton
    private lateinit var radioButtonM: RadioButton
    private lateinit var radioButtonL: RadioButton
    private lateinit var radioButtonXL: RadioButton
    private lateinit var textViewKivamLabel: TextView
    private lateinit var radioGroupKivam: RadioGroup
    private lateinit var radioButtonAzPismis: RadioButton
    private lateinit var radioButtonRafadan: RadioButton
    private lateinit var radioButtonKayisi: RadioButton
    private lateinit var radioButtonKati: RadioButton
    private lateinit var textViewTimerDisplay: TextView
    private lateinit var progressBarTimer: ProgressBar
    private lateinit var buttonStart: Button

    // Zamanlayıcı
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var totalTimeInMillis: Long = 0 // Toplam süreyi saklamak için
    private var timerRunning: Boolean = false

    // Ses
    private var mediaPlayer: MediaPlayer? = null

    // Pişirme Süreleri (Boyut ve Kıvama Göre Milisaniye Cinsinden)
    // Bu süreler örnektir, kendi tercihlerinize göre ayarlayabilirsiniz.
    private val sureler = mapOf(
        // S Boyutu
        R.id.radioButtonS to mapOf(
            R.id.radioButtonAzPismis to 2.5 * 60 * 1000, // 2.5 dakika
            R.id.radioButtonRafadan to 3.5 * 60 * 1000,  // 3.5 dakika
            R.id.radioButtonKayisi to 5.5 * 60 * 1000,   // 5.5 dakika
            R.id.radioButtonKati to 8.5 * 60 * 1000      // 8.5 dakika
        ),
        // M Boyutu
        R.id.radioButtonM to mapOf(
            R.id.radioButtonAzPismis to 3.0 * 60 * 1000,
            R.id.radioButtonRafadan to 4.0 * 60 * 1000,
            R.id.radioButtonKayisi to 6.0 * 60 * 1000,
            R.id.radioButtonKati to 9.0 * 60 * 1000
        ),
        // L Boyutu
        R.id.radioButtonL to mapOf(
            R.id.radioButtonAzPismis to 3.5 * 60 * 1000,
            R.id.radioButtonRafadan to 4.5 * 60 * 1000,
            R.id.radioButtonKayisi to 6.5 * 60 * 1000,
            R.id.radioButtonKati to 9.5 * 60 * 1000
        ),
        // XL Boyutu
        R.id.radioButtonXL to mapOf(
            R.id.radioButtonAzPismis to 4.0 * 60 * 1000,
            R.id.radioButtonRafadan to 5.0 * 60 * 1000,
            R.id.radioButtonKayisi to 7.0 * 60 * 1000,
            R.id.radioButtonKati to 10.0 * 60 * 1000
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // XML layout dosyanızın adı activity_main.xml ise bu satır doğru

        // Arayüz elemanlarını ID'leri ile eşleştir
        anaBaslik = findViewById(R.id.anaBaslik)
        textViewBoyutLabel = findViewById(R.id.textViewBoyutLabel)
        radioGroupBoyut = findViewById(R.id.radioGroupBoyut)
        radioButtonS = findViewById(R.id.radioButtonS)
        radioButtonM = findViewById(R.id.radioButtonM)
        radioButtonL = findViewById(R.id.radioButtonL)
        radioButtonXL = findViewById(R.id.radioButtonXL)
        textViewKivamLabel = findViewById(R.id.textViewKivamLabel)
        radioGroupKivam = findViewById(R.id.radioGroupKivam)
        radioButtonAzPismis = findViewById(R.id.radioButtonAzPismis)
        radioButtonRafadan = findViewById(R.id.radioButtonRafadan)
        radioButtonKayisi = findViewById(R.id.radioButtonKayisi)
        radioButtonKati = findViewById(R.id.radioButtonKati)
        textViewTimerDisplay = findViewById(R.id.textViewTimerDisplay)
        progressBarTimer = findViewById(R.id.progressBarTimer)
        buttonStart = findViewById(R.id.buttonStart)

        // Başlangıçta bir boyut ve kıvam seçili olsun (isteğe bağlı)
        // radioButtonM.isChecked = true
        // radioButtonKayisi.isChecked = true

        // Başlat/Durdur Butonu Tıklama Olayı
        buttonStart.setOnClickListener {
            if (timerRunning) {
                stopTimer()
            } else {
                val seciliBoyutId = radioGroupBoyut.checkedRadioButtonId
                val seciliKivamId = radioGroupKivam.checkedRadioButtonId

                if (seciliBoyutId == -1 || seciliKivamId == -1) {
                    Toast.makeText(this, "Lütfen yumurta boyutunu ve pişme derecesini seçin!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Süreyi al
                val sureMillis = sureler[seciliBoyutId]?.get(seciliKivamId)?.toLong()

                if (sureMillis != null && sureMillis > 0) {
                    totalTimeInMillis = sureMillis
                    startTimer(totalTimeInMillis)
                } else {
                    Toast.makeText(this, "Seçim için uygun süre bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        updateTimerText() // Başlangıçta 00:00 göster
        updateProgressBar() // Başlangıçta progress bar'ı sıfırla
    }

    private fun startTimer(durationMillis: Long) {
        if (timerRunning) {
            countDownTimer?.cancel()
        }
        timeLeftInMillis = durationMillis
        totalTimeInMillis = durationMillis // ProgressBar için toplam süreyi sakla

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) { // Her 1 saniyede bir
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
                updateProgressBar()
            }

            override fun onFinish() {
                timerRunning = false
                timeLeftInMillis = 0 // Süre bittiğinde tam 0 göster
                updateTimerText()
                updateProgressBar() // ProgressBar'ı tamamla
                playSound()
                updateUIState()
            }
        }.start()

        timerRunning = true
        updateUIState()
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        // timeLeftInMillis = 0 // Durdurulduğunda sıfırlamak yerine kaldığı yerden devam etmesi için yorum satırı
        updateTimerText()
        updateProgressBar() // Durdurulduğunda progress bar'ı da mevcut durumda bırak
        updateUIState()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun updateTimerText() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftInMillis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        textViewTimerDisplay.text = timeLeftFormatted
    }

    private fun updateProgressBar() {
        if (totalTimeInMillis > 0) {
            val progress = ((totalTimeInMillis - timeLeftInMillis) * 100 / totalTimeInMillis).toInt()
            progressBarTimer.progress = progress
        } else {
            progressBarTimer.progress = 0 // Zamanlayıcı çalışmıyorsa veya süre 0 ise
        }
        if (timeLeftInMillis == 0L && timerRunning == false && totalTimeInMillis > 0) { // Süre bittiğinde tamamlama
            progressBarTimer.progress = 100
        }
    }


    private fun updateUIState() {
        if (timerRunning) {
            buttonStart.text = "Durdur"
            // Zamanlayıcı çalışırken seçimleri devre dışı bırak
            radioGroupBoyut.isEnabled = false
            for (i in 0 until radioGroupBoyut.childCount) {
                radioGroupBoyut.getChildAt(i).isEnabled = false
            }
            radioGroupKivam.isEnabled = false
            for (i in 0 until radioGroupKivam.childCount) {
                radioGroupKivam.getChildAt(i).isEnabled = false
            }
        } else {
            buttonStart.text = "Başlat"
            // Zamanlayıcı durduğunda seçimleri etkinleştir
            radioGroupBoyut.isEnabled = true
            for (i in 0 until radioGroupBoyut.childCount) {
                radioGroupBoyut.getChildAt(i).isEnabled = true
            }
            radioGroupKivam.isEnabled = true
            for (i in 0 until radioGroupKivam.childCount) {
                radioGroupKivam.getChildAt(i).isEnabled = true
            }
            if (timeLeftInMillis == 0L && totalTimeInMillis > 0) { // Süre bittiyse ve sıfırlanmadıysa
                buttonStart.text = "Sıfırla" // Ya da tekrar "Başlat"
                // Sıfırlama sonrası için progressBarTimer.progress = 0; totalTimeInMillis = 0; yapılabilir.
                // Şimdilik sadece metni değiştiriyoruz, tekrar başlat'a basınca yeni seçimle başlar.
            }
        }
    }

    private fun playSound() {
        try {
            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, notificationSoundUri)
            mediaPlayer?.isLooping = false // Sesi bir kere çal
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Uyarı sesi çalınırken hata oluştu.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
