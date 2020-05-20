package org.folio.services;

import org.folio.dao.query.RecordQuery;
import org.folio.rest.jaxrs.model.ParsedRecordDto;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.jaxrs.model.RecordCollection;
import org.folio.rest.jaxrs.model.RecordsBatchResponse;
import org.folio.rest.jaxrs.model.SuppressFromDiscoveryDto;

import io.vertx.core.Future;

/**
 * {@link Record} service
 */
public interface LBRecordService extends EntityService<Record, RecordCollection, RecordQuery> {

  /**
   * Saves collection of records
   *
   * @param recordCollection records to save
   * @param tenantId         tenant id
   * @return future with response containing list of successfully saved records and error messages for records that were not saved
   */
  Future<RecordsBatchResponse> saveRecords(RecordCollection recordCollection, String tenantId);

  /**
   * Searches for Record either by SRS id or external relation id
   *
   * @param externalIdIdentifier specifies of external relation id type
   * @param id                   either SRS id or external relation id
   * @param tenantId             tenant id
   * @return future with {@link Record}
   */
  Future<Record> getFormattedRecord(String externalIdIdentifier, String id, String tenantId);

  /**
   * Change suppress from discovery flag for record by external relation id
   *
   * @param suppressFromDiscoveryDto dto that contains new value and id
   * @param tenantId                 tenant id
   * @return - future with record if succeeded
   */
  Future<Record> updateSuppressFromDiscoveryForRecord(SuppressFromDiscoveryDto suppressFromDiscoveryDto, String tenantId);

  /**
   * Creates new updated Record with incremented generation linked to a new Snapshot, and sets OLD status to the "old" Record,
   * no data is deleted as a result of the update
   *
   * @param parsedRecordDto parsed record DTO containing updates to parsed record
   * @param snapshotId      snapshot id to which new Record should be linked
   * @param tenantId        tenant id
   * @return future with updated Record
   */
  Future<Record> updateSourceRecord(ParsedRecordDto parsedRecordDto, String snapshotId, String tenantId);

}