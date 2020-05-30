package org.folio.dao;

import static org.folio.rest.jooq.Tables.RAW_RECORDS_LB;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.NotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.jaxrs.model.RawRecord;
import org.folio.rest.jooq.tables.mappers.RowMappers;
import org.folio.rest.jooq.tables.pojos.RawRecordsLb;
import org.folio.rest.jooq.tables.records.RawRecordsLbRecord;
import org.jooq.Condition;
import org.jooq.InsertSetStep;
import org.jooq.InsertValuesStepN;
import org.jooq.OrderField;

import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class LBRawRecordDao {

  private LBRawRecordDao() { }

  public static Future<List<RawRecord>> findByCondition(ReactiveClassicGenericQueryExecutor queryExecutor, Condition condition,
      Collection<OrderField<?>> orderFields, int offset, int limit) {
    return queryExecutor.executeAny(dsl -> dsl.selectFrom(RAW_RECORDS_LB)
      .where(condition)
      .orderBy(orderFields)
      .offset(offset)
      .limit(limit))
        .map(LBRawRecordDao::toRawRecords);
  }

  public static Future<Optional<RawRecord>> findByCondition(ReactiveClassicGenericQueryExecutor queryExecutor, Condition condition) {
    return queryExecutor.findOneRow(dsl -> dsl.selectFrom(RAW_RECORDS_LB)
      .where(condition))
        .map(LBRawRecordDao::toOptionalRawRecord);
  }

  public static Future<Optional<RawRecord>> findById(ReactiveClassicGenericQueryExecutor queryExecutor, String id) {
    return queryExecutor.findOneRow(dsl -> dsl.selectFrom(RAW_RECORDS_LB)
      .where(RAW_RECORDS_LB.ID.eq(UUID.fromString(id))))
        .map(LBRawRecordDao::toOptionalRawRecord);
  }

  public static Future<RawRecord> save(ReactiveClassicGenericQueryExecutor queryExecutor, RawRecord rawRecord) {
    RawRecordsLbRecord dbRecord = toDatabaseRawRecord(rawRecord);
    return queryExecutor.executeAny(dsl -> dsl.insertInto(RAW_RECORDS_LB)
      .set(dbRecord)
      .onDuplicateKeyUpdate()
      .set(dbRecord)
      .returning())
        .map(LBRawRecordDao::toRawRecord);
  }

  public static Future<List<RawRecord>> save(ReactiveClassicGenericQueryExecutor queryExecutor, List<RawRecord> snapshots) {
    return queryExecutor.executeAny(dsl -> {
      InsertSetStep<RawRecordsLbRecord> insertSetStep = dsl.insertInto(RAW_RECORDS_LB);
      InsertValuesStepN<RawRecordsLbRecord> insertValuesStepN = null;
      for (RawRecord RawRecord : snapshots) {
          insertValuesStepN = insertSetStep.values(toDatabaseRawRecord(RawRecord).intoArray());
      }
      return insertValuesStepN;
    }).map(LBRawRecordDao::toRawRecords);
  }

  public static Future<RawRecord> update(ReactiveClassicGenericQueryExecutor queryExecutor, RawRecord rawRecord) {
    RawRecordsLbRecord dbRecord = toDatabaseRawRecord(rawRecord);
    return queryExecutor.executeAny(dsl -> dsl.update(RAW_RECORDS_LB)
      .set(dbRecord)
      .where(RAW_RECORDS_LB.ID.eq(UUID.fromString(rawRecord.getId())))
      .returning())
        .map(LBRawRecordDao::toOptionalRawRecord)
        .map(optionalRawRecord -> {
          if (optionalRawRecord.isPresent()) {
            return optionalRawRecord.get();
          }
          throw new NotFoundException(String.format("RawRecord with id '%s' was not found", rawRecord.getId()));
        });
  }

  public static Future<Boolean> delete(ReactiveClassicGenericQueryExecutor queryExecutor, String id) {
    return queryExecutor.execute(dsl -> dsl.deleteFrom(RAW_RECORDS_LB)
      .where(RAW_RECORDS_LB.ID.eq(UUID.fromString(id))))
      .map(res -> res == 1);
  }

  public static Future<Integer> deleteAll(ReactiveClassicGenericQueryExecutor queryExecutor) {
    return queryExecutor.execute(dsl -> dsl.deleteFrom(RAW_RECORDS_LB));
  }

  public static RawRecord toRawRecord(Row row) {
    RawRecordsLb pojo = RowMappers.getRawRecordsLbMapper().apply(row);
    return new RawRecord()
      .withId(pojo.getId().toString())
      .withContent(pojo.getContent());
  }

  public static RawRecordsLbRecord toDatabaseRawRecord(RawRecord rawRecord) {
    RawRecordsLbRecord dbRecord = new RawRecordsLbRecord();
    if (StringUtils.isNotEmpty(rawRecord.getId())) {
      dbRecord.setId(UUID.fromString(rawRecord.getId()));
    }
    dbRecord.setContent(rawRecord.getContent());
    return dbRecord;
  }

  private static RawRecord toRawRecord(RowSet<Row> rows) {
    return toRawRecord(rows.iterator().next());
  }

  private static List<RawRecord> toRawRecords(RowSet<Row> rows) {
    return StreamSupport.stream(rows.spliterator(), false).map(LBRawRecordDao::toRawRecord).collect(Collectors.toList());
  }

  private static Optional<RawRecord> toOptionalRawRecord(RowSet<Row> rows) {
    return rows.rowCount() == 1 ? Optional.of(toRawRecord(rows.iterator().next())) : Optional.empty();
  }

  private static Optional<RawRecord> toOptionalRawRecord(Row row) {
    return Objects.nonNull(row) ? Optional.of(toRawRecord(row)) : Optional.empty();
  }

}