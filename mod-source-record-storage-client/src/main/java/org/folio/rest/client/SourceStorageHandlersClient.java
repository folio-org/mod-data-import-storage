
package org.folio.rest.client;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import org.folio.rest.tools.utils.VertxUtils;


/**
 * Auto-generated code - based on class org.folio.rest.jaxrs.resource.SourceStorageHandlers
 *
 */
public class SourceStorageHandlersClient {

    private final static String GLOBAL_PATH = "/source-storage/handlers";
    private String tenantId;
    private String token;
    private String okapiUrl;
    private HttpClientOptions options;
    private HttpClient httpClient;

    public SourceStorageHandlersClient(String okapiUrl, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this.tenantId = tenantId;
        this.token = token;
        this.okapiUrl = okapiUrl;
        options = new HttpClientOptions();
        options.setLogActivity(true);
        options.setKeepAlive(keepAlive);
        options.setConnectTimeout(connTO);
        options.setIdleTimeout(idleTO);
        httpClient = VertxUtils.getVertxFromContextOrNew().createHttpClient(options);
    }

    public SourceStorageHandlersClient(String okapiUrl, String tenantId, String token, boolean keepAlive) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this(okapiUrl, tenantId, token, keepAlive, 2000, 5000);
    }

    public SourceStorageHandlersClient(String okapiUrl, String tenantId, String token) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this(okapiUrl, tenantId, token, true, 2000, 5000);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageHandlersClient(String host, int port, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this(((("http://"+ host)+":")+ port), tenantId, token, keepAlive, connTO, idleTO);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageHandlersClient(String host, int port, String tenantId, String token, boolean keepAlive) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this(host, port, tenantId, token, keepAlive, 2000, 5000);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageHandlersClient(String host, int port, String tenantId, String token) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this(host, port, tenantId, token, true, 2000, 5000);
    }

    /**
     * Convenience constructor for tests ONLY!<br>Connect to localhost on 8081 as folio_demo tenant.@deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageHandlersClient() {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        this("localhost", 8081, "folio_demo", "folio_demo", false, 2000, 5000);
    }

    /**
     * Service endpoint "/source-storage/handlers/inventory-instance"+queryParams.toString()
     *
     */
    public void postSourceStorageHandlersInventoryInstance(String String, Handler<HttpClientResponse> responseHandler)
        throws Exception
    {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        StringBuilder queryParams = new StringBuilder("?");
        Buffer buffer = Buffer.buffer();
        if (String!= null) {
            buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(String));
        }
        io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl+"/source-storage/handlers/inventory-instance"+queryParams.toString());
        request.handler(responseHandler);
        request.putHeader("Content-type", "application/json");
        if (tenantId!= null) {
            request.putHeader("X-Okapi-Token", token);
            request.putHeader("x-okapi-tenant", tenantId);
        }
        if (okapiUrl!= null) {
            request.putHeader("X-Okapi-Url", okapiUrl);
        }
        request.putHeader("Content-Length", buffer.length()+"");
        request.setChunked(true);
        request.write(buffer);
        request.end();
    }

    /**
     * Service endpoint "/source-storage/handlers/updated-record"+queryParams.toString()
     *
     */
    public void postSourceStorageHandlersUpdatedRecord(String String, Handler<HttpClientResponse> responseHandler)
        throws Exception
    {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageHandlersResource
        StringBuilder queryParams = new StringBuilder("?");
        Buffer buffer = Buffer.buffer();
        if (String!= null) {
            buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(String));
        }
        io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl+"/source-storage/handlers/updated-record"+queryParams.toString());
        request.handler(responseHandler);
        request.putHeader("Content-type", "application/json");
        if (tenantId!= null) {
            request.putHeader("X-Okapi-Token", token);
            request.putHeader("x-okapi-tenant", tenantId);
        }
        if (okapiUrl!= null) {
            request.putHeader("X-Okapi-Url", okapiUrl);
        }
        request.putHeader("Content-Length", buffer.length()+"");
        request.setChunked(true);
        request.write(buffer);
        request.end();
    }

}