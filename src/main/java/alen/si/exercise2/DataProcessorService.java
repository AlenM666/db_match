package alen.si.exercise2;

import alen.si.exercise2.model.MatchEvent;
import alen.si.exercise2.repository.MatchEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class DataProcessorService {

    @Autowired
    private MatchEventRepository matchEventRepository;

    private final ConcurrentMap<Integer, List<MatchEvent>> matchEventBuffer = new ConcurrentHashMap<>();

    @Async
    @Transactional
    public CompletableFuture<Void> processFile(String filePath) {
        return CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }

                // Process and flush remaining events
                processRemainingEvents();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void processLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length == 4) {
            int matchId = Integer.parseInt(parts[0]);
            int marketId = Integer.parseInt(parts[1]);
            int outcomeId = Integer.parseInt(parts[2]);
            String specifiers = parts[3];

            MatchEvent event = new MatchEvent(matchId, marketId, outcomeId, specifiers);

            // Buffer events by matchId
            matchEventBuffer.computeIfAbsent(matchId, k -> new ArrayList<>()).add(event);

            // If buffer for this matchId reaches a threshold, process and clear
            if (matchEventBuffer.get(matchId).size() >= 100) {
                processEventsForMatchId(matchId);
            }
        }
    }

    private void processEventsForMatchId(int matchId) {
        List<MatchEvent> events = matchEventBuffer.get(matchId);
        if (events != null) {
            // Sort events for the specific matchId
            events.sort(Comparator.comparing(MatchEvent::getDateInsert));

            // Batch insert sorted events
            matchEventRepository.saveAll(events);

            // Clear the buffer for this matchId
            matchEventBuffer.remove(matchId);
        }
    }

    private void processRemainingEvents() {
        // Process any remaining events in the buffer
        matchEventBuffer.forEach((matchId, events) -> {
            events.sort(Comparator.comparing(MatchEvent::getDateInsert));
            matchEventRepository.saveAll(events);
        });
        matchEventBuffer.clear();
    }

    // Method to get min and max dateInsert
    public String getDateInsertStatistics() {
        return String.format("Min dateInsert: %s, Max dateInsert: %s",
                matchEventRepository.findMinDateInsert(),
                matchEventRepository.findMaxDateInsert());
    }
}