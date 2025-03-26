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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class DataProcesService {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final PriorityBlockingQueue<String[]> queue = new PriorityBlockingQueue<>(1000,
            Comparator.comparing((String[] a) -> Integer.parseInt(a[0])) // Sort by MATCH_ID
                    .thenComparing(a -> a[1].charAt(0)) // 'A' before 'B'
                    .thenComparingInt(a -> Integer.parseInt(a[2])) // Event sequence order
    );

    public DataProcesService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processFile(String filePath) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                queue.offer(parts);
            }
        }

        // Start multiple workers to process the queue
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            executorService.submit(this::processQueue);
        }
    }

    @Async
    public void processQueue() {
        List<String[]> batch = new ArrayList<>();
        while (true) {
            try {
                String[] record = queue.poll(1, TimeUnit.SECONDS);
                if (record != null) {
                    batch.add(record);
                    if (batch.size() >= 100) {
                        insertBatch(batch);
                        batch.clear();
                    }
                } else {
                    // Sleep briefly to avoid busy waiting
                    Thread.sleep(10);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void insertBatch(List<String[]> batch) {
        String sql = "INSERT INTO match_events (match_id, market_id, outcome_id, specifiers, date_insert) VALUES (?, ?, ?, ?, ?)";
        List<Object[]> batchArgs = new ArrayList<>();

        for (String[] record : batch) {
            batchArgs.add(new Object[]{
                    Integer.parseInt(record[0]),
                    Integer.parseInt(record[1]),
                    Integer.parseInt(record[2]),
                    record[3],
                    new Timestamp(System.currentTimeMillis())
            });
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    public void printMinMaxTimestamp() {
        String minDateSQL = "SELECT MIN(date_insert) FROM match_events";
        String maxDateSQL = "SELECT MAX(date_insert) FROM match_events";

        Timestamp minDate = jdbcTemplate.queryForObject(minDateSQL, Timestamp.class);
        Timestamp maxDate = jdbcTemplate.queryForObject(maxDateSQL, Timestamp.class);

        System.out.println("Earliest Inserted Record: " + minDate);
        System.out.println("Latest Inserted Record: " + maxDate);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
