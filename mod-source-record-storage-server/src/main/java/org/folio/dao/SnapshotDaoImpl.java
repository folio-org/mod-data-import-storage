package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.UpdateResult;
import org.folio.rest.jaxrs.model.Snapshot;
import org.folio.rest.jaxrs.model.SnapshotCollection;
import org.folio.rest.persist.Criteria.Criteria;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.cql.CQLWrapper;
import org.folio.rest.persist.interfaces.Results;

import java.util.Optional;

import static org.folio.dao.util.DaoUtil.constructCriteria;
import static org.folio.dao.util.DaoUtil.getCQL;

public class SnapshotDaoImpl implements SnapshotDao {

  private static final Logger LOG = LoggerFactory.getLogger(SnapshotDaoImpl.class);

  private static final String SNAPSHOTS_TABLE = "snapshots";
  private static final String SNAPSHOT_ID_FIELD = "'jobExecutionId'";

  private PostgresClient pgClient;

  public SnapshotDaoImpl(Vertx vertx, String tenantId) {
    pgClient = PostgresClient.getInstance(vertx, tenantId);
  }

  @Override
  public Future<SnapshotCollection> getSnapshots(String query, int offset, int limit) {
    Future<Results<Snapshot>> future = Future.future();
    try {
      String[] fieldList = {"*"};
      CQLWrapper cql = getCQL(SNAPSHOTS_TABLE, query, limit, offset);
      pgClient.get(SNAPSHOTS_TABLE, Snapshot.class, fieldList, cql, true, false, future.completer());
    } catch (Exception e) {
      LOG.error("Error while querying snapshots", e);
      future.fail(e);
    }
    return future.map(results -> new SnapshotCollection()
      .withSnapshots(results.getResults())
      .withTotalRecords(results.getResultInfo().getTotalRecords()));
  }

  @Override
  public Future<Optional<Snapshot>> getSnapshotById(String id) {
    Future<Results<Snapshot>> future = Future.future();
    try {
      Criteria idCrit = constructCriteria(SNAPSHOT_ID_FIELD, id);
      pgClient.get(SNAPSHOTS_TABLE, Snapshot.class, new Criterion(idCrit), true, false, future.completer());
    } catch (Exception e) {
      LOG.error("Error querying snapshots by id", e);
      future.fail(e);
    }
    return future
      .map(Results::getResults)
      .map(snapshots -> snapshots.isEmpty() ? Optional.empty() : Optional.of(snapshots.get(0)));
  }

  @Override
  public Future<String> saveSnapshot(Snapshot snapshot) {
    Future<String> future = Future.future();
    pgClient.save(SNAPSHOTS_TABLE, snapshot.getJobExecutionId(), snapshot, future.completer());
    return future;
  }

  @Override
  public Future<Boolean> updateSnapshot(Snapshot snapshot) {
    Future<UpdateResult> future = Future.future();
    try {
      Criteria idCrit = constructCriteria(SNAPSHOT_ID_FIELD, snapshot.getJobExecutionId());
      pgClient.update(SNAPSHOTS_TABLE, snapshot, new Criterion(idCrit), true, future.completer());
    } catch (Exception e) {
      LOG.error("Error updating snapshots", e);
      future.fail(e);
    }
    return future.map(updateResult -> updateResult.getUpdated() == 1);
  }

  @Override
  public Future<Boolean> deleteSnapshot(String id) {
    Future<UpdateResult> future = Future.future();
    pgClient.delete(SNAPSHOTS_TABLE, id, future.completer());
    return future.map(updateResult -> updateResult.getUpdated() == 1);
  }

}
