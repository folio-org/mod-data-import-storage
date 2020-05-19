package org.folio;

import java.util.Collections;
import java.util.List;

import org.folio.dao.query.RawRecordQuery;
import org.folio.rest.jaxrs.model.RawRecord;
import org.folio.rest.jaxrs.model.RawRecordCollection;

import io.vertx.ext.unit.TestContext;

public class RawRecordMocks implements EntityMocks<RawRecord, RawRecordCollection, RawRecordQuery> {

  private RawRecordMocks() { }

  @Override
  public String getId(RawRecord rawRecord) {
    return rawRecord.getId();
  }

  @Override
  public RawRecordQuery getNoopQuery() {
    return RawRecordQuery.query();
  }

  @Override
  public RawRecordQuery getArbitruaryQuery() {
    // NOTE: no reasonable field to filter on
    return RawRecordQuery.query();
  }

  @Override
  public RawRecordQuery getArbitruarySortedQuery() {
    return RawRecordQuery.query().builder()
      .orderBy("id")
      .query();
  }

  @Override
  public RawRecord getMockEntity() {
    return TestMocks.getRawRecord(0);
  }

  @Override
  public RawRecord getInvalidMockEntity() {
    return new RawRecord()
      .withId(TestMocks.getRecord(0).getId());
  }

  @Override
  public RawRecord getUpdatedMockEntity() {
    return new RawRecord()
      .withId(getMockEntity().getId())
      .withContent(getMockEntity().getContent());
  }

  @Override
  public List<RawRecord> getMockEntities() {
    return TestMocks.getRawRecords();
  }

  @Override
  public RawRecord getExpectedEntity() {
    return getMockEntity();
  }

  @Override
  public RawRecord getExpectedUpdatedEntity() {
    return getUpdatedMockEntity();
  }

  @Override
  public List<RawRecord> getExpectedEntities() {
    return getMockEntities();
  }

  @Override
  public List<RawRecord> getExpectedEntitiesForArbitraryQuery() {
    return getExpectedEntities();
  }

  @Override
  public List<RawRecord> getExpectedEntitiesForArbitrarySortedQuery() {
    List<RawRecord> expected = getExpectedEntitiesForArbitraryQuery();
    Collections.sort(expected, (rr1, rr2) -> rr1.getId().compareTo(rr2.getId()));
    return expected;
  }

  @Override
  public RawRecordCollection getExpectedCollection() {
    List<RawRecord> expected = getExpectedEntities();
    return new RawRecordCollection()
      .withRawRecords(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public RawRecordCollection getExpectedCollectionForArbitraryQuery() {
    List<RawRecord> expected = getExpectedEntitiesForArbitraryQuery();
    return new RawRecordCollection()
      .withRawRecords(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public RawRecordCollection getExpectedCollectionForArbitrarySortedQuery() {
    List<RawRecord> expected = getExpectedEntitiesForArbitrarySortedQuery();
    return new RawRecordCollection()
      .withRawRecords(expected)
      .withTotalRecords(expected.size());
  }

  @Override
  public void assertEmptyResult(TestContext context, int expectedTotal, RawRecordCollection actual) {
    context.assertEquals(new Integer(expectedTotal), actual.getTotalRecords());
    context.assertTrue(actual.getRawRecords().isEmpty());
  }

  @Override
  public void compareCollections(TestContext context, RawRecordCollection expected, RawRecordCollection actual) {
    context.assertEquals(expected.getTotalRecords(), actual.getTotalRecords());
    compareEntities(context, expected.getRawRecords(), actual.getRawRecords(), true);
  }

  @Override
  public void compareEntities(TestContext context, RawRecord expected, RawRecord actual) {
    context.assertEquals(expected.getId(), actual.getId());
    context.assertEquals(expected.getContent(), actual.getContent());
  }

  public static RawRecordMocks mock() {
    return new RawRecordMocks();
  }

}