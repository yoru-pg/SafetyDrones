package com.example.safetydrones.Tempo;
import com.example.safetydrones.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.util.List;


public class PrevisaoAdapter extends RecyclerView.Adapter<PrevisaoAdapter.PrevisaoViewHolder> {

    private List<TempoResponde.Day> listaDias;

    public PrevisaoAdapter(List<TempoResponde.Day> listaDias) {
        this.listaDias = listaDias;
    }

    @NonNull
    @Override
    public PrevisaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_previsao, parent, false);
        return new PrevisaoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PrevisaoViewHolder holder, int position) {
        TempoResponde.Day dia = listaDias.get(position);
        holder.dateTextView.setText(formatarData(dia.datetime));
        holder.tempTextView.setText("Temperatura: " + dia.temp + "°C");
        holder.conditionTextView.setText("Condições: " + TraduzTempo.traduzir(dia.conditions));
        holder.windTextView.setText("Vento: " + dia.windspeed + " km/h");

        String iconName = TraduzTempo.getIconFileName(dia.conditions, true);

        // Construir a URL com base no nome do ícone
        String iconUrl = "https://raw.githubusercontent.com/basmilius/weather-icons/dev/production/fill/png/64/" + iconName + ".png";
        // Carregar o ícone com Glide
        Glide.with(holder.itemView.getContext())
                .load(iconUrl)
                .into(holder.iconImageView);
    }

    @Override
    public int getItemCount() {
        return listaDias.size();
    }

    private String formatarData(String dataOriginal) {
        try {
            // Formato de data para analisar sem considerar horas
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date data = formatoOriginal.parse(dataOriginal);

            // Obter a data de hoje e amanhã
            Date hoje = new Date();
            Date amanha = new Date(hoje.getTime() + 86400000L); // +1 dia em milissegundos

            SimpleDateFormat soData = new SimpleDateFormat("yyyyMMdd", new Locale("pt", "PT")); // Comparação sem horas

            // Formatar as datas para "yyyyMMdd" para comparação
            String dataFormatada = soData.format(data);
            String hojeFormatado = soData.format(hoje);
            String amanhaFormatado = soData.format(amanha);

            // A comparação agora é feita somente pelo dia
            if (dataFormatada.equals(hojeFormatado)) {
                return "Hoje, dia " + new SimpleDateFormat("dd", new Locale("pt", "PT")).format(data) + ", " + new SimpleDateFormat("EEEE", new Locale("pt", "PT")).format(data);
            } else if (dataFormatada.equals(amanhaFormatado)) {
                return "Amanhã, dia " + new SimpleDateFormat("dd", new Locale("pt", "PT")).format(data) + ", " + new SimpleDateFormat("EEEE", new Locale("pt", "PT")).format(data);
            } else {
                // Caso contrário, mostra data normal
                SimpleDateFormat formatoNovo = new SimpleDateFormat("dd/MM/yyyy, EEEE", new Locale("pt", "PT"));
                return formatoNovo.format(data);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return dataOriginal;
        }
    }


    static class PrevisaoViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, tempTextView, conditionTextView, windTextView;
        ImageView iconImageView;


        public PrevisaoViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            tempTextView = itemView.findViewById(R.id.tempTextView);
            conditionTextView = itemView.findViewById(R.id.conditionTextView);
            windTextView = itemView.findViewById(R.id.windTextView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
        }
    }
}
