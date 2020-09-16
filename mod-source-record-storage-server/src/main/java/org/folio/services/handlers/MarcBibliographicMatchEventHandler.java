package org.folio.services.handlers;

import static org.folio.dao.util.RecordDaoUtil.filterRecordByInstanceHrid;
import static org.folio.dao.util.RecordDaoUtil.filterRecordByInstanceId;
import static org.folio.dao.util.RecordDaoUtil.filterRecordByRecordId;
import static org.folio.rest.jaxrs.model.EntityType.MARC_BIBLIOGRAPHIC;
import static org.folio.rest.jaxrs.model.MatchExpression.DataValueType.VALUE_FROM_RECORD;
import static org.folio.rest.jaxrs.model.ProfileSnapshotWrapper.ContentType.MATCH_PROFILE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.folio.DataImportEventPayload;
import org.folio.MatchDetail;
import org.folio.MatchProfile;
import org.folio.dao.RecordDao;
import org.folio.processing.events.services.handler.EventHandler;
import org.folio.processing.exceptions.EventProcessingException;
import org.folio.processing.exceptions.MatchingException;
import org.folio.processing.matching.reader.util.MarcValueReaderUtil;
import org.folio.processing.value.Value;
import org.folio.rest.jaxrs.model.EntityType;
import org.folio.rest.jaxrs.model.Field;
import org.folio.rest.jaxrs.model.MatchExpression;
import org.folio.rest.jaxrs.model.ProfileSnapshotWrapper;
import org.jooq.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@Component
public class MarcBibliographicMatchEventHandler implements EventHandler {

  private static final Logger LOG = LoggerFactory.getLogger(MarcBibliographicMatchEventHandler.class);
  private static final String EVENT_HAS_NO_DATA_MSG = "Failed to handle Instance event, cause event payload context does not contain INSTANCE and/or MARC_BIBLIOGRAPHIC data";
  private static final String MATCHED_ID_MARC_FIELD = "999ffs";
  private static final String INSTANCE_ID_MARC_FIELD = "999ffi";
  private static final String INSTANCE_HRID_MARC_FIELD = "001";
  private static final String FOUND_MULTIPLE_RECORDS_ERROR_MESSAGE = "Found multiple records matching specified conditions";
  private static final String CANNOT_FIND_RECORDS_ERROR_MESSAGE = "Can`t find records matching specified conditions";
  private static final String CANNOT_FIND_RECORDS_FOR_MARC_FIELD_ERROR_MESSAGE = "Can`t find records by this MARC-field path: %s";

  private final RecordDao recordDao;

