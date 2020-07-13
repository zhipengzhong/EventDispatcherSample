package young.eventdispatcher;

public enum ThreadMode {

    /**
     * Subscribers will called in the publish thread.
     */
    POSTING,

    /**
     * Subscribers will called in Android's main thread (UI thread).
     */
    MAIN
}
