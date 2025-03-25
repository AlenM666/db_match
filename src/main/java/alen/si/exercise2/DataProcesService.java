package alen.si.exercise2;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class DataProcesService {
    private final JdbcTemplate jdbcTemplate;
    //private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final PriorityBlockingQueue<String[]> queue = new PriorityBlockingQueue<>(1000, Comparator.comparingInt(a-> Integer.parseInt(a[0])));

    public DataProcesService(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void processFile(String filePath) throws Exception
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {
            String line;
            while ((line = br.readLine()) != null )
            {
                String[] parts = line.split("\\|");
                queue.offer(parts);
            }
        }
        for(int i=0;i<4;i++)
        {
            executorService.submit(this::processQueue);
        }
    }


    @Async
    public void processQueue()
    {
        List<String[]> batch = new ArrayList<>();
        while(!queue.isEmpty())
        {
            try
            {
                String[] record = queue.poll(1, TimeUnit.SECONDS);
                if(record != null)
                {
                    batch.add(record);
                    if(batch.size()>=100)
                    {
                        insertBatch(batch);
                        batch.clear();
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
            if(!batch.isEmpty())
            {
                insertBatch(batch);
            }
        }
    }


    private void insertBatch(List<String[]> batch)
    {
        String sql= "INSERT INTO match_events (match_id, market_id, outcome_id, specifiers, date_insert) VALUES (?, ?, ?, ?, ?)";;
        List<Object[]> batchArgs = new ArrayList<>();
        for(String[] record : batch)
        {
            batchArgs.add(new Object[]
            {
                    Integer.parseInt(record[0]),
                    Integer.parseInt(record[1]),
                    Integer.parseInt(record[2]),
                    record[3],
                    new Timestamp(System.currentTimeMillis())
            });
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
