package org.mvnsearch.spring.boot.open2internet.demo;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * portal controller
 *
 * @author linux_china
 */
@RestController
public class PortalController {

    @RequestMapping("/")
    public String index() {
        return "Welcome to local spring boot application!!!";
    }

    @RequestMapping("/welcome")
    public String welcome(ServerHttpRequest request,
                          @RequestBody(required = false) byte[] body) {
        return "welcome";
    }


}
