package hu.perit.spvitamin.spring.data.cache;

import java.util.List;
import java.util.function.Consumer;

/**
 * @param <T>
 * @author nagy_peter
 */
public interface WriteBehindCache<T>
{

    /**
     * @param maxQueueSize
     */
    void setMaxQueueSize(long maxQueueSize);

    /**
     * @param maxDelayMillis
     */
    void setMaxDelayMillis(long maxDelayMillis);

    /**
     * @param thrownAwayRecords
     */
    void setThrownAwayMethod(Consumer<List<T>> thrownAwayRecords);

    /**
     * @param data
     */
    void put(T data);

    /**
     * Call this method on preDestroy application event
     */
    void preDestroy();
}
