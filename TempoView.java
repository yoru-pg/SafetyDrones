package com.example.safetydrones.Tempo;

import java.util.List;

public interface TempoView {
    void atualizarTemperatura(double temp, double windspeed, String conditions, String condicao);
    void atualizarData(List<TempoResponde.Day> previsaoSemana);
    void mostrarErro(String mensagem);
}
