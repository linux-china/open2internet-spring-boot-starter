package org.mvnsearch.spring.boot.open2internet.http;

import java.util.HashMap;
import java.util.Map;

/**
 * http request from internet
 *
 * @author linux_china
 */
public class HttpRequest {
    private String path;
    private String query;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public String getRequestUri() {
        String uri = path;
        if (query != null && !query.isEmpty()) {
            uri = path + "?" + query;
        }
        return uri;
    }
}