  @Autowired
  public MarcBibliographicMatchEventHandler(final RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  @Override
  public CompletableFuture<DataImportEventPayload> handle(DataImportEventPayload dataImportEventPayload) {
    CompletableFuture<DataImportEventPayload> future = new CompletableFuture<>();
    HashMap<String, String> context = dataImportEventPayload.getContext();
    dataImportEventPayload.getEventsChain().add(dataImportEventPayload.getEventType());
    String recordAsString = context.get(MARC_BIBLIOGRAPHIC.value());
    if (StringUtils.isEmpty(recordAsString)) {
      LOG.error(EVENT_HAS_NO_DATA_MSG);
      future.completeExceptionally(new EventProcessingException(EVENT_HAS_NO_DATA_MSG));
      return future;
    }

    MatchDetail matchDetail = retrieveMatchDetail(dataImportEventPayload);
    String valueFromField = retrieveValueFromMarcFile(recordAsString, matchDetail.getExistingMatchExpression());
    MatchExpression matchExpression = matchDetail.getExistingMatchExpression();

    Condition condition = null;
    String marcFieldPath = null;
    if (matchExpression != null && matchExpression.getDataValueType() == VALUE_FROM_RECORD) {
      List<Field> fields = matchExpression.getFields();
      if (fields != null && matchDetail.getIncomingRecordType() == EntityType.MARC_BIBLIOGRAPHIC
        && matchDetail.getExistingRecordType() == EntityType.MARC_BIBLIOGRAPHIC) {
        marcFieldPath = fields.stream().map(field -> field.getValue().trim()).collect(Collectors.joining());
        condition = buildConditionBasedOnMarcField(valueFromField, marcFieldPath);
      }
    }

    if (condition != null) {
      recordDao.getRecords(condition, new ArrayList<>(), 0, 999, dataImportEventPayload.getTenant())
        .onComplete(ar -> {
          if (ar.succeeded()) {
            processSucceededResult(dataImportEventPayload, future, context, ar);
          } else {
            future.completeExceptionally(new MatchingException(ar.cause()));
          }
        });
    } else {
      constructError(dataImportEventPayload, String.format(CANNOT_FIND_RECORDS_FOR_MARC_FIELD_ERROR_MESSAGE, marcFieldPath));
    }
    return future;
  }

  @Override
  public boolean isEligible(DataImportEventPayload dataImportEventPayload) {
    if (dataImportEventPayload.getCurrentNode() != null && MATCH_PROFILE == dataImportEventPayload.getCurrentNode().getContentType()) {
      MatchProfile matchProfile = JsonObject.mapFrom(dataImportEventPayload.getCurrentNode().getContent()).mapTo(MatchProfile.class);
      return matchProfile.getIncomingRecordType() == MARC_BIBLIOGRAPHIC;
    }
    return false;
  }

  private String retrieveValueFromMarcFile(String recordAsString, MatchExpression existingMatchExpression) {
    String valueFromField = StringUtils.EMPTY;
    Value value = MarcValueReaderUtil.readValueFromRecord(recordAsString, existingMatchExpression);
    if (value.getType() == Value.ValueType.STRING) {
      valueFromField = String.valueOf(value.getValue());
    }
    return valueFromField;
  }

  private MatchDetail retrieveMatchDetail(DataImportEventPayload dataImportEventPayload) {
    MatchProfile matchProfile;
    ProfileSnapshotWrapper matchingProfileWrapper = dataImportEventPayload.getCurrentNode();
    if (matchingProfileWrapper.getContent() instanceof Map) {
      matchProfile = new JsonObject((Map) matchingProfileWrapper.getContent()).mapTo(MatchProfile.class);
    } else {
      matchProfile = (MatchProfile) matchingProfileWrapper.getContent();
    }
    return matchProfile.getMatchDetails().get(0);
  }

  private Condition buildConditionBasedOnMarcField(String valueFromField, String result) {
    Condition condition;
    switch (result) {
      case MATCHED_ID_MARC_FIELD:
        condition = filterRecordByRecordId(valueFromField);
        break;
      case INSTANCE_ID_MARC_FIELD:
        condition = filterRecordByInstanceId(valueFromField);
        break;
      case INSTANCE_HRID_MARC_FIELD:
        condition = filterRecordByInstanceHrid(valueFromField);
        break;
      default:
        condition = null;
    }
    return condition;
  }

  private void processSucceededResult(DataImportEventPayload dataImportEventPayload, CompletableFuture<DataImportEventPayload> future, HashMap<String, String> context, io.vertx.core.AsyncResult<org.folio.rest.jaxrs.model.RecordCollection> ar) {
    if (ar.result().getTotalRecords() == 1) {
      dataImportEventPayload.setEventType(DI_SRS_MARC_BIB_MATCHED.toString());
      context.put(EntityType.MARC_BIBLIOGRAPHIC.value(), Json.encode(ar.result().getRecords().get(0)));
      future.complete(dataImportEventPayload);
    } else if (ar.result().getTotalRecords() > 1) {
      constructError(dataImportEventPayload, FOUND_MULTIPLE_RECORDS_ERROR_MESSAGE);
    } else if (ar.result().getTotalRecords() == 0) {
      constructError(dataImportEventPayload, CANNOT_FIND_RECORDS_ERROR_MESSAGE);
    }
  }

  private void constructError(DataImportEventPayload dataImportEventPayload, String errorMessage) {
    LOG.error(errorMessage);
    dataImportEventPayload.setEventType(DI_SRS_MARC_BIB_NOT_MATCHED.toString());
  }
}