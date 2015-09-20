package gg.uhc.matchpost.reddit;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;

import java.util.UUID;
import java.util.concurrent.Callable;

public class RedditCallableFactory {

    protected final RedditClient client;
    protected final Credentials credentials;
    protected long validUntil = 0L;

    public RedditCallableFactory(RedditClient client) {
        this.client = client;
        this.credentials = Credentials.userlessApp("HQHV43XniYoxIQ", UUID.randomUUID());
    }

    protected void refreshOAuth() throws OAuthException {
        OAuthData data = client.getOAuthHelper().easyAuth(credentials);
        validUntil = data.getExpirationDate().getTime() - 20000; // take 20 seconds off the time to refresh early
        client.authenticate(data);
    }

    public Callable<String> createSubmissionCallable(String code) {
        return new SubmissionCallable(code);
    }

    class SubmissionCallable implements Callable<String> {

        protected final String code;

        SubmissionCallable(String code) {
            this.code = code;
        }

        @Override
        public String call() throws Exception {
            // refresh token if required
            if (validUntil < System.currentTimeMillis()) {
                refreshOAuth();
            }

            return client.getSubmission(code).getSelftext();
        }
    }
}
