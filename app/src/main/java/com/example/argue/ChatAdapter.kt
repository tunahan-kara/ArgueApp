package com.example.argue

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // İki farklı görünüm türü için sabitler tanımlıyoruz
    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2

    // Bu sınıf, her bir satırın XML'deki elemanlarını (örn: TextView) tutar
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
    }

    // Bu fonksiyon, mesajın gönderenine göre hangi tasarımın kullanılacağını belirler
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sender == "user") {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    // Doğru XML tasarımını (layout) seçer ve ViewHolder oluşturur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = if (viewType == VIEW_TYPE_SENT) {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_message, parent, false)
        } else {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_message, parent, false)
        }
        return MessageViewHolder(view)
    }

    // Veriyi alır ve ViewHolder'daki görsel elemana (TextView) atar
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textViewMessage.text = message.text
    }

    // Listenin toplam boyutunu döndürür
    override fun getItemCount() = messages.size
}