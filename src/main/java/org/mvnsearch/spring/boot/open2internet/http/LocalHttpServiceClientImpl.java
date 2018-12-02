package org.mvnsearch.spring.boot.open2internet.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvnsearch.spring.boot.open2internet.JsonSupport;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * local http service client implementation
 *
 * @author linux_china
 */
public class LocalHttpServiceClientImpl extends JsonSupport implements LocalHttpServiceClient {
    /**
     * web client to call local http service
     */
    private WebClient webClient;
    /**
     * local base web URI
     */
    private String localBaseWebUri;

    public LocalHttpServiceClientImpl(ObjectMapper objectMapper, String localBaseWebUri) {
        super(objectMapper);
        this.localBaseWebUri = localBaseWebUri;
        this.webClient = WebClient.create(localBaseWebUri);
    }

    @Override
    public String getLocalBaseWebUri() {
        return localBaseWebUri;
    }

    @Override
    public Mono<HttpResponse> getHttpResponse(HttpRequest httpRequest) {
        WebClient.RequestBodySpec webRequest = webClient.method(HttpMethod.valueOf(httpRequest.getMethod()))
                .uri(httpRequest.getRequestUri())
                .headers(httpHeaders -> {
                    for (Map.Entry<String, String> entry : httpRequest.getHeaders().entrySet()) {
                        httpHeaders.add(entry.getKey(), entry.getValue());
                    }
                });
        if (httpRequest.getBody() != null) {
            webRequest.body(Mono.just(new ByteArrayResource(httpRequest.getBody())), ByteArrayResource.class);
        }
        Mono<ClientResponse> clientResponseMono = webRequest.exchange();
        return clientResponseMono
                .map(clientResponse -> {
                    HttpResponse httpResponse = new HttpResponse();
                    httpResponse.setStatus(clientResponse.rawStatusCode());
                    HttpHeaders httpHeaders = clientResponse.headers().asHttpHeaders();
                    for (String name : httpHeaders.keySet()) {
                        httpResponse.addHeader(name, httpHeaders.getFirst(name));
                    }
                    return httpResponse;
                })
                .zipWith(clientResponseMono.flatMap(clientResponse -> clientResponse.bodyToMono(ByteArrayResource.class)), (httpResponse, byteArrayResource) -> {
                    if (byteArrayResource != null) {
                        httpResponse.setBody(byteArrayResource.getByteArray());
                    }
                    return httpResponse;
                });
    }


}
