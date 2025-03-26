package alen.si.exercise2;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;





@SpringBootApplication
@EnableAsync
public class Exercise2Application implements CommandLineRunner{
    private final DataProcessorService dataProcesService;

    public Exercise2Application(DataProcessorService dataProcesService)
    {
        this.dataProcesService = dataProcesService;
    }

    public static void main(String[] args)
    {
        SpringApplication.run(Exercise2Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception
    {

        String filePath = "src/main/resources/fo_random.txt";
        dataProcesService.processFile(filePath);

    }
}
