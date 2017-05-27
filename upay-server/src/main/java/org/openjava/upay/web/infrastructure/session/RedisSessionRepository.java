package org.openjava.upay.web.infrastructure.session;

import org.openjava.upay.shared.redis.RedisCacheSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

public class RedisSessionRepository extends RedisCacheSupport implements ISessionRepository
{
    private static Logger LOG = LoggerFactory.getLogger(RedisSessionRepository.class);
    
    private static final String ICARD_JSESSION_PREFIX = "icard:web:jsession_id:";
    
    private ISessionSerializer sessionSerializer = new ProtocolStuffSerializer();
    
    @Override
    public SharedHttpSession loadSession(String sessionId, int maxInactiveInterval)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            Pipeline transaction = connection.pipelined();
            byte[] sessionKey = sessionSerializer.serializeKey(ICARD_JSESSION_PREFIX + sessionId);
            Response<byte[]> response = transaction.get(sessionKey);
            transaction.expire(sessionKey, maxInactiveInterval);
            transaction.sync();
            byte[] data = response.get();
            return data == null ? null : sessionSerializer.deserializeSession(data);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Redis connection access exception", jce);
        } catch(JedisException ex) {
            LOG.error("Redis access exception", ex);
        } finally {
            releaseResource(connection);
        }
        
        return null;
    }

    @Override
    public void saveSession(SharedHttpSession session, int maxInactiveInterval)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            Pipeline transaction = connection.pipelined();
            byte[] sessionKey = sessionSerializer.serializeKey(ICARD_JSESSION_PREFIX + session.getId());
            byte[] data = sessionSerializer.serializeSession(session);
            transaction.set(sessionKey, data);
            transaction.expire(sessionKey, maxInactiveInterval);
            transaction.sync();
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Redis connection access exception", jce);
        } catch(JedisException ex) {
            LOG.error("Redis access exception", ex);
        } finally {
            releaseResource(connection);
        }
    }

    @Override
    public void removeSession(SharedHttpSession session)
    {
        Jedis connection = null;
        try {
            connection = getConnection();
            byte[] sessionKey = sessionSerializer.serializeKey(ICARD_JSESSION_PREFIX + session.getId());
            connection.del(sessionKey);
        } catch (JedisConnectionException jce) {
            releaseBrokenResource(connection);
            LOG.error("Redis connection access exception", jce);
        } catch(JedisException ex) {
            LOG.error("Redis access exception", ex);
        } finally {
            releaseResource(connection);
        }
    }

    public void setSessionSerializer(ISessionSerializer sessionSerializer)
    {
        this.sessionSerializer = sessionSerializer;
    }
}
