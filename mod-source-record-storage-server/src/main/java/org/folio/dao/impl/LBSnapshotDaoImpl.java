package org.folio.dao.impl;

import java.text.ParseException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.dao.LBSnapshotDao;
import org.folio.dao.PostgresClientFactory;
import org.folio.rest.jaxrs.model.Snapshot;
import org.folio.rest.jaxrs.model.SnapshotCollection;
import org.folio.rest.persist.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;

// <createTable tableName="snapshots_lb">
//   <column name="id" type="uuid">
//     <constraints primaryKey="true" nullable="false"/>
//   </column>
//   <column name="status" type="${database.defaultSchemaName}.job_execution_status">
//     <constraints nullable="false"/>
//   </column>
//   <column name="processing_started_date" type="timestamp"></column>
// </createTable>
@Component
public class LBSnapshotDaoImpl implements LBSnapshotDao {

  private static final Logger LOG = LoggerFactory.getLogger(LBSnapshotDaoImpl.class);

  private static final String TABLE_NAME = "snapshots_lb";

  private final PostgresClientFactory pgClientFactory;

  public LBSnapshotDaoImpl(@Autowired PostgresClientFactory pgClientFactory) {
    this.pgClientFactory = pgClientFactory;
  }

  @Override
  public Logger getLogger() {
    return LOG;
  }

  @Override
  public PostgresClient getPostgresClient(String tenantId) {
    return pgClientFactory.createInstance(tenantId);
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public String getId(Snapshot snapshot) {
    return snapshot.getJobExecutionId();
  }

  @Override
  public String toColumns(Snapshot snapshot) {
    String columns = "id";
    if (snapshot.getStatus() != null) {
      columns += ",status";
    }
    if (snapshot.getProcessingStartedDate() != null) {
      columns += ",processing_started_date";
    }
    return columns;
  }

  @Override
  public String toValues(Snapshot snapshot, boolean generateIdIfNotExists) {
    if (generateIdIfNotExists && StringUtils.isEmpty(snapshot.getJobExecutionId())) {
      snapshot.setJobExecutionId(UUID.randomUUID().toString());
    }
    String values = String.format("'%s'", snapshot.getJobExecutionId());
    if (snapshot.getStatus() != null) {
      values = String.format("%s,'%s'", values, snapshot.getStatus().toString());
    }
    if (snapshot.getProcessingStartedDate() != null) {
      values = String.format("%s,'%s'", values, ISO_8601_FORMAT.format(snapshot.getProcessingStartedDate()));
    }
    return values;
  }

  @Override
  public SnapshotCollection toCollection(ResultSet resultSet) {
    return new SnapshotCollection()
      .withSnapshots(resultSet.getRows().stream().map(this::toBean).collect(Collectors.toList()))
      .withTotalRecords(resultSet.getNumRows());
  }

  @Override
  public Snapshot toBean(JsonObject result) {
    Snapshot snapshot = new Snapshot().withJobExecutionId(result.getString("id"))
      .withStatus(Snapshot.Status.fromValue(result.getString("status")));
    String processingStartedDate = result.getString("processing_started_date");
    if (StringUtils.isNotEmpty(processingStartedDate)) {
      try {
        snapshot.setProcessingStartedDate(ISO_8601_FORMAT.parse(processingStartedDate));
      } catch (ParseException e) {
        LOG.error(e.getMessage(), e.getCause());
      }
    }
    return snapshot;
  }

}