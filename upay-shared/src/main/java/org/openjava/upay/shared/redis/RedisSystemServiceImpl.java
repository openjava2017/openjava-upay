package org.openjava.upay.shared.redis;

import org.openjava.upay.shared.exception.CacheSystemException;
import org.openjava.upay.util.AssertUtils;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisConnectionException;

@Component
public class RedisSystemServiceImpl extends RedisCacheSupport implements IRedisSystemService
{
    @Override
    public Long incAndGet(String key) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            return connection.incr(key);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public Long incAndGet(String key, int expireInSeconds) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            Pipeline transaction = connection.pipelined();

            Response<Long> result = transaction.incr(key);
            transaction.expire(key, expireInSeconds);
            transaction.sync();
            return result.get();
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public void setAndExpire(String key, String value, int expireInSeconds) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");
        AssertUtils.notEmpty(value, "value must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            // We still can use connection.setex to do the same thing
            Pipeline transaction = connection.pipelined();
            transaction.set(key, value);
            transaction.expire(key, expireInSeconds);
            transaction.sync();
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public void set(String key, String value) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");
        AssertUtils.notEmpty(value, "value must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            connection.set(key, value);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public String getAndExpire(String key, int expireInSeconds) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            Pipeline transaction = connection.pipelined();

            Response<String> result = transaction.get(key);
            transaction.expire(key, expireInSeconds);
            transaction.sync();
            return result.get();
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public String get(String key) throws CacheSystemException
    {
        AssertUtils.notEmpty(key, "key must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            return connection.get(key);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public void remove(String... keys) throws CacheSystemException
    {
        AssertUtils.notEmpty(keys, "keys must be not empty");

        Jedis connection = null;
        try {
            connection = getConnection();
            connection.del(keys);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            throw new CacheSystemException(jce.getMessage(), jce);
        } catch(Exception ex) {
            throw new CacheSystemException(ex.getMessage(), ex);
        } finally {
            releaseResource(connection);
        }
    }
}
