package org.folio.dao;

import java.util.Date;
import java.util.Optional;

import org.folio.dao.query.RecordQuery;
import org.folio.dao.util.SourceRecordContent;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.SourceRecord;
import org.folio.rest.jaxrs.model.SourceRecordCollection;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowStream;

/**
 * Data access object for {@link SourceRecord}
 */
public interface SourceRecordDao {

  /**
   * Searches for {@link SourceRecord} by matched id of {@link Record}
   * 
   * @param id       record id
   * @param tenantId tenant id
   * @return future with optional {@link SourceRecord} with {@link ParsedRecord} only
   */
  public Future<Optional<SourceRecord>> getSourceMarcRecordById(String id, String tenantId);

  /**
   * Searches for {@link SourceRecord} by matched id of {@link Record} with latest generation
   * 
   * @param id       record id
   * @param tenantId tenant id
   * @return future with optional {@link SourceRecord} with {@link ParsedRecord} only
   */
  public Future<Optional<SourceRecord>> getSourceMarcRecordByIdAlt(String id, String tenantId);

  /**
   * Searches for {@link SourceRecord} by {@link ExternalIdsHolder} instance id of {@link Record}
   * 
   * @param id       record instance id
   * @param tenantId tenant id
   * @return future with optional {@link SourceRecord} with {@link ParsedRecord} only
   */
  public Future<Optional<SourceRecord>> getSourceMarcRecordByInstanceId(String instanceId, String tenantId);

  /**
   * Searches for {@link SourceRecord} by {@link ExternalIdsHolder} instance id of {@link Record} with latest generation
   * 
   * @param id       record instance id
   * @param tenantId tenant id
   * @return future with optional {@link SourceRecord} with {@link ParsedRecord} only
   */
  public Future<Optional<SourceRecord>> getSourceMarcRecordByInstanceIdAlt(String instanceId, String tenantId);

  /**
   * Searches for collection of {@link SourceRecord} with {@link Record.State} of 'ACTUAL'
   * 
   * @param offset   starting index in a list of results
   * @param limit    maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link SourceRecordCollection} with list of {@link ParsedRecord} only
   */
  public Future<SourceRecordCollection> getSourceMarcRecords(Integer offset, Integer limit, String tenantId);

  /**
   * Searches for collection of {@link SourceRecord} with {@link Record.State} of 'ACTUAL' and latest generation
   * 
   * @param offset   starting index in a list of results
   * @param limit    maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link SourceRecordCollection} with list of {@link ParsedRecord} only
   */
  public Future<SourceRecordCollection> getSourceMarcRecordsAlt(Integer offset, Integer limit, String tenantId);

  /**
   * Searches for collection of {@link SourceRecord} for a given period with {@link Record.State} of 'ACTUAL'
   * 
   * @param from     starting updated date
   * @param till     ending updated date
   * @param offset   starting index in a list of results
   * @param limit    maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link SourceRecordCollection} with list of {@link ParsedRecord} only
   */
  public Future<SourceRecordCollection> getSourceMarcRecordsForPeriod(Date from, Date till, Integer offset, Integer limit, String tenantId);

  /**
   * Searches for collection of {@link SourceRecord} for a given period with {@link Record.State} of 'ACTUAL' and latest generation
   * 
   * @param from     starting updated date
   * @param till     ending updated date
   * @param offset   starting index in a list of results
   * @param limit    maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link SourceRecordCollection} with list of {@link ParsedRecord} only
   */
  public Future<SourceRecordCollection> getSourceMarcRecordsForPeriodAlt(Date from, Date till, Integer offset, Integer limit, String tenantId);

  /**
   * Searches for collection of {@link SourceRecord} by {@link RecordQuery} and streams response
   * 
   * @param content    specific {@link SourceRecordContent}
   * @param query      {@link RecordQuery} to prepare WHERE clause
   * @param offset     starting index in a list of results
   * @param limit      maximum number of results to return
   * @param tenantId   tenant id
   * @param handler    handler for the {@link RowStream}
   * @param endHandler handler for when stream is finished
   */
  public void getSourceMarcRecordsByQuery(SourceRecordContent content, RecordQuery query, Integer offset, Integer limit, String tenantId,
      Handler<RowStream<Row>> handler, Handler<AsyncResult<Void>> endHandler);

  /**
   * Map {@link Row} to {@link SourceRecord} by available properties from {@link Record}
   * 
   * @param row row of result set
   * @return mapped source record
   */
  public SourceRecord toSourceRecord(Row row);

}