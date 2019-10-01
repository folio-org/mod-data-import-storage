package org.folio.services;

import io.vertx.core.Future;
import org.folio.rest.jaxrs.model.ParsedRecordsBatchResponse;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.jaxrs.model.RecordsBatchResponse;
import org.folio.rest.jaxrs.model.SourceRecordCollection;
import org.folio.rest.jaxrs.model.SuppressFromDiscoveryDto;

import java.util.Optional;

/**
 * Record Service
 */
public interface RecordService {

  /**
   * Searches for records
   *
   * @param query    query from URL
   * @param offset   starting index in a list of results
   * @param limit    limit of records for pagination
   * @param tenantId tenant id
   * @return future with {@link RecordCollection}
   */
  Future<RecordCollection> getRecords(String query, int offset, int limit, String tenantId);

  /**
   * Searches for record by id
   *
   * @param id       Record id
   * @param tenantId tenant id
   * @return future with optional {@link Record}
   */
  Future<Optional<Record>> getRecordById(String id, String tenantId);

  /**
   * Saves record
   *
   * @param record   Record to save
   * @param tenantId tenant id
   * @return future with saved Record
   */
  Future<Record> saveRecord(Record record, String tenantId);

  /**
   * Saves collection of records
   *
   * @param recordsCollection Records to save
   * @param tenantId          tenant id
   * @return future with response containing list of successfully saved records and error messages for records that were not saved
   */
  Future<RecordsBatchResponse> saveRecords(RecordCollection recordsCollection, String tenantId);

  /**
   * Updates record with given id
   *
   * @param record   Record to update
   * @param tenantId tenant id
   * @return future with updated Record
   */
  Future<Record> updateRecord(Record record, String tenantId);

  /**
   * Searches for source records
   *
   * @param query          query from URL
   * @param offset         starting index in a list of results
   * @param limit          limit of records for pagination
   * @param deletedRecords indicates to return records marked as deleted or not
   * @param tenantId       tenant id
   * @return future with {@link SourceRecordCollection}
   */
  Future<SourceRecordCollection> getSourceRecords(String query, int offset, int limit, boolean deletedRecords, String tenantId);

  /**
   * Update parsed records from collection of records and external relations ids in one transaction
   *
   * @param recordCollection collection of records from which parsed records will be updated
   * @param tenantId         tenant id
   * @return future with response containing list of successfully updated records and error messages for records that were not updated
   */
  Future<ParsedRecordsBatchResponse> updateParsedRecords(RecordCollection recordCollection, String tenantId);

  /**
   * Searches for Record either by SRS id or external relation id
   *
   * @param externalIdIdentifier specifies of external relation id type
   * @param id             either SRS id or external relation id
   * @param tenantId       tenant id
   * @return future with {@link Record}
   */
  Future<Record> getFormattedRecord(String externalIdIdentifier, String id, String tenantId);

  /**
   * Change suppress from discovery flag for record by external relation id
   *
   * @param suppressFromDiscoveryDto - dto that contains new value and id
   * @param tenantId                 - tenant id
   * @return - future with true if succeeded
   */
  Future<Boolean> updateSuppressFromDiscoveryForRecord(SuppressFromDiscoveryDto suppressFromDiscoveryDto, String tenantId);

  /**
   * Deletes records associated with specified snapshot and snapshot itself
   *
   * @param snapshotId snapshot id
   * @param tenantId   tenant id
   * @return - future with true if succeeded
   */
  Future<Boolean> deleteRecordsBySnapshotId(String snapshotId, String tenantId);

}
