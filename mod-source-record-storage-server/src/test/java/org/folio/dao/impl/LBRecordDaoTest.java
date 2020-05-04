package org.folio.dao.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.dao.LBRecordDao;
import org.folio.dao.LBSnapshotDao;
import org.folio.dao.filter.RecordFilter;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.persist.PostgresClient;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class LBRecordDaoTest extends AbstractBeanDaoTest<Record, RecordCollection, RecordFilter, LBRecordDao> {

  private LBSnapshotDao snapshotDao;

  @Override
  public void createDependentBeans(TestContext context) {
    Async async = context.async();
    snapshotDao = new LBSnapshotDaoImpl(postgresClientFactory);
    snapshotDao.save(getSnapshots(), TENANT_ID).setHandler(save -> {
      if (save.failed()) {
        context.fail(save.cause());
      }
      async.complete();
    });
  }

  @Override
  public void createDao(TestContext context) {
    dao = new LBRecordDaoImpl(postgresClientFactory);
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
  public void shouldSaveGeneratingId(TestContext context) {
    Async async = context.async();
    dao.save(getMockBeanWithoutId(), TENANT_ID).setHandler(res -> {
      if (res.failed()) {
        context.fail(res.cause());
      }
      compareBeans(context, getMockBeanWithoutId(), res.result());
      async.complete();
    });
  }

  public Record getMockBeanWithoutId() {
    return new Record()
      .withSnapshotId(getSnapshot(1).getJobExecutionId())
      .withRecordType(Record.RecordType.MARC)
      .withMatchedProfileId("f9926e86-883b-4455-a807-fc5eeb9a951a")
      .withOrder(0)
      .withGeneration(1)
      .withState(Record.State.ACTUAL);
  }

  @Override
  public RecordFilter getNoopFilter() {
    return new RecordFilter();
  }

  @Override
  public RecordFilter getArbitruaryFilter() {
    RecordFilter snapshotFilter = new RecordFilter();
    snapshotFilter.setMatchedProfileId("df7bf522-66e1-4b52-9d48-abd739f37934");
    snapshotFilter.setState(Record.State.ACTUAL);
    return snapshotFilter;
  }

  @Override
  public Record getMockBean() {
    return getRecord(0);
  }

  @Override
  public Record getInvalidMockBean() {
    String id = UUID.randomUUID().toString();
    return new Record().withId(id)
      .withRecordType(Record.RecordType.MARC)
      .withOrder(0)
      .withGeneration(1)
      .withState(Record.State.ACTUAL);
  }

  @Override
  public Record getUpdatedMockBean() {
    return new Record()
      .withId(getMockBean().getId())
      .withMatchedId(getMockBean().getMatchedId())
      .withMatchedProfileId(getMockBean().getMatchedProfileId())
      .withSnapshotId(getMockBean().getSnapshotId())
      .withGeneration(getMockBean().getGeneration())
      .withRecordType(getMockBean().getRecordType())
      .withAdditionalInfo(getMockBean().getAdditionalInfo())
      .withExternalIdsHolder(getMockBean().getExternalIdsHolder())
      .withMetadata(getMockBean().getMetadata())
      .withState(Record.State.DRAFT)
      .withOrder(2);
  }

  @Override
  public List<Record> getMockBeans() {
    return getRecords();
  }

  @Override
  public void compareBeans(TestContext context, Record expected, Record actual) {
    if (StringUtils.isEmpty(expected.getId())) {
      context.assertNotNull(actual.getId());
    } else {
      context.assertEquals(expected.getId(), actual.getId());
    }
    if (StringUtils.isEmpty(expected.getMatchedId())) {
      context.assertNotNull(actual.getMatchedId());
    } else {
      context.assertEquals(expected.getMatchedId(), actual.getMatchedId());
    }
    context.assertEquals(expected.getSnapshotId(), actual.getSnapshotId());
    context.assertEquals(expected.getMatchedProfileId(), actual.getMatchedProfileId());
    context.assertEquals(expected.getGeneration(), actual.getGeneration());
    context.assertEquals(expected.getOrder(), actual.getOrder());
    context.assertEquals(expected.getState(), actual.getState());
    context.assertEquals(expected.getRecordType(), actual.getRecordType());
    if (expected.getAdditionalInfo() != null) {
      context.assertEquals(expected.getAdditionalInfo().getSuppressDiscovery(), actual.getAdditionalInfo().getSuppressDiscovery());
    }
    if (expected.getExternalIdsHolder() != null) {
      context.assertEquals(expected.getExternalIdsHolder().getInstanceId(), actual.getExternalIdsHolder().getInstanceId());
    }
    if (expected.getMetadata() != null) {
      context.assertEquals(expected.getMetadata().getCreatedByUserId(), actual.getMetadata().getCreatedByUserId());
      context.assertNotNull(actual.getMetadata().getCreatedDate());
      context.assertEquals(expected.getMetadata().getUpdatedByUserId(), actual.getMetadata().getUpdatedByUserId());
      context.assertNotNull(actual.getMetadata().getUpdatedDate());
    }
  }

  @Override
  public void assertNoopFilterResults(TestContext context, RecordCollection actual) {
    List<Record> expected = getMockBeans();
    context.assertEquals(new Integer(expected.size()), actual.getTotalRecords());
    expected.forEach(expectedRecord -> context.assertTrue(actual.getRecords().stream()
      .anyMatch(actualRecord -> actualRecord.getId().equals(expectedRecord.getId()))));
  }

  @Override
  public void assertArbitruaryFilterResults(TestContext context, RecordCollection actual) {
    List<Record> expected = getMockBeans().stream()
      .filter(bean -> bean.getState().equals(getArbitruaryFilter().getState()) &&
        bean.getMatchedProfileId().equals(getArbitruaryFilter().getMatchedProfileId()))
      .collect(Collectors.toList());
    context.assertEquals(new Integer(expected.size()), actual.getTotalRecords());
    expected.forEach(expectedRecord -> context.assertTrue(actual.getRecords().stream()
      .anyMatch(actualRecord -> actualRecord.getId().equals(expectedRecord.getId()))));
  }

}