package org.mvnsearch.spring.boot.open2internet.rsocket;

/**
 * open2internet authentication
 *
 * @author linux_china
 */
public class Open2InternetAuthentication {
    private String token;
    private String domain;

    public Open2InternetAuthentication() {

    }

    public Open2InternetAuthentication(String token, String domain) {
        this.token = token;
        this.domain = domain;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        if (token == null || domain == null || token.isEmpty() || domain.isEmpty()) {
            return "{}";
        }
        return "{" +
                "\"token\":\"" + token + '\"' +
                ", \"domain\":\"" + domain + '\"' +
                '}';
    }
}
