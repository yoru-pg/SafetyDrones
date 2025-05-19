package com.example.safetydrones.Drones;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safetydrones.R;
import com.example.safetydrones.Model.Model_Drone;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InfoDrone extends AppCompatActivity {

    private Model_Drone drone;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_infodrones);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Drones");

        TextView droneNome = findViewById(R.id.drone_nome);
        TextView droneModelo = findViewById(R.id.drone_modelo);
        TextView dronePeso = findViewById(R.id.drone_peso);
        TextView droneAutonomia = findViewById(R.id.drone_autonomia);
        TextView droneVelocidade = findViewById(R.id.drone_velocidade);
        TextView droneCategoria = findViewById(R.id.drone_categoria);
        TextView txtDetalhes = findViewById(R.id.txt_detalhes);

        Button btnEliminar = findViewById(R.id.btn_eliminar);
        Button btnEditar = findViewById(R.id.btn_editar);
        ImageButton btnVoltar = findViewById(R.id.btn_voltar);
        ImageView categoriaimg = findViewById(R.id.categoria_img);

        Intent intent = getIntent();
        if (intent != null) {
            String nome = intent.getStringExtra("nome");
            String modelo = intent.getStringExtra("modelo");
            String peso = intent.getStringExtra("peso");
            String autonomia = intent.getStringExtra("autonomia");
            String velocidade = intent.getStringExtra("velocidade");
            String droneId = intent.getStringExtra("id");

            // Criação do objeto Model_Drone (Model)
            drone = new Model_Drone(nome, modelo,
                    Double.parseDouble(peso), Integer.parseInt(autonomia),
                    Double.parseDouble(velocidade));
            drone.setId(droneId);

            // Atualizar a interface com os dados do Drone
            droneNome.setText(String.format("Nome: %s", drone.getNome()));
            droneModelo.setText(String.format("Modelo: %s", drone.getModelo()));
            dronePeso.setText(String.format("Peso: %.2f Kg", drone.getPeso()));
            droneAutonomia.setText(String.format("Autonomia: %d minutos", drone.getAutonomia()));
            droneVelocidade.setText(String.format("Velocidade: %.2f m/s", drone.getVelocidade()));
            droneCategoria.setText(String.format("Categoria: %s", drone.getCategoria()));

            // Ação ao voltar
            btnVoltar.setOnClickListener(v -> finish());

            // Ação de eliminar
            btnEliminar.setOnClickListener(v -> eliminarDrone());

            // Ação de editar
            btnEditar.setOnClickListener(v -> {
                Intent editIntent = new Intent(InfoDrone.this, EditDrone.class);
                editIntent.putExtra("droneId", drone.getId());
                editIntent.putExtra("nome", drone.getNome());
                editIntent.putExtra("modelo", drone.getModelo());
                editIntent.putExtra("peso", String.valueOf(drone.getPeso()));
                editIntent.putExtra("autonomia", String.valueOf(drone.getAutonomia()));
                editIntent.putExtra("velocidade", String.valueOf(drone.getVelocidade()));
                startActivityForResult(editIntent, 1);
            });

            // Atualizando o texto de categoria
            atualizarDetalhesCategoria(drone.getCategoria(), categoriaimg, txtDetalhes);
        } else {
            Toast.makeText(this, "Erro ao carregar informações do drone.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void atualizarDetalhesCategoria(String categoria, ImageView categoriaimg, TextView txtDetalhes) {
        String detalhesCategoria = "Subcategoria de Operação Aberta A1\n C0 e C1\n" +
                "EQUIPAMENTO:\n" +
                "\n" +
                "SEM MARCAÇÃO DE CLASSE CE:\n" +
                "• Opera uma aeronave não tripulada de construção amadora (com menos 250g incluindo a carga) e com velocidade máxima inferior a 19 m/s, ou;\n" +
                "\n" +
                "• Transitoriamente, uma aeronave não tripulada com menos de 500g sem marcação de classe (aqueles drones que não tem marcação de classe europeia, mas são comercializados atualmente em escala por um fabricante no mercado único, até 31 de dezembro de 2023 desde que o piloto remoto esteja munido do certificado de competência relativo à prova de conclusão de formação e exame à distância A1/A3;\n" +
                "\n" +
                "COM MARCAÇÃO DE CLASSE CE:\n" +
                "• Opera um drone C0, ou com menos de 250g\n" +
                "\n" +
                "Sugestão de operação (A1): Operações de lazer";

        switch (categoria.trim().toUpperCase()) {
            case "C0":
                categoriaimg.setImageResource(R.drawable.categoria_c0);
                categoriaimg.setVisibility(VISIBLE);
                break;
            case "C1":
                categoriaimg.setImageResource(R.drawable.categoria_c1);
                categoriaimg.setVisibility(VISIBLE);
                break;
            case "C2":
                categoriaimg.setImageResource(R.drawable.categoria_c2);
                categoriaimg.setVisibility(VISIBLE);
                detalhesCategoria = "Subcategoria de Operação Aberta A2\nC2\n" +
                        "EQUIPAMENTO:\n" +
                        "SEM MARCAÇÃO DE CLASSE CE: Aeronaves não tripuladas com massa à descolagem inferior a 2kg (aqueles drones que não têm marcação de classe europeia, mas são comercializados atualmente em escala por um fabricante no mercado único, até 31 de dezembro de 2023), desde que seja operada a mais de 50m de pessoas e os pilotos remotos possuam um certificado de competência de piloto remoto A2;\n" +
                        "\n" +
                        "COM MARCAÇÃO DE CLASSE CE: Utilizar uma aeronave com marcação de conformidade de classe C2 e com:\n" +
                        "\n" +
                        "• Sistema de identificação remota direta (o operador remoto deve introduzir no sistema o número do registo do operador providenciado pela ANAC);\n" +
                        "\n" +
                        "• Função de reconhecimento geoespacial ativos e atualizados (o drone tem de ter um sistema para visualizar as zonas geográficas e que permita importar as zonas da ANAC referidas no Regulamento da ANAC n.º 1093/2016 e publicadas transitoriamente em www.anac.pt, https://uas.anac.pt/registry/explore (back-up: https://dnt.anac.pt/) para efeitos do artigo 15.º do Regulamento de Execução (UE) 2019/947).\n" +
                        "\n" +
                        "Sugestão de operação (A2): Operação de lazer, mas pode também ser utilizado por operadores comerciais";
                break;
            case "C3 & C4":
                categoriaimg.setImageResource(R.drawable.categoria_c4);
                categoriaimg.setVisibility(VISIBLE);
                detalhesCategoria = "Subcategoria de Operação Aberta A3\nC3 e C4\n" +
                        "EQUIPAMENTO: Apenas podem operar drones:\n" +
                        "\n" +
                        "SEM MARCAÇÃO DE CLASSE CE: Podem ser utilizados os de construção amadora (desenvolvidos em casa) e sem marcação de classe de conformidade europeia (atualmente no mercado e comercializados em grande escala por um fabricante, apenas até 31 de dezembro de 2023), com massa máxima à descolagem, incluindo qualquer carga (peso do drone mais a carga útil ou qualquer outro componente apenso), com menos de 25 Kg;\n" +
                        "\n" +
                        "COM MARCAÇÃO DE CLASSE CE: Aeronaves não tripuladas com marcação de conformidade de classe europeia C0 a C6 desde que contenham também:\n" +
                        "• Sistema de identificação remota direta (o operador remoto deve introduzir no sistema o número do registo do operador providenciado pela ANAC), caso esteja disponível no UAS com marcação de classe ou seja obrigatório pelas zonas geográficas.\n" +
                        "\n" +
                        "• Função de reconhecimento geoespacial ativos e atualizados (o drone tem de ter um sistema para visualizar as zonas geográficas e que permita importar as zonas da ANAC referidas no Regulamento da ANAC n.º 1093/2016 e publicadas transitoriamente em www.anac.pt, https://uas.anac.pt/registry/explore (back-up: https://dnt.anac.pt/) para efeitos do artigo 15.º do Regulamento de Execução (UE) 2019/947).\n" +
                        "\n" +
                        "Sugestão de operação (A3): Trabalho agrícola ou outros similares";
                break;
            default:
                categoriaimg.setImageResource(R.drawable.background_image);
                categoriaimg.setVisibility(VISIBLE);
                detalhesCategoria = "Categoria desconhecida. Verifique os dados do drone.";
                break;
        }

        txtDetalhes.setText(detalhesCategoria);

        Button btnVerVideo = findViewById(R.id.btn_ver_video);
        btnVerVideo.setOnClickListener(v -> {
            String videoUrl = getVideoUrl(categoria);
            if (videoUrl != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Sem vídeo disponível para esta categoria.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnVideoInfo = findViewById(R.id.btninfovideo);
        btnVideoInfo.setOnClickListener(v -> {
            String videoUrl = getVideoInfoUrl(categoria);
            if (videoUrl != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Sem vídeo de informação disponível para esta categoria.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getVideoUrl(String categoria) {
        switch (categoria) {
            case "C0":
            case "C1":
                return "https://www.youtube.com/watch?v=uPiTGmds0PY";
            case "C2":
            case "C3 & C4":
                return "https://www.youtube.com/watch?v=hRVekYCupAE";
            default:
                return null;
        }
    }

    private String getVideoInfoUrl(String categoria) {
        switch (categoria) {
            case "C0":
            case "C1":
                return "https://www.youtube.com/watch?v=LwPjPiKxsLc";
            case "C2":
                return "https://www.youtube.com/watch?v=d6khMVV3icM";
            case "C3 & C4":
                return "https://www.youtube.com/watch?v=Dn7Q4oIrP3I";
            default:
                return null;
        }
    }


    private void eliminarDrone() {
        drone.eliminarDrone(mDatabase, mAuth, new Model_Drone.FirebaseCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(InfoDrone.this, message, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(InfoDrone.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
