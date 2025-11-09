package com.example.fisioaging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class TesteSalvo(val arquivo: File, var isSelected: Boolean = false)

class SincroniaAdapter(private val testes: MutableList<TesteSalvo>) :
    RecyclerView.Adapter<SincroniaAdapter.TesteViewHolder>() {

    class TesteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkbox_teste)
        val nomeArquivo: TextView = view.findViewById(R.id.text_nome_arquivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TesteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_teste_sincronia, parent, false)
        return TesteViewHolder(view)
    }

    override fun onBindViewHolder(holder: TesteViewHolder, position: Int) {
        val teste = testes[position]

        holder.nomeArquivo.text = formatarNomeArquivo(teste.arquivo.name)

        holder.checkBox.isChecked = teste.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            teste.isSelected = isChecked
        }
    }

    override fun getItemCount() = testes.size

    fun getItensSelecionados(): List<TesteSalvo> {
        return testes.filter { it.isSelected }
    }

    private fun formatarNomeArquivo(nomeArquivo: String): String {

        try {
            val parser = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm:ss", Locale.getDefault())
            val dataString = nomeArquivo.removePrefix("teste_").removeSuffix(".csv")
            val data = parser.parse(dataString)
            return "Teste de ${formatter.format(data)}"
        } catch (e: Exception) {
            return nomeArquivo
        }
    }
}