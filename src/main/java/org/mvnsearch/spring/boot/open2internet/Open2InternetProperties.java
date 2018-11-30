package org.mvnsearch.spring.boot.open2internet;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * open2internet properties
 *
 * @author linux_china
 */
@ConfigurationProperties(
        prefix = "open2internet"
)
public class Open2InternetProperties {
    /**
     * access point to open your local service
     */
    private String uri = "tcp://microservices.club:42252";
    /**
     * access token to define you local domain
     */
    private String accessToken;
    /**
     * custom domain to expose
     */
    private String customDomain;

    /**
     * disable expose to internet
     */
    private boolean disabled = false;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
