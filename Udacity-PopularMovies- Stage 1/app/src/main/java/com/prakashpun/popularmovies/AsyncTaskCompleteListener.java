package com.prakashpun.popularmovies;

/**
 * Created by prakash_pun on 12/7/2016.
 */

public interface AsyncTaskCompleteListener<T> {

    /**
     * Invoked when the AsyncTask has completed its execution.
     * @param result The resulting object from the AsyncTask.
     */
    public void onTaskComplete(T result);
}
