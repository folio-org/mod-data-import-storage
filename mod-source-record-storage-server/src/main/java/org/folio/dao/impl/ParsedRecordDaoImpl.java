package org.folio.dao.impl;

import static org.folio.dao.util.DaoUtil.CONTENT_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.ID_COLUMN_NAME;
import static org.folio.dao.util.DaoUtil.PARSED_RECORDS_TABLE_NAME;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

import org.folio.dao.AbstractEntityDao;
import org.folio.dao.ParsedRecordDao;
import org.folio.dao.query.ParsedRecordQuery;
import org.folio.dao.util.ColumnBuilder;
import org.folio.dao.util.DaoUtil;
import org.folio.dao.util.MarcUtil;
import org.folio.rest.jaxrs.model.ParsedRecord;
import org.folio.rest.jaxrs.model.ParsedRecordCollection;
import org.springframework.stereotype.Component;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;

// <createTable tableName="marc_records_lb">
//   <column name="id" type="uuid">
//     <constraints primaryKey="true" nullable="false"/>
//   </column>
//   <column name="content" type="jsonb">
//     <constraints nullable="false"/>
//   </column>
// </createTable>
@Component
public class ParsedRecordDaoImpl extends AbstractEntityDao<ParsedRecord, ParsedRecordCollection, ParsedRecordQuery> implements ParsedRecordDao {

  @Override
  public String getTableName() {
    return PARSED_RECORDS_TABLE_NAME;
  }

  @Override
  public String getColumns() {
    return ColumnBuilder.of(ID_COLUMN_NAME)
      .append(CONTENT_COLUMN_NAME)
      .build();
  }

  @Override
  public String getId(ParsedRecord parsedRecord) {
    return parsedRecord.getId();
  }

  @Override
  protected JsonArray toParams(ParsedRecord parsedRecord, boolean generateIdIfNotExists) {
    // NOTE: ignoring generateIdIfNotExists, id is required
    // error_records id is foreign key with records_lb
    return new JsonArray()
      .add(parsedRecord.getId())
      .add(parsedRecord.getContent());
  }

  @Override
  protected ParsedRecordCollection toCollection(ResultSet resultSet) {
    return toEmptyCollection(resultSet)
      .withParsedRecords(resultSet.getRows().stream().map(this::toEntity).collect(Collectors.toList()));
  }

  @Override
  protected ParsedRecordCollection toEmptyCollection(ResultSet resultSet) {
    return new ParsedRecordCollection()
      .withParsedRecords(Collections.emptyList())
      .withTotalRecords(DaoUtil.getTotalRecords(resultSet));
  }

  @Override
  protected ParsedRecord toEntity(JsonObject result) {
    String content = result.getString(CONTENT_COLUMN_NAME);
    return formatContent(new ParsedRecord()
      .withId(result.getString(ID_COLUMN_NAME))
      .withContent(content));
  }

  @Override
  protected ParsedRecord toEntity(JsonArray row) {
    String content = row.getString(1);
    return formatContent(new ParsedRecord()
      .withId(row.getString(0))
      .withContent(content));
  }

  @Override
  protected ParsedRecord postSave(ParsedRecord parsedRecord) {
    return formatContent(parsedRecord);
  }

  @Override
  protected ParsedRecord postUpdate(ParsedRecord parsedRecord) {
    return formatContent(parsedRecord);
  }

  private ParsedRecord formatContent(ParsedRecord parsedRecord) {
    try {
      String formattedContent = MarcUtil.marcJsonToTxtMarc((String) parsedRecord.getContent());
      parsedRecord.withFormattedContent(formattedContent);
    } catch (IOException e) {
      log.error("Error formatting content", e);
    }
    return parsedRecord;
  }

}