package edu.cornell.kfs.concur.aws;

public class ConcurStaticConfig {
    private final String login_username;
    private final String login_password;
    private final String consumer_key;
    private final String secret_key;

    public ConcurStaticConfig(String login_username, String login_password,
            String consumer_key, String secret_key) {
        this.login_username = login_username;
        this.login_password = login_password;
        this.consumer_key = consumer_key;
        this.secret_key = secret_key;
    }

    public String getLogin_username() {
        return login_username;
    }

    public String getLogin_password() {
        return login_password;
    }

    public String getConsumer_key() {
        return consumer_key;
    }

    public String getSecret_key() {
        return secret_key;
    }
}
