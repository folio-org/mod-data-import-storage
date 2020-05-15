package org.folio.dao.impl;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.folio.LBRecordMocks;
import org.folio.TestMocks;
import org.folio.dao.LBRecordDao;
import org.folio.dao.LBSnapshotDao;
import org.folio.dao.query.RecordQuery;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class LBRecordDaoTest extends AbstractEntityDaoTest<Record, RecordCollection, RecordQuery, LBRecordDao, LBRecordMocks> {

  private LBSnapshotDao snapshotDao;

  @Override
  public void createDao(TestContext context) throws IllegalAccessException {
    snapshotDao = new LBSnapshotDaoImpl();
    FieldUtils.writeField(snapshotDao, "postgresClientFactory", postgresClientFactory, true);
    dao = new LBRecordDaoImpl();
    FieldUtils.writeField(dao, "postgresClientFactory", postgresClientFactory, true);
  }

  @Override
  public void createDependentEntities(TestContext context) throws IllegalAccessException {
    Async async = context.async();
    snapshotDao.save(TestMocks.getSnapshots(), TENANT_ID).onComplete(save -> {
      if (save.failed()) {
        context.fail(save.cause());
      }
      async.complete();
    });
  }

  @Override
  public void clearTables(TestContext context) {
    Async async = context.async();
    PostgresClient pgClient = PostgresClient.getInstance(vertx, TENANT_ID);
    String sql = String.format(DELETE_SQL_TEMPLATE, dao.getTableName());
    pgClient.execute(sql, delete -> {
      if (delete.failed()) {
        context.fail(delete.cause());
      }
      String snapshotSql = String.format(DELETE_SQL_TEMPLATE, snapshotDao.getTableName());
      pgClient.execute(snapshotSql, snapshotDelete -> {
        if (snapshotDelete.failed()) {
          context.fail(snapshotDelete.cause());
        }
        async.complete();
      });
    });
  }

  @Test
  public void shouldGetByMatchedId(TestContext context) {
    Async async = context.async();
    dao.save(mocks.getMockEntity(), TENANT_ID).onComplete(save -> {
      if (save.failed()) {
        context.fail(save.cause());
      }
      dao.getByMatchedId(mocks.getMockEntity().getMatchedId(), TENANT_ID).onComplete(res -> {
        if (res.failed()) {
          context.fail(res.cause());
        }
        context.assertTrue(res.result().isPresent());
        mocks.compareEntities(context, mocks.getExpectedEntity(), res.result().get());
        async.complete();
      });
    });
  }

  @Test
  public void shouldGetByInstanceId(TestContext context) {
    Async async = context.async();
    dao.save(mocks.getMockEntity(), TENANT_ID).onComplete(save -> {
      if (save.failed()) {
        context.fail(save.cause());
      }
      dao.getByInstanceId(mocks.getMockEntity().getExternalIdsHolder().getInstanceId(), TENANT_ID).onComplete(res -> {
        if (res.failed()) {
          context.fail(res.cause());
        }
        context.assertTrue(res.result().isPresent());
        mocks.compareEntities(context, mocks.getExpectedEntity(), res.result().get());
        async.complete();
      });
    });
  }

  @Test
  public void shouldSaveGeneratingId(TestContext context) {
    Async async = context.async();
    dao.save(getMockEntityWithoutId(), TENANT_ID).onComplete(res -> {
      if (res.failed()) {
        context.fail(res.cause());
      }
      mocks.compareEntities(context, getMockEntityWithoutId(), res.result());
      async.complete();
    });
  }

  public Record getMockEntityWithoutId() {
    return new Record()
      .withSnapshotId(TestMocks.getSnapshot(1).getJobExecutionId())
      .withRecordType(Record.RecordType.MARC)
      .withMatchedProfileId("f9926e86-883b-4455-a807-fc5eeb9a951a")
      .withOrder(0)
      .withGeneration(1)
      .withState(Record.State.ACTUAL);
  }

  @Override
  public LBRecordMocks initMocks() {
    return LBRecordMocks.mock();
  }

}