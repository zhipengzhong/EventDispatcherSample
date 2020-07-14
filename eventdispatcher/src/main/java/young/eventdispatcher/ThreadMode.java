package young.eventdispatcher;

public enum ThreadMode {

    /**
     * Subscribers will called in the publish thread.
     */
    POSTING,

    /**
     * Subscribers will called in Android's main thread (UI thread).
     */
    MAIN,

    /**
     * If the event is generated in the UI thread, DispatcherHandle will posts it to the subscriber using the
     * background thread.
     */
    BACKGROUND,

    /**
     * The subscriber is always posted independently of the thread that produced the event.
     */
    ASYNC
}
