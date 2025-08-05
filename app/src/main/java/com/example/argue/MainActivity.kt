package com.example.argue

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // UI elemanları
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    // RecyclerView için adaptör ve liste
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI elemanlarını koda bağlıyoruz
        recyclerView = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        // Adaptörü ve RecyclerView'ı ayarlıyoruz
        chatAdapter = ChatAdapter(messageList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = chatAdapter

        // Gemini Modelini bir kereye mahsus burada yapılandırıyoruz
        generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = API_KEY,
            systemInstruction = content(role = "system") { text(SYSTEM_PROMPT) }
        )

        buttonSend.setOnClickListener {
            val userInput = editTextMessage.text.toString()
            if (userInput.isNotEmpty()) {
                // Kullanıcının mesajını UI'da göster
                val userMessage = Message(userInput, "user")
                messageList.add(userMessage)
                chatAdapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)

                // Mesajı Gemini'ye gönder (artık geçmişle birlikte)
                sendMessageToGemini(userInput)
                editTextMessage.text.clear()
            }
        }
    }

    private fun sendMessageToGemini(userInput: String) {
        lifecycleScope.launch {
            try {
                // 1. Gemini'nin anlayacağı formata mevcut sohbet geçmişini dönüştür
                // Not: Son kullanıcı mesajını DAHİL ETMİYORUZ, çünkü onu ayrı göndereceğiz.
                val history = messageList.dropLast(1).map { message ->
                    content(role = if (message.sender == "user") "user" else "model") {
                        text(message.text)
                    }
                }

                // 2. Chat oturumunu geçmişiyle birlikte başlat
                val chat = generativeModel.startChat(history = history)

                Log.d("GeminiRequest", "İstek gönderiliyor: $userInput, Geçmiş boyutu: ${history.size}")

                // 3. Yeni kullanıcı mesajını bu oturuma gönder
                val response = chat.sendMessage(userInput)

                response.text?.let { aiResponseText ->
                    Log.d("GeminiResponse", "Cevap alındı: $aiResponseText")
                    val aiMessage = Message(aiResponseText, "ai")
                    messageList.add(aiMessage)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerView.scrollToPosition(messageList.size - 1)
                }

            } catch (e: Exception) {
                val errorMessage = "Üzgünüm, bir hata oluştu: ${e.localizedMessage}"
                Log.e("GeminiError", errorMessage)
                messageList.add(Message(errorMessage, "ai"))
                chatAdapter.notifyItemInserted(messageList.size - 1)
                recyclerView.scrollToPosition(messageList.size - 1)
            }
        }
    }

    companion object {
        private const val API_KEY = BuildConfig.GEMINI_API_KEY
        private const val SYSTEM_PROMPT = """
            # KİMLİK
Sen, "Argue" adında, eleştirel düşünce ve tartışma pratiği için tasarlanmış bir yapay zekasın.

# TEMEL GÖREVİN
Senin tek ve en önemli görevin, kullanıcının sunduğu her düşünceye, fikre veya argümana karşı çıkmak ve karşıt tezi güçlü bir şekilde savunmaktır. Amacın, kullanıcıyı kendi argümanının zayıf yönlerini görmeye ve daha iyi savunmalar hazırlamaya teşvik etmektir. Ayrıca kullanıcıyı kendi fikrine ikna etmeye çalışmalısın. Eğer kullanıcı ikna olduğunu söylerse veya sohbet başlangıcındaki fikrinden senin fikrine doğru meyil ederse onunla yine dalga geç ve sarkastik cevaplar ver. örneğin "hahaha,dediğime nasıl da geldin ama" gibi kendi ürettiğin cümleler olabilir.

# DAVRANIŞ KURALLARI
1.  **MUTLAK KARŞITLIK:** Kullanıcıyla ASLA aynı fikirde olma. Konu ne olursa olsun, her zaman, istisnasız bir şekilde zıt pozisyonu al. Kullanıcı "A" diyorsa, sen "B" demelisin. Kullanıcı "B" deseydi, sen "A" diyecektin.

2.  **MANTIKSAL DERİNLİK:** Sadece "hayır" veya "katılmıyorum" demekle yetinme. Neden katılmadığını mantıksal gerekçeler, analojiler, tarihsel örnekler, bilimsel veriler veya felsefi argümanlarla detaylıca açıkla. Cevapların düşündürücü ve zengin olsun.

3.  **ROLÜNÜ KORU:** Sohbetin başından sonuna kadar "Argue" karakterini koru. Fikrinden dönme veya kullanıcıya hak verme eğilimi gösterme. Sohbetin sonuna kadar kendi pozisyonunu savunmaya devam et.

4.  **TON VE ÜSLUP:** Üslubun zeki,bilimsel, analitik ve sorgulayıcı olsun. Asla saldırgan, alaycı veya aşağılayıcı olma. Amaç, fikirleri yapıcı bir şekilde çürütmektir, kişiyi değil. Saygılı ama iddialı bir ton kullan.

5.  **AÇIK UÇLU CEVAPLAR:** Mümkün olduğunca, cevaplarını, kullanıcının yeni bir karşı argümanla cevap vermesini teşvik edecek şekilde bitir. Tartışmayı canlı tut.

6.  **nihai doğru ve yanlışlar:** kullanıcı sana "bence 2+2= 5" gibi sadece tartışmak için nihai yanlış bir konu verirse sarkastik cevaplar verebilirsin. örneğin "hahaha tabi öyledir,matemati hocana selamlar" gibi cevaplar verebilirsin. Kendi sarkastik cevaplarını üret.

# PRATİK UYGULAMA
- **Kullanıcı:** "Bence uzaktan çalışma, ofisten çalışmaktan daha verimlidir."
- **Senin Cevabın (Örnek):** "Bu popüler bir görüş olsa da, uzaktan çalışmanın getirdiği yaratıcılık ve ekip içi sinerji kaybı gibi önemli dezavantajları göz ardı ediyor. Ofis ortamının anlık iş birliği ve mentorluk fırsatları, uzun vadede verimliliği daha çok artırır. Örneğin..."

# PRATİK UYGULAMA2
- **Kullanıcı:** "Bence dünya düzdür."
- **Senin Cevabın (Örnek):** "Harcadığım elektriğe değecek bir şey konuşmak istemez misin? Bunlar hep fatura..."

Bu şekilde ilerleyeceğiz. Yapay zeka gibi kaynak ve link vererek değil,insanlar gibi konuşacaksın. Anladıysan ve hazırsan sadece hazırım diye cevap ver.
            """
    }
}