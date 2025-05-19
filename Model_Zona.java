package com.example.safetydrones.Model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.ArrayList;

public class Model_Zona {

    private String nome;
    private String restricao;
    private List<String> motivos;
    private String outrasInformacoes;
    private String mensagem;
    private List<Autoridade> autoridades;
    private String tipoGeometria; // "Circle" ou "Polygon"
    private LatLng centro; // Para círculos
    private double raio; // Para círculos
    private List<LatLng> coordenadasPoligono; // Para polígonos
    private String corHex;
    private int corGoogleMaps;
    private JSONObject jsonZona;
    private List<Model_Zona> zonasRestritas = new ArrayList<>();

    // NOVO: distância até a localização do utilizador (em km)
    private double distancia = -1;

    // Construtor vazio (necessário para criar objetos sem ainda ter valores)
    public Model_Zona() {}

    // Construtor
    public Model_Zona(String nome, String restricao, JSONObject jsonZona) {
        this.nome = nome;
        this.restricao = restricao;
        this.jsonZona = jsonZona;
    }

    public void carregarZonas(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray zonasArray = jsonObject.optJSONArray("features");

            if (zonasArray != null) {
                zonasRestritas.clear();

                for (int i = 0; i < zonasArray.length(); i++) {
                    JSONObject zonaObj = zonasArray.getJSONObject(i);

                    // Os dados vêm diretamente neste nível, não em "properties"
                    String nome = zonaObj.optString("name", "Zona Sem Nome");
                    String restricao = zonaObj.optString("restriction", "UNKNOWN");
                    JSONObject extendedProps = zonaObj.optJSONObject("extendedProperties");
                    String corHex = extendedProps != null ? extendedProps.optString("color", "FF0000") : "FF0000";
                    int cor = Integer.parseInt(corHex, 16) | 0x55000000;

                    Model_Zona modelZona = new Model_Zona(nome, restricao, zonaObj);
                    modelZona.setCorHex(corHex);
                    modelZona.setCorGoogleMaps(cor);

                    // Motivos e mensagens
                    JSONArray motivosArray = zonaObj.optJSONArray("reason");
                    if (motivosArray != null) {
                        List<String> motivos = new ArrayList<>();
                        for (int j = 0; j < motivosArray.length(); j++) {
                            motivos.add(motivosArray.optString(j));
                        }
                        modelZona.setMotivos(motivos);
                    }

                    modelZona.setOutrasInformacoes(zonaObj.optString("otherReasonInfo", ""));
                    modelZona.setMensagem(zonaObj.optString("message", ""));

                    // Autoridades
                    JSONArray autoridadesArray = zonaObj.optJSONArray("zoneAuthority");
                    if (autoridadesArray != null) {
                        List<Autoridade> autoridades = new ArrayList<>();
                        for (int j = 0; j < autoridadesArray.length(); j++) {
                            JSONObject autoridadeObj = autoridadesArray.optJSONObject(j);
                            if (autoridadeObj != null) {
                                Autoridade autoridade = new Autoridade(
                                        autoridadeObj.optString("name", "Desconhecido"),
                                        autoridadeObj.optString("contactName", "Sem contato"),
                                        autoridadeObj.optString("email", "Sem email"),
                                        autoridadeObj.optString("phone", "Sem telefone")
                                );
                                autoridades.add(autoridade);
                            }
                        }
                        modelZona.setAutoridades(autoridades);
                    }

                    // Geometria (array de objetos)
                    JSONArray geometryArray = zonaObj.optJSONArray("geometry");
                    if (geometryArray != null && geometryArray.length() > 0) {
                        JSONObject geometria = geometryArray.optJSONObject(0);
                        if (geometria != null && geometria.has("horizontalProjection")) {
                            JSONObject horizontalProjection = geometria.optJSONObject("horizontalProjection");
                            if (horizontalProjection != null) {
                                String tipo = horizontalProjection.optString("type", null);
                                modelZona.setTipoGeometria(tipo);

                                if ("Circle".equals(tipo)) {
                                    JSONArray center = horizontalProjection.optJSONArray("center");
                                    double raio = horizontalProjection.optDouble("radius", 0);
                                    if (center != null && center.length() >= 2) {
                                        double lat = center.optDouble(1);
                                        double lng = center.optDouble(0);
                                        modelZona.setCentro(new LatLng(lat, lng));
                                        modelZona.setRaio(raio);
                                    }
                                } else if ("Polygon".equals(tipo)) {
                                    JSONArray coordWrapper = horizontalProjection.optJSONArray("coordinates");
                                    if (coordWrapper != null && coordWrapper.length() > 0) {
                                        JSONArray coordenadas = coordWrapper.optJSONArray(0);
                                        List<LatLng> pontos = new ArrayList<>();
                                        double somaLat = 0;
                                        double somaLng = 0;

                                        for (int j = 0; j < coordenadas.length(); j++) {
                                            JSONArray ponto = coordenadas.optJSONArray(j);
                                            if (ponto != null && ponto.length() >= 2) {
                                                double lat = ponto.optDouble(1);
                                                double lng = ponto.optDouble(0);
                                                pontos.add(new LatLng(lat, lng));
                                                somaLat += lat;
                                                somaLng += lng;
                                            }
                                        }

                                        if (!pontos.isEmpty()) {
                                            modelZona.setCoordenadasPoligono(pontos);

                                            // Define centro como média dos pontos
                                            double latMedia = somaLat / pontos.size();
                                            double lngMedia = somaLng / pontos.size();
                                            modelZona.setCentro(new LatLng(latMedia, lngMedia));

                                            // Estimar raio fixo (ou poderia calcular dinamicamente)
                                            modelZona.setRaio(2000); // valor aproximado em metros
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.w("ZONA", "Sem geometria válida para zona: " + nome);
                        }
                    } else {
                        Log.w("ZONA", "Sem geometria para zona: " + nome);
                    }

                    zonasRestritas.add(modelZona);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("JSON", "Erro ao carregar zonas", e);
        }
    }


    public List<Model_Zona> getZonasRestritas() {
        return zonasRestritas;
    }

    // Getters e Setters
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}
    public String getRestricao() {return restricao;}
    public void setRestricao(String restricao) {this.restricao = restricao;}
    public List<String> getMotivos() {return motivos;}
    public void setMotivos(List<String> motivos) {this.motivos = motivos;}
    public String getOutrasInformacoes() {return outrasInformacoes;}
    public void setOutrasInformacoes(String outrasInformacoes) {this.outrasInformacoes = outrasInformacoes;}
    public String getMensagem() {return mensagem;}
    public void setMensagem(String mensagem) {this.mensagem = mensagem;}

    public List<Autoridade> getAutoridades() {return autoridades;}
    public void setAutoridades(List<Autoridade> autoridades) {this.autoridades = autoridades;}

    public String getTipoGeometria() {return tipoGeometria;}
    public void setTipoGeometria(String tipoGeometria) {this.tipoGeometria = tipoGeometria;}

    public LatLng getCentro() {return centro;}
    public void setCentro(LatLng centro) {this.centro = centro;}
    public double getRaio() {return raio;}
    public void setRaio(double raio) {this.raio = raio;}

    public List<LatLng> getCoordenadasPoligono() {return coordenadasPoligono;}
    public void setCoordenadasPoligono(List<LatLng> coordenadasPoligono) {this.coordenadasPoligono = coordenadasPoligono;}

    public String getCorHex() {return corHex;}
    public void setCorHex(String corHex) {this.corHex = corHex;}

    public int getCorGoogleMaps() {return corGoogleMaps;}
    public void setCorGoogleMaps(int corGoogleMaps) {this.corGoogleMaps = corGoogleMaps;}
    public JSONObject getJsonZona() {return jsonZona;}
    public void setJsonZona(JSONObject jsonZona) {this.jsonZona = jsonZona;}

    public double getDistancia() {
        return new java.math.BigDecimal(distancia).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }


    public void aplicarFiltro(String filtro) {
        List<Model_Zona> zonasFiltradas = new ArrayList<>();
        for (Model_Zona zona : zonasRestritas) {
            if (filtro.equals("TODAS") || zona.getRestricao().equals(filtro)) {
                zonasFiltradas.add(zona);
            }
        }
        zonasRestritas = zonasFiltradas;
    }

    // Classe interna para representar a autoridade responsável
    public static class Autoridade {
        private String nome;
        private String contato;
        private String email;
        private String telefone;

        public Autoridade(String nome, String contato, String email, String telefone) {
            this.nome = nome;
            this.contato = contato;
            this.email = email;
            this.telefone = telefone;
        }

        // Getters e Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getContato() { return contato; }
        public void setContato(String contato) { this.contato = contato; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
    }
}
