package org.mvnsearch.spring.boot.open2internet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import org.mvnsearch.spring.boot.open2internet.http.HttpResponse;

import java.nio.ByteBuffer;

/**
 * json support
 *
 * @author linux_china
 */
public class JsonSupport {
    private ObjectMapper objectMapper;

    public JsonSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected String toJson(HttpResponse httpResponse) {
        try {
            return objectMapper.writeValueAsString(httpResponse);
        } catch (Exception e) {
            return "{}";
        }
    }

    protected String toJson(Exception e) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatus(500);
        httpResponse.addHeader("Content-Type", "text/plain;charset=UTF-8");
        httpResponse.setBody(e.getMessage().getBytes());
        return toJson(httpResponse);
    }

    protected <T> T readValue(ByteBuffer data, Class<T> valueType) throws Exception {
        return objectMapper.readValue(new ByteBufferBackedInputStream(data), valueType);
    }
}
