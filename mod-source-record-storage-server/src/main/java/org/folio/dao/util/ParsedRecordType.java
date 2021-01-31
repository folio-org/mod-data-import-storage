package org.folio.dao.util;

import java.util.UUID;

import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.Record;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.LoaderOptionsStep;
import org.jooq.Record2;

/**
 * Interface for operations with separate parsed record tables.
 */
public interface ParsedRecordType {

  void formatRecord(Record record) throws FormatRecordException;

  Condition getRecordImplicitCondition();

  Condition getSourceRecordImplicitCondition();

  Record2<UUID, JSONB> toDatabaseRecord2(ParsedRecord parsedRecord);

  @SuppressWarnings("squid:S1452")
  LoaderOptionsStep<? extends Record2<UUID, JSONB>> toLoaderOptionsStep(DSLContext dsl);

}
