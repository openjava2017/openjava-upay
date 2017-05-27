package org.openjava.upay.shared.redis;

public interface IDistributedLock
{
    boolean tryLock(String lockKey, String owner);
    
    boolean tryLock(String lockKey, String owner, long timeout);
    
    void unlock(String lockKey);
}
