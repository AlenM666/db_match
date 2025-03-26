package alen.si.exercise2;

import alen.si.exercise2.model.MatchEvent;
import alen.si.exercise2.repository.MatchEventRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DataProcessorService {

    @Autowired
    private MatchEventRepository matchEventRepository;

    // Enhanced data structure to track event sequence and priority
    @Data
    private class EventWrapper implements Comparable<EventWrapper> {
        private MatchEvent event;
        private String eventType;  // A or B
        private int sequenceNumber;
        private Instant processingTimestamp;

        @Override
        public int compareTo(EventWrapper other) {
            //int matchIdComparison = Integer.compare(this.event.getMatchId(), other.event.getMatchId());
            int matchIdComparison = this.event.getMatchId().compareTo(other.event.getMatchId());

            if (matchIdComparison != 0) return matchIdComparison;

            int eventTypePriority = this.eventType.compareTo(other.eventType);
            if (eventTypePriority != 0) return eventTypePriority;

            return Integer.compare(this.sequenceNumber, other.sequenceNumber);
        }
    }

    private final PriorityBlockingQueue<EventWrapper> eventQueue =
            new PriorityBlockingQueue<>(1000, Comparator.naturalOrder());

    private final ConcurrentMap<String, AtomicInteger> sequenceTrackers = new ConcurrentHashMap<>();

    @Transactional
    public void processFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            System.out.println("Reading file: " + filePath);
            startEventProcessor();

            String line;
            boolean isFirstLine = true; // Track first line

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;  // Skip header
                    System.out.println("Skipping header: " + line);
                    continue;
                }

                System.out.println("Processing line: " + line);
                processLine(line);
            }

            eventQueue.put(createTerminationEvent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void processLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            // Keep matchId as a String, no need to parse as an int
            String matchId = parts[0].replace("'", "").trim(); // Updated to treat matchId as String
            int marketId = Integer.parseInt(parts[1]);
            int outcomeId = Integer.parseInt(parts[2]);
            String specifiers = parts[3];

            // Determine event type (A or B) based on processing speed
            String eventType = determineEventType();

            // Create the MatchEvent with String matchId
            MatchEvent event = new MatchEvent(matchId, marketId, outcomeId, specifiers);

            // Create an EventWrapper for sorting in the priority queue
            EventWrapper wrapper = new EventWrapper();
            wrapper.setEvent(event);
            wrapper.setEventType(eventType);
            wrapper.setProcessingTimestamp(Instant.now());

            // Increment sequence number for this matchId and eventType
            String key = matchId + eventType;
            wrapper.setSequenceNumber(
                    sequenceTrackers.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet()
            );

            // Add event to the queue for processing
            eventQueue.put(wrapper);
        }
    }




    /*
    private void processLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            int matchId = Integer.parseInt(parts[0]);
            int marketId = Integer.parseInt(parts[1]);
            int outcomeId = Integer.parseInt(parts[2]);
            String specifiers = parts[3];

            String eventType = determineEventType();
            MatchEvent event = new MatchEvent(matchId, marketId, outcomeId, specifiers);

            EventWrapper wrapper = new EventWrapper();
            wrapper.setEvent(event);
            wrapper.setEventType(eventType);
            wrapper.setProcessingTimestamp(Instant.now());

            String key = matchId + eventType;
            wrapper.setSequenceNumber(
                    sequenceTrackers.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet()
            );

            eventQueue.put(wrapper);
            System.out.println("Queued event: " + wrapper.getEvent());
        }
    }

     */

    private void startEventProcessor() {
        new Thread(() -> {
            List<MatchEvent> batch = new ArrayList<>();
            final int BATCH_SIZE = 50; // Batch insert size

            try {
                while (true) {
                    EventWrapper wrapper = eventQueue.take();
                    if (isTerminationEvent(wrapper)) {
                        if (!batch.isEmpty()) {
                            saveBatch(batch);
                        }
                        break;
                    }

                    batch.add(wrapper.getEvent());
                    System.out.println("Saving event: " + wrapper.getEvent());

                    if (batch.size() >= BATCH_SIZE) {
                        saveBatch(batch);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error inserting event: " + e.getMessage());
            }
        }).start();
    }

    @Transactional
    protected void saveBatch(List<MatchEvent> batch) {
        matchEventRepository.saveAll(batch);
        System.out.println("Batch of " + batch.size() + " events saved successfully!");
        batch.clear();
    }

    private String determineEventType() {
        return Math.random() < 0.5 ? "A" : "B";
    }

    private EventWrapper createTerminationEvent() {
        EventWrapper terminator = new EventWrapper();
        terminator.setEventType("TERMINATE");
        return terminator;
    }

    private boolean isTerminationEvent(EventWrapper wrapper) {
        return "TERMINATE".equals(wrapper.getEventType());
    }

    public String getDateInsertStatistics() {
        return String.format("Min dateInsert: %s, Max dateInsert: %s",
                matchEventRepository.findMinDateInsert(),
                matchEventRepository.findMaxDateInsert());
    }
}
