package org.folio;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.folio.dao.query.SnapshotQuery;
import org.folio.rest.jaxrs.model.Snapshot;
import org.folio.rest.jaxrs.model.SnapshotCollection;
import org.springframework.beans.BeanUtils;

import io.vertx.ext.unit.TestContext;

public class LBSnapshotMocks implements EntityMocks<Snapshot, SnapshotCollection, SnapshotQuery> {

  private LBSnapshotMocks() { }

  @Override
  public String getId(Snapshot snapshot) {
    return snapshot.getJobExecutionId();
  }

  @Override
  public SnapshotQuery getNoopQuery() {
    return new SnapshotQuery();
  }

  @Override
  public SnapshotQuery getArbitruaryQuery() {
    SnapshotQuery snapshotQuery = new SnapshotQuery();
    snapshotQuery.setStatus(Snapshot.Status.NEW);
    return snapshotQuery;
  }

  @Override
  public SnapshotQuery getArbitruarySortedQuery() {
    SnapshotQuery snapshotQuery = new SnapshotQuery();
    snapshotQuery.setStatus(Snapshot.Status.NEW);
    snapshotQuery.orderBy("status");
    return snapshotQuery;
  }

  @Override
  public SnapshotQuery getCompleteQuery() {
    SnapshotQuery query = new SnapshotQuery();
    BeanUtils.copyProperties(TestMocks.getSnapshot("6681ef31-03fe-4abc-9596-23de06d575c5").get(), query);
    query.withProcessingStartedDate(null);
    return query;
  }

  @Override
  public Snapshot getMockEntity() {
    return TestMocks.getSnapshot(0);
  }

  @Override
  public Snapshot getInvalidMockEntity() {
    return new Snapshot()
      .withJobExecutionId("f3ba7619-d9b6-4e7d-9ebf-587d2d3807d0");
  }

  @Override
  public Snapshot getUpdatedMockEntity() {
    return new Snapshot()
      .withJobExecutionId(getMockEntity().getJobExecutionId())
      .withStatus(Snapshot.Status.PARSING_IN_PROGRESS)
      .withProcessingStartedDate(new Date(1589218979000l));
  }

  @Override
  public List<Snapshot> getMockEntities() {
    return TestMocks.getSnapshots();
  }

  @Override
  public String getCompleteWhereClause() {
    return "WHERE id = '6681ef31-03fe-4abc-9596-23de06d575c5'" +
      " AND status = 'PROCESSING_IN_PROGRESS'";
  }

  @Override
  public Snapshot getExpectedEntity() {
    return getMockEntity();
  }

  @Override
  public Snapshot getExpectedUpdatedEntity() {
    return getUpdatedMockEntity();
  }

  @Override
  public List<Snapshot> getExpectedEntities() {
    return getMockEntities();
  }

  @Override
  public List<Snapshot> getExpectedEntitiesForArbitraryQuery() {
    return getExpectedEntities().stream()
      .filter(entity -> entity.getStatus().equals(getArbitruaryQuery().getStatus()))
      .collect(Collectors.toList());
  }

  @Override
  public List<Snapshot> getExpectedEntitiesForArbitrarySortedQuery() {
    List<Snapshot> expected = getExpectedEntitiesForArbitraryQuery();
    Collections.sort(expected, (s1, s2) -> s1.getStatus().compareTo(s2.getStatus()));
    return expected;
  }

  @Override
  public SnapshotCollection getExpectedCollection() {
    List<Snapshot> expected = getExpectedEntities();
    return new SnapshotCollection()
      .withSnapshots(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public SnapshotCollection getExpectedCollectionForArbitraryQuery() {
    List<Snapshot> expected = getExpectedEntitiesForArbitraryQuery();
    return new SnapshotCollection()
      .withSnapshots(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public SnapshotCollection getExpectedCollectionForArbitrarySortedQuery() {
    List<Snapshot> expected = getExpectedEntitiesForArbitrarySortedQuery();
    return new SnapshotCollection()
      .withSnapshots(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public void compareCollections(TestContext context, SnapshotCollection expected, SnapshotCollection actual) {
    context.assertEquals(expected.getTotalRecords(), actual.getTotalRecords());
    compareEntities(context, expected.getSnapshots(), actual.getSnapshots());
  }

  @Override
  public void compareEntities(TestContext context, Snapshot expected, Snapshot actual) {
    if (StringUtils.isEmpty(expected.getJobExecutionId())) {
      context.assertNotNull(actual.getJobExecutionId());
    } else {
      context.assertEquals(expected.getJobExecutionId(), actual.getJobExecutionId());
    }
    context.assertEquals(expected.getStatus(), actual.getStatus());
    if (Objects.nonNull(expected.getProcessingStartedDate())) {
      context.assertEquals(expected.getProcessingStartedDate().getTime(), 
        actual.getProcessingStartedDate().getTime());
    }
  }

  public static LBSnapshotMocks mock() {
    return new LBSnapshotMocks();
  }

}