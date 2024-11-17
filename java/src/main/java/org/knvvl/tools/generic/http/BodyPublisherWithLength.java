package org.knvvl.tools.generic.http;

import java.net.http.HttpRequest.BodyPublisher;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow.Subscriber;

/**
 * This class can add a fix length to a BodyPublisher
 * 
 * @author gevmic0
 *
 */
class BodyPublisherWithLength implements BodyPublisher
{
    private BodyPublisher delegate;
    private long length;

    BodyPublisherWithLength(BodyPublisher delegate, long length)
    {
        this.delegate = delegate;
        this.length = length;
    }

    @Override
    public void subscribe(Subscriber<? super ByteBuffer> subscriber)
    {
        delegate.subscribe(subscriber);
    }

    @Override
    public long contentLength()
    {
        return length;
    }
}
