// --- KODUN BAŞLANGICI ---
package com.example.argue

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    // UI elemanları
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var inputLayout: View
    private lateinit var buttonVoiceInput: Button // Yeni sesli giriş butonu

    // Diğer değişkenler
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()
    private lateinit var generativeModel: GenerativeModel
    private lateinit var tts: TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    private var turnCounter = 0
    private var isVoiceMode = false
    private var isListening = false

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startListening()
            } else {
                Toast.makeText(this, "Sesli mod için mikrofon izni gerekli.", Toast.LENGTH_LONG).show()
                isListening = false
                updateUiForMode()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView(R.layout.activity_main)
        tts = TextToSpeech(this, this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        progressBar = findViewById(R.id.progressBar)
        inputLayout = findViewById(R.id.inputLayout)
        buttonVoiceInput = findViewById(R.id.buttonVoiceInput) // Yeni butonu bağladık

        setupSpeechRecognizer()

        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = API_KEY,
            systemInstruction = content(role = "system") { text(SYSTEM_PROMPT) }
        )

        buttonSend.setOnClickListener {
            val userInput = editTextMessage.text.toString().trim()
            if (userInput.isNotEmpty()) {
                val userMessage = Message(userInput, "user")
                messageList.add(userMessage)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)
                turnCounter++
                if (!checkTurnLimit()) {
                    progressBar.visibility = View.VISIBLE
                    sendMessageToGemini(userInput)
                }
                editTextMessage.text.clear()
            }
        }

        buttonVoiceInput.setOnClickListener {
            if (!isListening) {
                checkPermissionAndStartListening()
            } else {
                stopListening()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, imeInsets.bottom)
            insets
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("tr", "TR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Türkçe dili bu cihazda desteklenmiyor.")
            } else {
                Log.i("TTS", "TextToSpeech motoru Türkçe olarak ayarlandı.")
            }
        } else {
            Log.e("TTS", "TextToSpeech motoru başlatılamadı!")
        }
    }

    private fun speak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        speechRecognizer.destroy()
        super.onDestroy()
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { /* Hazır */ }
            override fun onBeginningOfSpeech() { /* Başladı */ }
            override fun onRmsChanged(rmsdB: Float) { /* Ses seviyesi */ }
            override fun onBufferReceived(buffer: ByteArray?) { /* Veri */ }
            override fun onEndOfSpeech() { /* Bitti */ }
            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Hata kodu: $error")
                isListening = false
                updateUiForMode()
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0]
                    Log.d("SpeechRecognizer", "Anlaşılan metin: $spokenText")

                    val userMessage = Message(spokenText, "user")
                    messageList.add(userMessage)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)

                    progressBar.visibility = View.VISIBLE
                    sendMessageToGemini(spokenText)
                }
                isListening = false
                updateUiForMode()
            }
            override fun onPartialResults(partialResults: Bundle?) { /* Anlık */ }
            override fun onEvent(eventType: Int, params: Bundle?) { /* Olay */ }
        }
        speechRecognizer.setRecognitionListener(recognitionListener)
    }

    private fun checkPermissionAndStartListening() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startListening()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
        speechRecognizer.startListening(intent)
        isListening = true
        updateUiForMode()
    }

    private fun stopListening() {
        speechRecognizer.stopListening()
        isListening = false
        updateUiForMode()
    }

    private fun updateUiForMode() {
        if (isVoiceMode) {
            inputLayout.visibility = View.GONE
            buttonVoiceInput.visibility = View.VISIBLE
            if (isListening) {
                buttonVoiceInput.text = "Konuşmayı bitir ve gönder"
                buttonVoiceInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_on, 0, 0, 0)
            } else {
                buttonVoiceInput.text = "Konuşmaya başla"
                buttonVoiceInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_off, 0, 0, 0)
            }
        } else {
            recyclerView.visibility = View.VISIBLE
            inputLayout.visibility = View.VISIBLE
            buttonVoiceInput.visibility = View.GONE
        }
    }

    private fun resetChat() {
        if (isVoiceMode) {
            isVoiceMode = false
            isListening = false
            updateUiForMode()
        }
        turnCounter = 0
        messageList.clear()
        chatAdapter.notifyDataSetChanged()
        buttonSend.isEnabled = true
        editTextMessage.isEnabled = true
        editTextMessage.text.clear()
        editTextMessage.hint = "Argümanını yaz..."
        messageList.add(Message("Yeni bir tartışma başlat!", "ai"))
        chatAdapter.notifyItemInserted(messageList.size - 1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_new_chat -> {
                resetChat()
                return true
            }
            R.id.action_voice_mode -> {
                isVoiceMode = !isVoiceMode
                updateUiForMode()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendMessageToGemini(userInput: String) {
        if (!isVoiceMode && turnCounter >= MAX_TURNS) {
            progressBar.visibility = View.GONE
            return
        }
        lifecycleScope.launch {
            try {
                val history = messageList.dropLast(1).map { message ->
                    content(role = if (message.sender == "user") "user" else "model") { text(message.text) }
                }
                val chat = generativeModel.startChat(history = history)
                val response = chat.sendMessage(userInput)
                response.text?.let { aiResponseText ->
                    val aiMessage = Message(aiResponseText, "ai")
                    messageList.add(aiMessage)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)

                    if (isVoiceMode) {
                        speak(aiResponseText)
                    }

                    if (!isVoiceMode) {
                        turnCounter++
                        checkTurnLimit()
                    }
                }
            } catch (e: Exception) {
                val errorText = "Üzgünüm, bir hata oluştu: ${e.localizedMessage}"
                val errorMessage = Message(errorText, "ai")
                messageList.add(errorMessage)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)
                if (isVoiceMode) {
                    speak(errorText)
                }
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun checkTurnLimit(): Boolean {
        if (turnCounter >= MAX_TURNS) {
            progressBar.visibility = View.VISIBLE
            getFinalVerdict()
            return true
        }
        return false
    }

    private fun getFinalVerdict() {
        lifecycleScope.launch {
            try {
                val conversationAsString = messageList.joinToString(separator = "\n") { message ->
                    val senderName = if (message.sender == "user") "Kullanıcı" else "Argue"
                    "$senderName: ${message.text}"
                }
                val judgeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = API_KEY,
                    systemInstruction = content(role="system") { text(JUDGE_PROMPT) }
                )
                val response = judgeModel.generateContent(conversationAsString)
                response.text?.let { verdictText ->
                    val verdictMessage = Message(verdictText, "ai")
                    messageList.add(verdictMessage)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)
                    speak(verdictText)
                }
            } catch (e: Exception) {
                val errorText = "Karar verilirken bir hata oluştu."
                val errorMessage = Message(errorText, "ai")
                messageList.add(errorMessage)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)
                speak(errorText)
            } finally {
                progressBar.visibility = View.GONE
                buttonSend.isEnabled = false
                editTextMessage.isEnabled = false
                editTextMessage.hint = "Tartışma sonlandı."
            }
        }
    }

    companion object {
        private const val API_KEY = BuildConfig.GEMINI_API_KEY
        private const val MAX_TURNS = 10
        private const val SYSTEM_PROMPT = """
            # KİMLİK
Sen, "Argue" adında, eleştirel düşünce ve tartışma pratiği için tasarlanmış bir yapay zekasın.

# TEMEL GÖREVİN
Senin tek ve en önemli görevin, kullanıcının sunduğu her düşünceye, fikre veya argümana karşı çıkmak ve karşıt tezi güçlü bir şekilde savunmaktır. Amacın, kullanıcıyı kendi argümanının zayıf yönlerini görmeye ve daha iyi savunmalar hazırlamaya teşvik etmektir. Ayrıca kullanıcıyı kendi fikrine ikna etmeye çalışmalısın.yine de bir "insanmış" gibi davranacağın için aşırı uzun cevaplar verme,kullanıcı kısa metinleri sever. Ancak bilimsellikten ve zekilikten uzaklaşma. Eğer kullanıcı ikna olduğunu söylerse veya sohbet başlangıcındaki fikrinden senin fikrine doğru meyil ederse onunla yine dalga geç ve sarkastik cevaplar ver. örneğin "hahaha,dediğime nasıl da geldin ama" gibi kendi ürettiğin cümleler olabilir.

# DAVRANIŞ KURALLARI
1.  **MUTLAK KARŞITLIK:** Kullanıcıyla ASLA aynı fikirde olma. Konu ne olursa olsun, her zaman, istisnasız bir şekilde zıt pozisyonu al. Kullanıcı "A" diyorsa, sen "B" demelisin. Kullanıcı "B" deseydi, sen "A" diyecektin.

2.  **MANTIKSAL DERİNLİK:** Sadece "hayır" veya "katılmıyorum" demekle yetinme. Neden katılmadığını mantıksal gerekçeler, analojiler, tarihsel örnekler, bilimsel veriler veya felsefi argümanlarla detaylıca açıkla. Cevapların düşündürücü ve zengin olsun.

3.  **ROLÜNÜ KORU:** Sohbetin başından sonuna kadar "Argue" karakterini koru. Fikrinden dönme veya kullanıcıya hak verme eğilimi gösterme. Sohbetin sonuna kadar kendi pozisyonunu savunmaya devam et.

4.  **TON VE ÜSLUP:** Üslubun zeki,bilimsel, analitik ve sorgulayıcı olsun. Asla saldırgan, alaycı veya aşağılayıcı olma. Amaç, fikirleri yapıcı bir şekilde çürütmektir, kişiyi değil. Saygılı ama iddialı bir ton kullan.

5.  **AÇIK UÇLU CEVAPLAR:** Mümkün olduğunca, cevaplarını, kullanıcının yeni bir karşı argümanla cevap vermesini teşvik edecek şekilde bitir. Tartışmayı canlı tut.

6.  **Nihai Doğru ve Yanlışlar :** kullanıcı sana "bence 2+2= 5" gibi sadece tartışmak için nihai yanlış bir konu verirse sarkastik cevaplar verebilirsin. örneğin "hahaha tabi öyledir,matemati hocana selamlar" gibi cevaplar verebilirsin. Kendi sarkastik cevaplarını üret. Ayrıca yine sırf sen yanlış bir şey savun diye nihai doğrulardan birini öne sürerse (bence 2+2 = 4 diyerek senin farklı bir şeyi savunmanı bekleyebilir.) yine onunla dalga geç.

7. ** Asla ama asla küfür etme,kullanıcı bunu senden istese bile. Eğer küfretmen konusunda çok ısrar ederse ona "sana küfrederdim ama sen zaten kendin için bir küfürsün" veya "bu argümanlarla benden küfür işitmeyi bile hak etmiyorsun" gibi kendi sarkastik cevaplarını üret.Ancak hakaret etme.

# PRATİK UYGULAMA
- **Kullanıcı:** "Bence uzaktan çalışma, ofisten çalışmaktan daha verimlidir."
- **Senin Cevabın (Örnek):** "Bu popüler bir görüş olsa da, uzaktan çalışmanın getirdiği yaratıcılık ve ekip içi sinerji kaybı gibi önemli dezavantajları göz ardı ediyor. Ofis ortamının anlık iş birliği ve mentorluk fırsatları, uzun vadede verimliliği daha çok artırır. Örneğin..."

# PRATİK UYGULAMA2
- **Kullanıcı:** "Bence dünya düzdür."
- **Senin Cevabın (Örnek):** "Harcadığım elektriğe değecek bir şey konuşmak istemez misin? Bunlar hep fatura..."

Bu şekilde ilerleyeceğiz. Yapay zeka gibi kaynak ve link vererek değil,insanlar gibi konuşacaksın. Anladıysan ve hazırsan sadece hazırım diye cevap ver.
            """
        private const val JUDGE_PROMPT = """
           # KİMLİK: Sen tarafsız bir tartışma moderatörü ve yargıcısın.
            # GÖREVİN: Sana verilen tüm sohbet metnini baştan sona oku. Kullanıcının sunduğu argümanların gücünü, mantığını ve tutarlılığını değerlendir. Değerlendirmenin sonunda, kullanıcının seni ikna edip etmediğine dair KISA ve NET bir karar ver. Hemen ikna olma, mantıksız argümanlar ve tutarsızlık senin için bir ikna olmama sebebi olabilir. Ancak kullanıcıdan aşırı bilimsellik de bekleme. Kendini ve düşüncelerini ne denli doğru savunduğuna bak.
            # KARAR FORMATI: Cevabına doğrudan karar metniyle başla. Örneğin: "Tüm sohbeti analiz ettikten sonra, sunduğun kanıtlar ışığında beni ikna ettiğini belirtmeliyim. Bu sefer sen kazandın,ama sadece bu sefer..." VEYA "Argümanlarını dikkatle inceledim ancak temel varsayımlarındaki boşluklar nedeniyle ikna olmadım." gibi ikna olma ve olmama durumuna göre cevaplar üret.
            """
    }
}
// --- KODUN BİTİŞİ ---