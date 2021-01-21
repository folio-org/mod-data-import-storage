package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.folio.config.ApplicationConfig;
import org.folio.processing.events.EventManager;
import org.folio.rest.resource.interfaces.InitAPI;
import org.folio.services.handlers.InstancePostProcessingEventHandler;
import org.folio.services.handlers.MarcBibliographicMatchEventHandler;
import org.folio.services.handlers.actions.ModifyRecordEventHandler;
import org.folio.spring.SpringContextUtil;
import org.folio.verticle.consumers.DataImportConsumersVerticle;
import org.folio.verticle.consumers.ParsedMarcChunkConsumersVerticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class InitAPIImpl implements InitAPI {

  private static final Logger LOGGER = LoggerFactory.getLogger(InitAPIImpl.class);

  @Autowired
  private InstancePostProcessingEventHandler instancePostProcessingEventHandler;

  @Autowired
  private ModifyRecordEventHandler modifyRecordEventHandler;

  @Autowired
  private MarcBibliographicMatchEventHandler marcBibliographicMatchEventHandler;

  @Value("${srs.kafka.ParsedMarcChunkConsumer.instancesNumber:1}")
  private int parsedMarcChunkConsumerInstancesNumber;

  @Value("${srs.kafka.DataImportConsumer.instancesNumber:1}")
  private int dataImportConsumerInstancesNumber;

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    try {
      SpringContextUtil.init(vertx, context, ApplicationConfig.class);
      SpringContextUtil.autowireDependencies(this, context);
      registerEventHandlers();
      deployConsumerVerticles(vertx).onComplete(ar -> {
        if (ar.succeeded()) {
          handler.handle(Future.succeededFuture(true));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      });
    } catch (Throwable th) {
      LOGGER.error("Failed to init module", th);
      handler.handle(Future.failedFuture(th));
    }
  }

  private void registerEventHandlers() {
    EventManager.registerEventHandler(instancePostProcessingEventHandler);
    EventManager.registerEventHandler(modifyRecordEventHandler);
    EventManager.registerEventHandler(marcBibliographicMatchEventHandler);
  }

  private Future<?> deployConsumerVerticles(Vertx vertx) {
    //TODO: get rid of this workaround with global spring context
    ParsedMarcChunkConsumersVerticle.setSpringGlobalContext(vertx.getOrCreateContext().get("springContext"));
    DataImportConsumersVerticle.setSpringGlobalContext(vertx.getOrCreateContext().get("springContext"));

    Promise<String> deployConsumer1 = Promise.promise();
    Promise<String> deployConsumer2 = Promise.promise();

    vertx.deployVerticle("org.folio.verticle.consumers.ParsedMarcChunkConsumersVerticle",
      new DeploymentOptions().setWorker(true).setInstances(parsedMarcChunkConsumerInstancesNumber), deployConsumer1);

    vertx.deployVerticle("org.folio.verticle.consumers.DataImportConsumersVerticle",
      new DeploymentOptions().setWorker(true).setInstances(dataImportConsumerInstancesNumber), deployConsumer2);

    return CompositeFuture.all(deployConsumer1.future(), deployConsumer2.future());
  }

}
