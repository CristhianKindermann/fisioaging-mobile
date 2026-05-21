package com.example.fisioaging.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RelatorioTesteResponse(
    @SerializedName("total_repeticoes") val totalRepeticoes: Int? = null,
    @SerializedName("repeticoes_completas") val repeticoesCompletas: Int? = null,
    @SerializedName("percentual_completas") val percentualCompletas: Double? = null,
    @SerializedName("altura_media") val alturaMedia: Double? = null,
    @SerializedName("cadencia") val cadencia: Double? = null,
    @SerializedName("amplitude_maxima_oscilacao") val amplitudeMaximaOscilacao: Double? = null,
    @SerializedName("tempo_total_execucao") val tempoTotalExecucao: Double? = null,
    @SerializedName("desvio_padrao_aceleracoes") val desvioPadraoAceleracoes: Double? = null,
    @SerializedName("velocidade_media_oscilacao") val velocidadeMediaOscilacao: Double? = null,
    @SerializedName("indice_estabilidade") val indiceEstabilidade: Double? = null,
    @SerializedName("classificacao") val classificacao: String? = null,
    @SerializedName("status") val status: String? = null
) : Serializable
