package org.mvnsearch.spring.boot.open2internet;

import java.util.HashMap;
import java.util.Map;

/**
 * http response from local Spring Boot Application
 *
 * @author linux_china
 */
public class HttpResponse {
    private int status;
    private Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
}
