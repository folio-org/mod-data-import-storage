package org.folio.dao.query;

import static org.folio.dao.util.DaoUtil.ID_COLUMN_NAME;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.folio.dao.util.DaoUtil;
import org.folio.dao.util.WhereClauseBuilder;
import org.folio.rest.jaxrs.model.RawRecord;

public class RawRecordQuery extends RawRecord implements EntityQuery {

  private final Set<OrderBy> sort = new HashSet<>();

  private final Map<String, String> propertyToColumn;

  public RawRecordQuery() {
    propertyToColumn = ImmutableMap.copyOf(DaoUtil.contentPropertyToMap());
  }

  @Override
  public Set<OrderBy> getSort() {
    return sort;
  }

  @Override
  public Map<String, String> getPropertyToColumn() {
    return propertyToColumn;
  }

  @Override
  public String toWhereClause() {
    return WhereClauseBuilder.of()
      .append(getId(), ID_COLUMN_NAME)
      .build();
  }

  @Override
  public boolean equals(Object other) {
    return DaoUtil.equals(this, other) && super.equals(other);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}