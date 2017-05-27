package org.openjava.upay.shared.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisDistributedLock extends RedisCacheSupport implements IDistributedLock
{
    private static Logger LOG = LoggerFactory.getLogger(RedisDistributedLock.class);
    
    private static String KEY_FRONTDESK_LOCKER_PREFEX = "icard:lock:";

    @Override
    public boolean tryLock(String lockKey, String owner)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            String key = KEY_FRONTDESK_LOCKER_PREFEX + lockKey;
            Long i = connection.setnx(key, owner);
            if (i == 1) {
                // Avoid dead lock
                connection.expire(key, 30);
            }
            return i == 1;
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Distributed lock system exception", jce);
            return false;
        } catch(Exception ex) {
            LOG.error("Distributed lock system exception", ex);
            return false;
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public boolean tryLock(String lockKey, String owner, long timeout)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            String key = KEY_FRONTDESK_LOCKER_PREFEX + lockKey;
            long start = System.currentTimeMillis();
            Long i = 0L;
            do {
                i = connection.setnx(key, owner);
                if (i == 1) {
                    // Avoid dead lock
                    connection.expire(key, 30);
                    break;
                }
                
                if (timeout <= 0) {
                    break;
                }
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException iex) {
                    break;
                }
            } while (System.currentTimeMillis() - start < timeout);
            
            return i == 1;
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Distributed lock system exception", jce);
            return false;
        } catch(Exception ex) {
            LOG.error("Distributed lock system exception", ex);
            return false;
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public void unlock(String lockKey)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            connection.del(KEY_FRONTDESK_LOCKER_PREFEX + lockKey);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Distributed lock system exception", jce);
        } catch(Exception ex) {
            LOG.error("Distributed lock system exception", ex);
        } finally {
            releaseResource(connection);
        }
    }
}
