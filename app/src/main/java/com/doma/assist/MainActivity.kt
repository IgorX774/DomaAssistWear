package com.doma.assist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var audioManager: AudioManager
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private val requestVoiceCommand = 2816

    private val audioCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            updateAudioStatus("Saída de áudio conectada.")
        }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            updateAudioStatus("Saída de áudio removida.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        tts = TextToSpeech(this, this)
        buildInterface()
        audioManager.registerAudioDeviceCallback(audioCallback, null)
        updateAudioStatus()
    }

    private fun buildInterface() {
        val scroll = ScrollView(this).apply { setBackgroundColor(0xFF101820.toInt()) }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }
        val title = TextView(this).apply {
            text = "Doma Assist"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFB9C8D2.toInt())
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        }
        content.addView(title, matchWrap())
        content.addView(status, matchWrap())
        content.addView(actionButton("Verificar áudio", ::checkAudio))
        content.addView(actionButton("Conectar fone Bluetooth", ::openBluetoothSettings))
        content.addView(actionButton("Ouvir comando de voz", ::listenForCommand))
        content.addView(actionButton("Alerta de segurança", ::safetyAlert))

        val message = EditText(this).apply {
            hint = "Digite uma mensagem para ouvir"
            setHintTextColor(0xFF91A4B0.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setSingleLine(false)
            minLines = 2
            setPadding(dp(12), dp(8), dp(12), dp(8))
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF62D6C7.toInt())
        }
        content.addView(message, matchWrap())
        content.addView(actionButton("Ler mensagem", { speak(message.text.toString().ifBlank { "Não há mensagem para ler." }) }))
        scroll.addView(content)
        setContentView(scroll)
    }

    private fun actionButton(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(0xFFFFFFFF.toInt())
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF176B68.toInt()))
        setOnClickListener { action() }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, dp(5), 0, dp(5))
        layoutParams = params
    }

    private fun checkAudio() {
        val speaker = audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val bluetooth = audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        val message = when {
            speaker && bluetooth -> "Alto-falante e fone Bluetooth disponíveis."
            speaker -> "Alto-falante disponível. Fone Bluetooth não conectado."
            bluetooth -> "Fone Bluetooth conectado."
            else -> "Nenhuma saída de áudio detectada."
        }
        updateAudioStatus(message)
        speak(message)
    }

    private fun audioOutputAvailable(type: Int): Boolean {
        val hasOutputFeature = packageManager.hasSystemFeature("android.hardware.audio.output")
        return hasOutputFeature && audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any { it.type == type }
    }

    private fun updateAudioStatus(prefix: String? = null) {
        val speaker = audioOutputAvailable(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val bluetooth = audioOutputAvailable(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP)
        val current = when {
            speaker && bluetooth -> "Áudio: alto-falante e Bluetooth disponíveis"
            speaker -> "Áudio: alto-falante disponível"
            bluetooth -> "Áudio: Bluetooth conectado"
            else -> "Áudio: nenhuma saída compatível detectada"
        }
        status.text = if (prefix == null) current else "$prefix\n$current"
    }

    private fun openBluetoothSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("EXTRA_CONNECTION_ONLY", true)
                putExtra("EXTRA_CLOSE_ON_CONNECT", true)
                putExtra("android.bluetooth.devicepicker.extra.FILTER_TYPE", 1)
            }
            startActivity(intent)
        } catch (_: Exception) {
            speak("Não foi possível abrir as configurações Bluetooth neste dispositivo.")
        }
    }

    private fun listenForCommand() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("O reconhecimento de voz não está disponível neste dispositivo.")
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Diga: ler mensagem, verificar áudio ou alerta")
        }
        try {
            startActivityForResult(intent, requestVoiceCommand)
        } catch (_: Exception) {
            speak("Não foi possível iniciar o reconhecimento de voz.")
        }
    }

    @Deprecated("Deprecated by Android; retained for broad Wear OS compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != requestVoiceCommand || resultCode != RESULT_OK) return
        val command = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.lowercase(Locale.getDefault()) ?: return
        when {
            "áudio" in command || "audio" in command -> checkAudio()
            "alerta" in command || "emergência" in command || "emergencia" in command -> safetyAlert()
            "mensagem" in command -> speak("Abra o campo de mensagem e toque em Ler mensagem.")
            else -> speak("Comando não reconhecido. Diga verificar áudio ou alerta.")
        }
    }

    private fun safetyAlert() {
        val message = "Atenção. Alerta de segurança. Siga as orientações de emergência do local."
        status.text = "Alerta de segurança acionado."
        speak(message)
    }

    private fun speak(text: String) {
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "doma-${System.currentTimeMillis()}")
        else status.text = "Preparando áudio. Tente novamente em instantes."
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts.language = Locale("pt", "BR")
            ttsReady = true
        }
    }

    override fun onDestroy() {
        audioManager.unregisterAudioDeviceCallback(audioCallback)
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
