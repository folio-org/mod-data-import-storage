package org.folio.dao.impl;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.StreamSupport.stream;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.folio.dao.util.DaoUtil.CREATED_BY_USER_ID_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.CREATED_DATE_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.GET_BY_WHERE_SQL_TEMPLATE;
import static org.folio.dao.util.DaoUtil.ID_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.RECORDS_TABLE_NAME;
import static org.folio.dao.util.DaoUtil.UPDATED_BY_USER_ID_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.UPDATED_DATE_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.execute;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.folio.dao.AbstractEntityDao;
import org.folio.dao.LBRecordDao;
import org.folio.dao.query.RecordQuery;
import org.folio.dao.util.ColumnBuilder;
import org.folio.dao.util.DaoUtil;
import org.folio.dao.util.ExternalIdType;
import org.folio.dao.util.TupleWrapper;
import org.folio.rest.jaxrs.model.AdditionalInfo;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.Record.RecordType;
import org.folio.rest.jaxrs.model.Record.State;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.jaxrs.model.SuppressFromDiscoveryDto.IncomingIdType;
import org.springframework.stereotype.Component;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

// <createTable tableName="records_lb">
//   <column name="id" type="uuid">
//     <constraints primaryKey="true" nullable="false"/>
//   </column>
//   <column name="snapshotid" type="uuid">
//     <constraints nullable="false"/>
//   </column>
//   <column name="matchedid" type="uuid">
//     <constraints nullable="false"/>
//   </column>
//   <column name="generation" type="integer">
//     <constraints nullable="false"/>
//   </column>
//   <column name="recordtype" type="${database.defaultSchemaName}.record_type">
//     <constraints nullable="false"/>
//   </column>
//   <column name="instanceid" type="uuid"></column>
//   <column name="state" type="${database.defaultSchemaName}.record_state_type">
//     <constraints nullable="false"/>
//   </column>
//   <column name="orderinfile" type="integer"></column>
//   <column name="suppressdiscovery" type="boolean"></column>
//   <column name="createdbyuserid" type="uuid"></column>
//   <column name="createddate" type="timestamptz"></column>
//   <column name="updatedbyuserid" type="uuid"></column>
//   <column name="updateddate" type="timestamptz"></column>
// </createTable>
@Component
public class LBRecordDaoImpl extends AbstractEntityDao<Record, RecordCollection, RecordQuery> implements LBRecordDao {

  public static final String MATCHED_ID_COLUMN_NAME = "matchedid";
  public static final String SNAPSHOT_ID_COLUMN_NAME = "snapshotid";
  public static final String GENERATION_COLUMN_NAME = "generation";
  public static final String ORDER_IN_FILE_COLUMN_NAME = "orderinfile";
  public static final String RECORD_TYPE_COLUMN_NAME = "recordtype";
  public static final String STATE_COLUMN_NAME = "state";
  public static final String INSTANCE_ID_COLUMN_NAME = "instanceid";
  public static final String SUPPRESS_DISCOVERY_COLUMN_NAME = "suppressdiscovery";

  private static final String GET_RECORD_GENERATION_TEMPLATE = "SELECT get_highest_generation_lb('%s','%s');";

  @Override
  public Future<Optional<Record>> getByMatchedId(String matchedId, String tenantId) {
    return execute(postgresClientFactory.getClient(tenantId), connection ->
      getByMatchedId(connection, matchedId, tenantId));
  }

  @Override
  public Future<Optional<Record>> getByMatchedId(SqlConnection connetion, String matchedId, String tenantId) {
    String sql = format(GET_BY_WHERE_SQL_TEMPLATE, getColumns(), getTableName(), MATCHED_ID_COLUMN_NAME, matchedId);
    log.info("Attempting get by matched id: {}", sql);
    return select(connetion, sql, tenantId);
  }

  @Override
  public Future<Optional<Record>> getByInstanceId(String instanceId, String tenantId) {
    return execute(postgresClientFactory.getClient(tenantId), connection ->
      getByInstanceId(connection, instanceId, tenantId));
  }

  @Override
  public Future<Optional<Record>> getByInstanceId(SqlConnection connetion, String instanceId, String tenantId) {
    String sql = format(GET_BY_WHERE_SQL_TEMPLATE, getColumns(), getTableName(), INSTANCE_ID_COLUMN_NAME, instanceId);
    log.info("Attempting get by instance id: {}", sql);
    return select(connetion, sql, tenantId);
  }

  @Override
  public Future<Integer> calculateGeneration(Record record, String tenantId) {
    return execute(postgresClientFactory.getClient(tenantId), connection ->
      calculateGeneration(connection, record, tenantId));
  }

  @Override
  public Future<Integer> calculateGeneration(SqlConnection connetion, Record record, String tenantId) {
    Promise<RowSet<Row>> promise = Promise.promise();
    String sql = format(GET_RECORD_GENERATION_TEMPLATE, record.getMatchedId(), record.getSnapshotId());
    log.info("Attempting get record generation: {}", sql);
    connetion.query(sql).execute(promise);
    return promise.future().map(resultSet -> {
      Integer generation = resultSet.iterator().next().getInteger(0);
      if (generation > 0) {
        generation++;
      }
      return generation;
    });
  }

  @Override
  public Future<Optional<Record>> getRecordById(String id, IncomingIdType idType, String tenantId) {
    return execute(postgresClientFactory.getClient(tenantId), connection ->
      getRecordById(connection, id, idType, tenantId));
  }

