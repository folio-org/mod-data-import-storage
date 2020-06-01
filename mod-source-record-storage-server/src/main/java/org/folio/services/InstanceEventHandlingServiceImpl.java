package org.folio.services;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.folio.dao.RecordDao;
import org.folio.processing.events.utils.ZIPArchiver;
import org.folio.rest.jaxrs.model.DataImportEventPayload;
import org.folio.rest.jaxrs.model.ExternalIdsHolder;
import org.folio.rest.jaxrs.model.Record;
import org.folio.rest.tools.utils.ObjectMapperTool;
import org.folio.services.util.AdditionalFieldsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.folio.rest.jaxrs.model.EntityType.INSTANCE;
import static org.folio.rest.jaxrs.model.EntityType.MARC_BIBLIOGRAPHIC;
import static org.folio.services.util.AdditionalFieldsUtil.TAG_999;

@Component
public class InstanceEventHandlingServiceImpl implements EventHandlingService {

  private static final Logger LOG = LoggerFactory.getLogger(InstanceEventHandlingServiceImpl.class);
  private static final String FAIL_MSG = "Failed to handle instance event {}";
  private static final String PREVIOUS_RECORDS = "snapshotId<>%s AND state==ACTUAL AND externalIdsHolder.instanceId==%s AND deleted==false";
  private static final String EVENT_HAS_NO_DATA_MSG = "Failed to handle Instance event, cause event payload context does not contain INSTANCE and/or MARC_BIBLIOGRAPHIC data";

  @Autowired
  private RecordDao recordDao;

  @Override
  public Future<Boolean> handleEvent(String eventContent, String tenantId) {
    try {
      Pair<String, String> instanceRecordPair = extractPayload(eventContent);
      if (StringUtils.isEmpty(instanceRecordPair.getLeft()) || StringUtils.isEmpty(instanceRecordPair.getRight())) {
        LOG.error(EVENT_HAS_NO_DATA_MSG);
        return Future.failedFuture(EVENT_HAS_NO_DATA_MSG);
      }
      return setInstanceIdToRecord(
        ObjectMapperTool.getMapper().readValue(instanceRecordPair.getRight(), Record.class),
        new JsonObject(instanceRecordPair.getLeft()), tenantId)
        .compose(record -> updatePreviousRecords(record.getExternalIdsHolder().getInstanceId(), record.getSnapshotId(), tenantId))
        .map(true);
    } catch (IOException e) {
      LOG.error(FAIL_MSG, e, eventContent);
      return Future.failedFuture(e);
    }
  }

  private Pair<String, String> extractPayload(String eventContent) throws IOException {
    DataImportEventPayload dataImportEventPayload = ObjectMapperTool.getMapper().readValue(ZIPArchiver.unzip(eventContent), DataImportEventPayload.class);
    String instanceAsString = dataImportEventPayload.getContext().get(INSTANCE.value());
    String recordAsString = dataImportEventPayload.getContext().get(MARC_BIBLIOGRAPHIC.value());
    return Pair.of(instanceAsString, recordAsString);
  }

  private Future<Void> updatePreviousRecords(String instanceId, String snapshotId, String tenantId) {
    return recordDao.getRecords(format(PREVIOUS_RECORDS, snapshotId, instanceId), 0, 999, tenantId)
      .compose(recordCollection -> {
        Promise<Void> result = Promise.promise();
        List<Future> futures = new ArrayList<>();
        recordCollection.getRecords()
          .forEach(record -> futures.add(recordDao.updateRecord(record.withState(Record.State.OLD), tenantId)));
        CompositeFuture.all(futures).onComplete(ar -> {
          if (ar.succeeded()) {
            result.complete();
          } else {
            result.fail(ar.cause());
            LOG.error(ar.cause(), "ERROR during update old records state for instance chane event");
          }
        });
        return result.future();
      });
  }

  /**
   * Adds specified instanceId to record and additional custom field with instanceId to parsed record.
   * Updates changed record in database.
   *
   * @param record   record to update
   * @param instance instance in Json
   * @param tenantId tenant id
   * @return future with updated record
   */
  private Future<Record> setInstanceIdToRecord(Record record, JsonObject instance, String tenantId) {
    if (record.getExternalIdsHolder() == null) {
      record.setExternalIdsHolder(new ExternalIdsHolder());
    }
    if (isNotEmpty(record.getExternalIdsHolder().getInstanceId())) {
      return Future.succeededFuture(record);
    }
    String instanceId = instance.getString("id");
    boolean isAddedField = AdditionalFieldsUtil.addFieldToMarcRecord(record, TAG_999, 'i', instanceId);
    AdditionalFieldsUtil.fillHrIdFieldInMarcRecord(Pair.of(record, instance));
    if (isAddedField) {
      record.getExternalIdsHolder().setInstanceId(instanceId);
      return recordDao.updateParsedRecord(record, tenantId).map(record);
    }
    return Future.failedFuture(new RuntimeException(format("Failed to add instance id '%s' to record with id '%s'", instanceId, record.getId())));
  }
}
