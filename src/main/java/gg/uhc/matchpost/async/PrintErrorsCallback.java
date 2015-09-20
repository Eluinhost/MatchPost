package gg.uhc.matchpost.async;

import com.google.common.util.concurrent.FutureCallback;

/**
 * Does nothing with the success and prints the stack trace on failure
 */
public class PrintErrorsCallback<T> implements FutureCallback<T> {
    @Override
    public void onSuccess(T result) {}

    @Override
    public void onFailure(Throwable t) {
        t.printStackTrace();
    }
}
