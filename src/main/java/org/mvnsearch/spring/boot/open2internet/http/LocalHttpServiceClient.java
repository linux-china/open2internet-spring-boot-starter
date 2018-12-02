package org.mvnsearch.spring.boot.open2internet.http;

import reactor.core.publisher.Mono;

/**
 * local http service client
 *
 * @author linux_china
 */
public interface LocalHttpServiceClient {

    String getLocalBaseWebUri();

    Mono<HttpResponse> getHttpResponse(HttpRequest httpRequest);
}
