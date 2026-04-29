package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private Scanner scanner = new Scanner(System.in);

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=54aea7e6";

    private ConsumoApi consumo = new ConsumoApi();

    private ConverteDados conversor = new ConverteDados();

   public void exibeMenu(){
       System.out.println("Buscar série: ");
       var nomeSerie = scanner.nextLine();
       var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

       DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
       System.out.println(dados);

       List<DadosTemporada> temporadas = new ArrayList<>();

       for (int i = 1; i <= dados.totalTemporadas(); i++){
           json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
           DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
           temporadas.add(dadosTemporada);
       }
       temporadas.forEach(System.out::println);

       for(int i = 0; i < dados.totalTemporadas(); i++){
           List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
           for(int n = 0; n < episodiosTemporada.size(); n++){
               System.out.println("S" + (i+1) + " Ep." + (n+1) + ": " + episodiosTemporada.get(n).titulo());
           }
       }

       //temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

       List<DadosEpisodio> dadosEpisodios = temporadas.stream()
               .flatMap(t -> t.episodios().stream())
               .collect(Collectors.toList());

       System.out.println("\n5 melhores episódios: ");

       dadosEpisodios.stream()
               .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
               .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
               .limit(5)
               .forEach(System.out::println);

       List<Episodio> episodios = temporadas.stream()
               .flatMap(t -> t.episodios().stream()
                       .map(d -> new Episodio(t.numero(), d)))
               .collect(Collectors.toList());

       episodios.forEach(System.out::println);

       System.out.println("Busca episódio: ");
       var trechoTitulo = scanner.nextLine();

       Optional<Episodio> episodioBuscado = episodios.stream()
               .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
               .findFirst();

       if(episodioBuscado.isPresent()){
           System.out.println("Episódio encontrado!");
           System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
       } else {
           System.out.println("Episódio não encontrado!");
       }

//       System.out.println("A partir de qual ano você deseja ver os episódios?");
//       var ano = scanner.nextInt();
//       scanner.nextLine();
//
//       LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//       DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//       episodios.stream()
//               .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//               .forEach(e -> System.out.println(
//                       "Temporada: " + e.getTemporada() +
//                               "Epísódio: " + e.getTitulo() +
//                               "Data de lançamento: " + e.getDataLancamento().format(formatador)
//               ));

       Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
               .filter(e -> e.getAvaliacao() > 0.0)
               .collect(Collectors.groupingBy(Episodio::getTemporada,
                       Collectors.averagingDouble(Episodio::getAvaliacao)));

       System.out.println(avaliacoesPorTemporada);

       DoubleSummaryStatistics est = episodios.stream()
               .filter(e -> e.getAvaliacao() > 0.0)
               .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

       System.out.println("Média: " + est.getAverage());
   }
}