  @Override
  public Future<Optional<Record>> getRecordById(SqlConnection connection, String id, IncomingIdType idType, String tenantId) {
    if (idType == IncomingIdType.INSTANCE) {
      return getByInstanceId(connection, id, tenantId);
    } else {
      return getById(connection, id, tenantId);
    }
  }

  @Override
  public Future<Optional<Record>> getRecordByExternalId(String externalId, ExternalIdType externalIdType, String tenantId) {
    return execute(postgresClientFactory.getClient(tenantId), connection ->
      getRecordByExternalId(connection, externalId, externalIdType, tenantId));
  }

  @Override
  public Future<Optional<Record>> getRecordByExternalId(SqlConnection connection, String externalId, ExternalIdType externalIdType, String tenantId) {
    if (externalIdType == ExternalIdType.INSTANCE) {
      return getByInstanceId(externalId, tenantId);
    }
    return getById(externalId, tenantId);
  }

  @Override
  public String getTableName() {
    return RECORDS_TABLE_NAME;
  }

  @Override
  public String getColumns() {
    return ColumnBuilder
      .of(ID_COLUMN_NAME)
      .append(SNAPSHOT_ID_COLUMN_NAME)
      .append(MATCHED_ID_COLUMN_NAME)
      .append(GENERATION_COLUMN_NAME)
      .append(RECORD_TYPE_COLUMN_NAME)
      .append(INSTANCE_ID_COLUMN_NAME)
      .append(STATE_COLUMN_NAME)
      .append(ORDER_IN_FILE_COLUMN_NAME)
      .append(SUPPRESS_DISCOVERY_COLUMN_NAME)
      .append(CREATED_BY_USER_ID_COLUMN_NAME)
      .append(CREATED_DATE_COLUMN_NAME)
      .append(UPDATED_BY_USER_ID_COLUMN_NAME)
      .append(UPDATED_DATE_COLUMN_NAME)
      .build();
  }

  @Override
  public String getId(Record record) {
    return record.getId();
  }

  @Override
  protected Tuple toTuple(Record record, boolean generateIdIfNotExists) {
    if (generateIdIfNotExists && isEmpty(record.getId())) {
      record.setId(UUID.randomUUID().toString());
    }
    if (isEmpty(record.getMatchedId())) {
      record.setMatchedId(record.getId());
    }
    TupleWrapper tupleWrapper = TupleWrapper.of()
      .addUUID(record.getId())
      .addUUID(record.getSnapshotId())
      .addUUID(record.getMatchedId())
      .addInteger(record.getGeneration())
      .addEnum(record.getRecordType());
    if (nonNull(record.getExternalIdsHolder())) {
      tupleWrapper.addUUID(record.getExternalIdsHolder().getInstanceId());
    } else {
      tupleWrapper.addNull();
    }
    tupleWrapper.addEnum(record.getState())
      .addInteger(record.getOrder());
    if (nonNull(record.getAdditionalInfo())) {
      tupleWrapper.addBoolean(record.getAdditionalInfo().getSuppressDiscovery());
    } else {
      tupleWrapper.addNull();
    }
    if (nonNull(record.getMetadata())) {
      tupleWrapper.addUUID(record.getMetadata().getCreatedByUserId())
        .addOffsetDateTime(record.getMetadata().getCreatedDate())
        .addUUID(record.getMetadata().getUpdatedByUserId())
        .addOffsetDateTime(record.getMetadata().getUpdatedDate());
    } else {
      tupleWrapper.addNull().addNull().addNull().addNull();
    }
    return tupleWrapper.get();
  }

  @Override
  protected RecordCollection toCollection(RowSet<Row> rowSet) {
    return toEmptyCollection(rowSet)
      .withRecords(stream(rowSet.spliterator(), false)
        .map(this::toEntity).collect(Collectors.toList()));
  }

  @Override
  protected RecordCollection toEmptyCollection(RowSet<Row> rowSet) {
    return new RecordCollection()
      .withRecords(Collections.emptyList())
      .withTotalRecords(DaoUtil.getTotalRecords(rowSet));
  }

  @Override
  protected Record toEntity(Row row) {
    Record record = new Record()
      .withId(row.getUUID(ID_COLUMN_NAME).toString())
      .withSnapshotId(row.getUUID(SNAPSHOT_ID_COLUMN_NAME).toString())
      .withMatchedId(row.getUUID(MATCHED_ID_COLUMN_NAME).toString())
      .withGeneration(row.getInteger(GENERATION_COLUMN_NAME))
      .withRecordType(RecordType.valueOf(row.getString(RECORD_TYPE_COLUMN_NAME)));
    UUID instanceId = row.getUUID(INSTANCE_ID_COLUMN_NAME);
    if (nonNull(instanceId)) {
      ExternalIdsHolder externalIdHolder = new ExternalIdsHolder();
      externalIdHolder.setInstanceId(instanceId.toString());
      record.setExternalIdsHolder(externalIdHolder);
    }
    record.withState(State.valueOf(row.getString(STATE_COLUMN_NAME)))
      .withOrder(row.getInteger(ORDER_IN_FILE_COLUMN_NAME));
    Boolean suppressDiscovery = row.getBoolean(SUPPRESS_DISCOVERY_COLUMN_NAME);
    if (nonNull(suppressDiscovery)) {
      AdditionalInfo additionalInfo = new AdditionalInfo();
      additionalInfo.setSuppressDiscovery(suppressDiscovery);
      record.setAdditionalInfo(additionalInfo);
    }
    return record
      .withMetadata(DaoUtil.metadataFromRow(row));
  }

}