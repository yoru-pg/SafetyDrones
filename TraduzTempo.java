package com.example.safetydrones.Tempo;
import java.util.HashMap;
import java.util.Map;

public class TraduzTempo {

    private static final Map<String, String> traducoes = new HashMap<>();

    static {
        traducoes.put("clear", "Céu limpo");
        traducoes.put("clear sky", "Céu limpo");
        traducoes.put("partially cloudy", "Parcialmente nublado");
        traducoes.put("partly cloudy", "Parcialmente nublado");
        traducoes.put("overcast", "Nublado");
        traducoes.put("cloudy", "Encoberto");
        traducoes.put("rain", "Chuva");
        traducoes.put("rainy", "Chuva");
        traducoes.put("light rain", "Chuva fraca");
        traducoes.put("moderate rain", "Chuva moderada");
        traducoes.put("heavy rain", "Chuva forte");
        traducoes.put("showers", "Aguaceiros");
        traducoes.put("snow", "Neve");
        traducoes.put("fog", "Nevoeiro");
        traducoes.put("thunderstorm", "Trovoada");
        traducoes.put("windy", "Vento forte");
        traducoes.put("drizzle", "Chuvisco");
        traducoes.put("hail", "Granizo");
        traducoes.put("mist", "Neblina");
    }

    public static String traduzir(String condicoes) {
        String[] partes = condicoes.toLowerCase().split(",\\s*");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < partes.length; i++) {
            String traducao = traducoes.getOrDefault(partes[i], partes[i]);
            resultado.append(Character.toUpperCase(traducao.charAt(0)))
                    .append(traducao.substring(1));

            if (i < partes.length - 1) {
                resultado.append(", ");
            }
        }

        return resultado.toString();
    }

    public static String getIconFileName(String condicaoOriginal, boolean isDay) {
        if (condicaoOriginal == null || condicaoOriginal.isEmpty()) return "unknown";

        String[] partes = condicaoOriginal.toLowerCase().split(",\\s*");
        boolean hasRain = false, hasThunder = false, hasCloud = false, hasClear = false, hasOvercast = false;
        boolean hasDrizzle = false, hasSnow = false, hasFog = false, hasMist = false, hasPCloud = false;
        boolean hasHail = false, hasWind = false, hasLowRain = false, hasHighRain = false;


        for (String parte : partes) {
           if (parte.contains("thunderstorm")) hasThunder = true;
           if (parte.contains("rain") || parte.contains("rainy")) hasRain = true;
           if (parte.contains("drizzle")) hasDrizzle = true;
           if (parte.contains("light rain") || parte.contains("showers")) hasLowRain = true;
           if (parte.contains("moderate rain") || parte.contains("heavy rain")) hasHighRain = true;
           if (parte.contains("snow")) hasSnow = true;
           if (parte.contains("hail")) hasHail = true;
           if (parte.contains("fog")) hasFog = true;
           if (parte.contains("mist")) hasMist = true;
           if (parte.contains("overcast")) hasOvercast = true;
           if (parte.contains("cloudy")) hasCloud = true;
           if (parte.contains("partially cloudy") || parte.contains("partly cloudy")) hasPCloud = true;
           if (parte.contains("clear") || parte.contains("clear sky")) hasClear = true;
           if (parte.contains("wind")) hasWind = true;
        }

        String prefix = isDay ? "day" : "night";

        if(hasThunder && hasOvercast && hasRain) return "thunderstorms-" + prefix + "-extreme-rain";
        if(hasThunder && hasOvercast && hasSnow) return "thunderstorms-" + prefix + "-extreme-snow";
        if(hasThunder && hasOvercast) return "thunderstorms-" + prefix + "-extreme";

        if(hasThunder && hasCloud && hasRain) return "thunderstorms-" + prefix + "-overcast-rain";
        if(hasThunder && hasCloud && hasSnow) return "thunderstorms-" + prefix + "-overcast-snow";
        if(hasThunder && hasCloud) return "thunderstorms-" + prefix + "-overcast";
        if(hasThunder && hasRain) return "thunderstorms-" + prefix + "-rain";
        if(hasThunder && hasSnow) return "thunderstorms-" + prefix + "-snow";


        if (hasOvercast && hasDrizzle) return "extreme-" + prefix + "-drizzle";
        if (hasOvercast && hasFog) return "extreme-" + prefix + "-fog";
        if (hasOvercast && hasHail) return "extreme-" + prefix + "-hail";
        if (hasOvercast && hasRain) return "extreme-" + prefix + "-rain";
        if (hasOvercast && hasSnow) return "extreme-" + prefix + "-snow";

        if (hasCloud && hasDrizzle) return "overcast-" + prefix + "-drizzle";
        if (hasCloud && hasFog) return "overcast-" + prefix + "-fog";
        if (hasCloud && hasHail) return "overcast-" + prefix + "-hail";
        if (hasCloud && hasRain) return "overcast-" + prefix + "-rain";
        if (hasCloud && hasSnow) return "overcast-" + prefix + "-snow";

        if(hasPCloud && hasDrizzle) return "partly-cloudy-" + prefix + "-drizzle";
        if(hasPCloud && hasFog) return "partly-cloudy-" + prefix + "-fog";
        if(hasPCloud && hasHail) return "partly-cloudy-" + prefix + "-hail";
        if(hasPCloud && hasRain) return "partly-cloudy-" + prefix + "-rain";
        if(hasPCloud && hasSnow) return "partly-cloudy-" + prefix + "-snow";

        if (hasOvercast) return "extreme-" + prefix;
        if (hasPCloud) return "partly-cloudy-" + prefix;
        if (hasFog) return "fog-" + prefix;
        if (hasClear) return "clear-" + prefix;
        if (hasThunder) return "thunderstorms-" + prefix;
        if (hasSnow) return "snow";
        if (hasHail) return "hail";
        if (hasMist) return "mist";
        if (hasDrizzle) return "drizzle";
        if (hasRain) return "rain";
        if (hasLowRain) return "drizzle";
        if (hasHighRain) return "extreme-rain";
        if (hasWind) return "wind-alert";
        if (hasCloud) return "cloudy";

        return "not-available";
    }
}
