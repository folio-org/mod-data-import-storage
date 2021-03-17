package org.folio.rest.impl.wrapper;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.WriteStream;
import io.vertx.sqlclient.Row;
import static java.lang.String.format;

public class SearchRecordIdsWriteStream implements WriteStream<Row> {
  private final HttpServerResponse delegate;
  private int writeIndex = 0;
  private int totalCount = 0;
  private String COMMA = ",";
  private String DOUBLE_QUOTE = "\"";
  private final String emptyResponse = "{\n  \"records\" : [ ],\n  \"totalCount\" : 0\n}";
  private final String responseBeginning = "{\n  \"records\" : [%s";
  private final String responseEnding = "],\n  \"totalCount\" : %s\n}";

  public SearchRecordIdsWriteStream(HttpServerResponse delegate) {
    this.delegate = delegate;
  }

  @Override
  public Future<Void> write(Row row) {
    String instanceId = row.getUUID("instance_id").toString();
    if (writeIndex == 0) {
      this.totalCount = row.getInteger("totalCount");
      this.writeIndex++;
      return this.delegate.write(format(responseBeginning, DOUBLE_QUOTE + instanceId + DOUBLE_QUOTE));
    } else {
      this.writeIndex++;
      return this.delegate.write(COMMA + DOUBLE_QUOTE + instanceId + DOUBLE_QUOTE);
    }
  }

  @Override
  public void write(Row row, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("The method is not supported");
  }

  @Override
  public void end(Handler<AsyncResult<Void>> handler) {
    if (this.totalCount == 0) {
      this.delegate.write(emptyResponse).onSuccess(ar -> {
        this.delegate.end(handler);
      });
    } else {
      this.delegate.write(format(responseEnding, totalCount)).onSuccess(ar -> {
        this.delegate.end(handler);
      });
    }
  }

  @Override
  public WriteStream<Row> exceptionHandler(Handler<Throwable> handler) {
    delegate.exceptionHandler(handler);
    return this;
  }

  @Override
  public WriteStream<Row> setWriteQueueMaxSize(int maxSize) {
    delegate.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return delegate.writeQueueFull();
  }

  @Override
  public WriteStream<Row> drainHandler(@Nullable Handler<Void> handler) {
    delegate.drainHandler(handler);
    return this;
  }

  public void close() {
    this.delegate.close();
  }
}
