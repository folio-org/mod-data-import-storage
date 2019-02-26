package org.folio.dao;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.jaxrs.model.SourceRecord;
import org.folio.rest.jaxrs.model.SourceRecordCollection;

import java.util.Optional;

/**
 * Data access object for {@link Record}
 */
public interface RecordDao {

  /**
   * Searches for {@link Record} in the db view
   *
   * @param query  query string to filter records based on matching criteria in fields
   * @param offset starting index in a list of results
   * @param limit  maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link RecordCollection}
   */
  Future<RecordCollection> getRecords(String query, int offset, int limit, String tenantId);

  /**
   * Searches for {@link Record} by id
   *
   * @param id Record id
   * @param tenantId tenant id
   * @return future with optional {@link Record}
   */
  Future<Optional<Record>> getRecordById(String id, String tenantId);

  /**
   * Saves {@link Record} to the db
   *
   * @param record {@link Record} to save
   * @param tenantId tenant id
   * @return future with true if succeeded
   */
  Future<Boolean> saveRecord(Record record, String tenantId);

  /**
   * Updates {{@link Record} in the db
   *
   * @param record {@link Record} to update
   * @param tenantId tenant id
   * @return future with true if succeeded
   */
  Future<Boolean> updateRecord(Record record, String tenantId);

  /**
   * Deletes {@link Record} from the db
   *
   * @param id id of the {@link Record} to delete
   * @param tenantId tenant id
   * @return future with true if succeeded
   */
  Future<Boolean> deleteRecord(String id, String tenantId);

  /**
   * Searches for {@link SourceRecord} in the db view
   *
   * @param query  query string to filter results based on matching criteria in fields
   * @param offset starting index in a list of results
   * @param limit  maximum number of results to return
   * @param tenantId tenant id
   * @return future with {@link SourceRecordCollection}
   */
  Future<SourceRecordCollection> getSourceRecords(String query, int offset, int limit, String tenantId);

}
