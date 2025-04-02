package alen.si.m_dclone.serives;

import alen.si.m_dclone.entity.DataEntity;
import alen.si.m_dclone.repositorys.DataEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.*;

@Service
public class DataService {
  public final DataEntityRepository dataEntityRepository;

  public DataService(DataEntityRepository dataEntityRepository)
  {
    this.dataEntityRepository = dataEntityRepository;
  }


  @Transactional
  public void importDataService(String filePath)
  {
//    List<DataEntity> dataList = new ArrayList<>();
//      BufferedReader reader = new BufferedReader(new FileReader(filePath));

    try(BufferedReader reader = new BufferedReader(new FileReader(filePath)))
    {
      Map<String, List<DataEntity>> dataMap = new TreeMap<>();

      System.out.println("\nFile found: " + filePath + "\n");
      String line;
      boolean firstLine = true;

      while((line = reader.readLine()) != null)
      {
        // to skip the header
        if(firstLine)
        {
          firstLine = false;
          if(line.contains("MATCH_ID"))
          {
            continue;
          }
        }

        String[] parts = line.split("\\|");
        if(parts.length < 4) continue;

        String matchId = parts[0].replace("'", "").trim();
        String marketId = parts[1].trim();
        String outcomeId = parts[2].replace("'", "").trim();
        String specifiers = (parts.length > 4) ? parts[3].replace("'", "").trim() : null;

        //create entitiy obj
        DataEntity dataEntity = new DataEntity();
        dataEntity.setIdRaw(matchId);
        dataEntity.setMarketId(marketId);
        dataEntity.setOutcomeId(outcomeId);
        dataEntity.setSpecifiers(specifiers);
        dataEntity.setDateInsert(Instant.now());

//        dataList.add(dataEntity);
        dataMap.computeIfAbsent(matchId, k -> new ArrayList<>()).add(dataEntity);
      }
      for(List<DataEntity> dataList : dataMap.values())
      {
        dataList.sort(Comparator.comparing(DataEntity::getIdRaw)
                .thenComparing(DataEntity::getMarketId)
                .thenComparing(DataEntity::getOutcomeId)
                .thenComparing(DataEntity::getSpecifiers, Comparator.nullsFirst(Comparator.naturalOrder())));
        dataEntityRepository.saveAll(dataList);
      }
      System.out.println("\n\nData imported successfully");

      // save all data in a single batch
      //dataEntityRepository.saveAll(dataList);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
  }


//  public static void readFile(String filePath)
//  {
//    try {
//      BufferedReader reader = new BufferedReader(new FileReader(filePath));
//      System.out.println("\nFile found: " + filePath);
//
//      String line;
//      line = reader.readLine();
//      boolean isFirstline = true;
//
//      int counter = 0;
//      while (line != null && counter < 10)
//      {
//        System.out.println(line);
//        line = reader.readLine();
//        counter++;
//      }
//      reader.close();
//
//    } catch (FileNotFoundException e) {
//      throw new RuntimeException(e);
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//
//  }

}
