package org.folio.dao;

import org.folio.dao.query.RawRecordQuery;
import org.folio.rest.jaxrs.model.RawRecord;
import org.folio.rest.jaxrs.model.RawRecordCollection;

/**
 * Data access object for {@link RawRecord}
 */
public interface RawRecordDao extends EntityDao<RawRecord, RawRecordCollection, RawRecordQuery> {

}