package alen.si.m_dclone;

import alen.si.m_dclone.repositorys.DataEntityRepository;
import alen.si.m_dclone.serives.DataService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Instant;

@SpringBootApplication
@EnableAsync
public class MDCloneApplication {
  private final DataService dataService;
  private final DataEntityRepository dataEntityRepository;

    public MDCloneApplication(DataService dataService, DataEntityRepository dataEntityRepository) {
        this.dataService = dataService;
        this.dataEntityRepository = dataEntityRepository;
    }

    public static void main(String[] args) {
    SpringApplication.run(MDCloneApplication.class, args);
//    String filePath = "fo/fo-copy.txt";
//    DataService.importDataService(filePath);
  }

  @Bean
  CommandLineRunner run(DataService dataService)
  {
    return args ->
    {
//      String filePath = "fo/fo-copy.txt";
      String filePath = "fo/fo_random.txt";
      dataService.importDataService(filePath);

      Instant minDateInsert = dataEntityRepository.findMinDateInsert();
      Instant maxDateInsert = dataEntityRepository.findMaxDateInsert();
      System.out.println("Earliest Insertion: " + minDateInsert);
      System.out.println("Latest Insertion: " + maxDateInsert);
//      DataService.importDataService(filePath);
    };
  }

}
