
package org.folio.rest.client;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import org.folio.rest.tools.utils.VertxUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * Auto-generated code - based on class org.folio.rest.jaxrs.resource.SourceStorageSourceRecords
 *
 */
public class SourceStorageSourceRecordsClient {

    private final static String GLOBAL_PATH = "/source-storage/source-records";
    private String tenantId;
    private String token;
    private String okapiUrl;
    private HttpClientOptions options;
    private HttpClient httpClient;

    public SourceStorageSourceRecordsClient(String okapiUrl, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
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

    public SourceStorageSourceRecordsClient(String okapiUrl, String tenantId, String token, boolean keepAlive) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this(okapiUrl, tenantId, token, keepAlive, 2000, 5000);
    }

    public SourceStorageSourceRecordsClient(String okapiUrl, String tenantId, String token) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this(okapiUrl, tenantId, token, true, 2000, 5000);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageSourceRecordsClient(String host, int port, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this(((("http://"+ host)+":")+ port), tenantId, token, keepAlive, connTO, idleTO);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageSourceRecordsClient(String host, int port, String tenantId, String token, boolean keepAlive) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this(host, port, tenantId, token, keepAlive, 2000, 5000);
    }

    /**
     * @deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageSourceRecordsClient(String host, int port, String tenantId, String token) {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this(host, port, tenantId, token, true, 2000, 5000);
    }

    /**
     * Convenience constructor for tests ONLY!<br>Connect to localhost on 8081 as folio_demo tenant.@deprecated  use a constructor that takes a full okapiUrl instead
     *
     */
    @Deprecated
    public SourceStorageSourceRecordsClient() {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        this("localhost", 8081, "folio_demo", "folio_demo", false, 2000, 5000);
    }

    /**
     * Service endpoint "/source-storage/source-records"+queryParams.toString()
     *
     */
    public void getSourceStorageSourceRecords(String recordId, String snapshotId, String instanceId, String recordType, Boolean suppressFromDiscovery, Boolean deleted, String leaderRecordStatus, Date updatedAfter, Date updatedBefore, String[] orderBy, int offset, int limit, Handler<HttpClientResponse> responseHandler)
        throws UnsupportedEncodingException
    {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        StringBuilder queryParams = new StringBuilder("?");
        if (recordId!= null) {
            queryParams.append("recordId=");
            queryParams.append(URLEncoder.encode(recordId, "UTF-8"));
            queryParams.append("&");
        }
        if (snapshotId!= null) {
            queryParams.append("snapshotId=");
            queryParams.append(URLEncoder.encode(snapshotId, "UTF-8"));
            queryParams.append("&");
        }
        if (instanceId!= null) {
            queryParams.append("instanceId=");
            queryParams.append(URLEncoder.encode(instanceId, "UTF-8"));
            queryParams.append("&");
        }
        if (recordType!= null) {
            queryParams.append("recordType=");
            queryParams.append(URLEncoder.encode(recordType, "UTF-8"));
            queryParams.append("&");
        }
        if (suppressFromDiscovery!= null) {
            queryParams.append("suppressFromDiscovery=");
            queryParams.append(suppressFromDiscovery);
            queryParams.append("&");
        }
        if (deleted!= null) {
            queryParams.append("deleted=");
            queryParams.append(deleted);
            queryParams.append("&");
        }
        if (leaderRecordStatus!= null) {
            queryParams.append("leaderRecordStatus=");
            queryParams.append(URLEncoder.encode(leaderRecordStatus, "UTF-8"));
            queryParams.append("&");
        }
        if (updatedAfter!= null) {
            queryParams.append("updatedAfter=");
            queryParams.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(ZonedDateTime.ofInstant(updatedAfter.toInstant(), ZoneId.systemDefault())));
            queryParams.append("&");
        }
        if (updatedBefore!= null) {
            queryParams.append("updatedBefore=");
            queryParams.append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(ZonedDateTime.ofInstant(updatedBefore.toInstant(), ZoneId.systemDefault())));
            queryParams.append("&");
        }
        if (orderBy!= null) {
            queryParams.append("orderBy=");
            if(orderBy.getClass().isArray()){queryParams.append(String.join("&orderBy=",orderBy));}
            queryParams.append("&");
        }
        queryParams.append("offset=");
        queryParams.append(offset);
        queryParams.append("&");
        queryParams.append("limit=");
        queryParams.append(limit);
        queryParams.append("&");
        io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl+"/source-storage/source-records"+queryParams.toString());
        request.handler(responseHandler);
        request.putHeader("Accept", "application/json,text/plain");
        if (tenantId!= null) {
            request.putHeader("X-Okapi-Token", token);
            request.putHeader("x-okapi-tenant", tenantId);
        }
        if (okapiUrl!= null) {
            request.putHeader("X-Okapi-Url", okapiUrl);
        }
        request.end();
    }

    /**
     * Service endpoint "/source-storage/source-records/"+id+""+queryParams.toString()
     *
     */
    public void getSourceStorageSourceRecordsById(String id, String idType, Handler<HttpClientResponse> responseHandler)
        throws UnsupportedEncodingException
    {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        StringBuilder queryParams = new StringBuilder("?");
        if (idType!= null) {
            queryParams.append("idType=");
            queryParams.append(URLEncoder.encode(idType, "UTF-8"));
            queryParams.append("&");
        }
        io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl+"/source-storage/source-records/"+id+""+queryParams.toString());
        request.handler(responseHandler);
        request.putHeader("Accept", "application/json,text/plain");
        if (tenantId!= null) {
            request.putHeader("X-Okapi-Token", token);
            request.putHeader("x-okapi-tenant", tenantId);
        }
        if (okapiUrl!= null) {
            request.putHeader("X-Okapi-Url", okapiUrl);
        }
        request.end();
    }

    /**
     * Service endpoint "/source-storage/source-records"+queryParams.toString()
     *
     */
    public void postSourceStorageSourceRecords(String idType, Boolean deleted, java.util.List List, Handler<HttpClientResponse> responseHandler)
        throws UnsupportedEncodingException, Exception
    {
        // Auto-generated code
        // - generated by       org.folio.rest.tools.ClientGenerator
        // - generated based on org.folio.rest.jaxrs.resource.SourceStorageSourceRecordsResource
        StringBuilder queryParams = new StringBuilder("?");
        if (idType!= null) {
            queryParams.append("idType=");
            queryParams.append(URLEncoder.encode(idType, "UTF-8"));
            queryParams.append("&");
        }
        if (deleted!= null) {
            queryParams.append("deleted=");
            queryParams.append(deleted);
            queryParams.append("&");
        }
        Buffer buffer = Buffer.buffer();
        if (List!= null) {
            buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(List));
        }
        io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl+"/source-storage/source-records"+queryParams.toString());
        request.handler(responseHandler);
        request.putHeader("Content-type", "application/json");
        request.putHeader("Accept", "application/json,text/plain");
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
