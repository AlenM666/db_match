package alen.si.m_dclone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "match_java")
//@GeneratedValue(strategy = GenerationType.AUTO)
public class DataEntity
{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public long id;
  public String idRaw;
  public String marketId;
  public String outcomeId;
  public String specifiers;
  public Instant dateInsert;

  public DataEntity()
  {
    this.dateInsert = Instant.now(); // Automatically set timestamp
  }

  public DataEntity(long id, String idRaw, String marketId, String outcomeId, String specifiers, Instant dateInsert)
  {
    this.id = id;
    this.idRaw = idRaw;
    this.marketId = marketId;
    this.outcomeId = outcomeId;
    this.specifiers = specifiers;
    this.dateInsert = dateInsert;
  }

}