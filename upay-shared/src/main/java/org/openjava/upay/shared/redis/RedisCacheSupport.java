package org.openjava.upay.shared.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class RedisCacheSupport
{
    private static Logger LOG = LoggerFactory.getLogger(RedisCacheSupport.class);
    
    private String redisHost;
    private int redisPort;
    private JedisPoolConfig jedisPoolConfig;
    private JedisPool jedisPool;
    
    public void setRedisHost(String redisHost)
    {
        this.redisHost = redisHost;
    }

    public void setRedisPort(int redisPort)
    {
        this.redisPort = redisPort;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig)
    {
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public void start()
    {
        jedisPool = new JedisPool(jedisPoolConfig, redisHost, redisPort);
    }

    public void destroy() throws Exception
    {
        jedisPool.destroy();
    }
    
    protected Jedis getConnection()
    {
        return jedisPool.getResource();
    }
    
    protected void releaseResource(Jedis connection)
    {
        try {
            jedisPool.returnResource(connection);
        } catch (Exception ex) {
            LOG.error("Failed to return jedis connection", ex);
        }
    }
    
    protected void releaseBrokenResource(Jedis connection)
    {
        jedisPool.returnBrokenResource(connection);
    }
}
