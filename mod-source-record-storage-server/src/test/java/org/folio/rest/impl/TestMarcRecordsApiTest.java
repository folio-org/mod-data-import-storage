package org.folio.rest.impl;

import java.util.Collections;

import org.apache.http.HttpStatus;
import org.folio.TestMocks;
import org.folio.rest.jaxrs.model.RawRecord;
import org.folio.rest.jaxrs.model.TestMarcRecordsCollection;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.restassured.RestAssured;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class TestMarcRecordsApiTest extends AbstractRestVerticleTest {

  private static final String POPULATE_TEST_MARK_RECORDS_PATH = "/source-storage/populate-test-marc-records";

  @Test
  public void shouldReturnNoContentOnPostRecordCollectionPassedInBody() {
    RestAssured.given()
      .spec(spec)
      .body(new TestMarcRecordsCollection()
        .withRawRecords(TestMocks.getRawRecords()))
      .when()
      .post(POPULATE_TEST_MARK_RECORDS_PATH)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);
  }

  @Test
  public void shouldReturnUnprocessableEntityOnPostWhenNoRecordCollectionPassedInBody() {
    TestMarcRecordsCollection testMarcRecordsCollection = new TestMarcRecordsCollection()
      .withRawRecords(Collections.singletonList(new RawRecord()));

    RestAssured.given()
      .spec(spec)
      .body(testMarcRecordsCollection)
      .when()
      .post(POPULATE_TEST_MARK_RECORDS_PATH)
      .then()
      .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY);

    RestAssured.given()
      .spec(spec)
      .when()
      .post(POPULATE_TEST_MARK_RECORDS_PATH)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

}
