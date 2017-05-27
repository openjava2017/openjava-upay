package org.openjava.upay.shared.redis;

import org.openjava.upay.shared.exception.CacheSystemException;

public interface IRedisSystemService
{
    Long incAndGet(String key) throws CacheSystemException;

    Long incAndGet(String key, int expireInSeconds) throws CacheSystemException;

    void setAndExpire(String key, String value, int expireInSeconds) throws CacheSystemException;

    void set(String key, String value) throws CacheSystemException;

    String getAndExpire(String key, int expireInSeconds) throws CacheSystemException;

    String get(String key) throws CacheSystemException;

    void remove(String... keys) throws CacheSystemException;
}