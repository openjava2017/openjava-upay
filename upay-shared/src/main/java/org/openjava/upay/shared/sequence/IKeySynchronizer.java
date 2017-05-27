package org.openjava.upay.shared.sequence;

import org.openjava.upay.shared.model.PersistentSequenceKey;

public interface IKeySynchronizer
{
    PersistentSequenceKey loadSequenceKey(String key, String scope);
    
    Long getSequenceKeyValue(Long id);
    
    boolean compareAndSet(Long id, Long oldValue, Long newValue);
}
