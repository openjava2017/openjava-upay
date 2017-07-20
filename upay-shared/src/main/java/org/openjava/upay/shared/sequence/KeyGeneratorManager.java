package org.openjava.upay.shared.sequence;

import com.alibaba.druid.util.StringUtils;
import org.openjava.upay.shared.model.PersistentSequenceKey;
import org.openjava.upay.shared.redis.IRedisSystemService;
import org.openjava.upay.util.AssertUtils;
import org.openjava.upay.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component("keyGeneratorManager")
public class KeyGeneratorManager
{
    private static Logger LOG = LoggerFactory.getLogger(KeyGeneratorManager.class);

    private final ConcurrentMap<KeyEntry, IKeyGenerator> keyGenerators = new ConcurrentHashMap<KeyEntry, IKeyGenerator>();

    private final ISerialKeyGenerator keyGenerator = new SerialKeyGeneratorImpl();

    private Lock locker = new ReentrantLock();
    
    @Resource
    private IKeySynchronizer keySynchronizer;
    
    public IKeyGenerator getKeyGenerator(SequenceKey key)
    {
        return getKeyGenerator(key, null);
    }
    
    public IKeyGenerator getKeyGenerator(SequenceKey key, String scope)
    {
        if (key == null) {
            throw new IllegalArgumentException("Miss key parameter");
        }
        
        KeyEntry cachedKey = new KeyEntry(key, scope);
        IKeyGenerator keyGenerator = keyGenerators.get(cachedKey);
        // First check, no need synchronize code block
        if (keyGenerator == null) {
            boolean result = false;
            try {
                result = locker.tryLock(15, TimeUnit.SECONDS);
                if (result) {
                    // Double check for performance purpose
                    if ((keyGenerator = keyGenerators.get(cachedKey)) == null) {
                        int retry = 1;
                        for (;; retry++) { // CAS
                            PersistentSequenceKey persistentKey = keySynchronizer.loadSequenceKey(key.identifier(), scope);
                            if (persistentKey == null) {
                                throw new RuntimeException("Unregistered sequence key generator: " + key.identifier());
                            }
                            
                            long newStartWith = persistentKey.getStartWith();
                            long newEndWith = newStartWith + persistentKey.getIncSpan();
                            if (keySynchronizer.compareAndSet(persistentKey.getId(), newStartWith, newEndWith)) {
                                keyGenerator = new KeyGeneratorImpl(persistentKey.getId(), cachedKey, newStartWith,
                                    newEndWith - 1, persistentKey.getIncSpan());
                                break;
                            }
                            if (retry >= 8) {
                                throw new RuntimeException("Exceed max retry to generate key id for " + key.identifier());
                            }
                        }
                        keyGenerators.put(cachedKey, keyGenerator);
                    }
                }
            } catch (InterruptedException iex) {
                LOG.error("Thread interrupted", iex);
            } finally {
                if (result) {
                    locker.unlock();
                } else {
                    if (!Thread.interrupted()) {
                        throw new RuntimeException("Timeout to get KeyGenerator for " + key.identifier());
                    }
                }
            }
        }
        
        return keyGenerator;
    }
    
    public ISerialKeyGenerator getSerialKeyGenerator()
    {
        return keyGenerator;
    }

    private class KeyGeneratorImpl implements IKeyGenerator
    {
        private long id;
        private KeyEntry keyEntry;
        private long startWith;
        private long endWith;
        private long incSpan;
        private Lock keyLocker = new ReentrantLock();
        
        public KeyGeneratorImpl(long id, KeyEntry key, long startWith, long endWith, long incSpan)
        {
            this.id = id;
            this.keyEntry = key;
            this.startWith = startWith;
            this.endWith = endWith;
            this.incSpan = incSpan;
        }
        
        @Override
        public long nextId()
        {
            boolean result = false;
            try {
                result = keyLocker.tryLock(15L, TimeUnit.SECONDS);
                if (result) {
                    if (startWith <= endWith) {
                        return startWith ++;
                    } else {
                        int retry = 1;
                        for (;; retry ++) {
                            long newStartWith = keySynchronizer.getSequenceKeyValue(id);
                            long newEndWith = newStartWith + incSpan;
                            if (keySynchronizer.compareAndSet(id, newStartWith, newEndWith)) {
                                startWith = newStartWith;
                                endWith = newEndWith - 1;
                                break;
                            }
                            if (retry >= 8) {
                                throw new RuntimeException("Exceed max retry to generate key id for " + keyEntry.key.identifier());
                            }
                        }
                        // Then recursive call for a next ID
                        return nextId();
                    }
                } else {
                    throw new RuntimeException("Timeout to generate key id for " + keyEntry.key.identifier());
                }
            } catch (InterruptedException iex) {
                LOG.error("Thread interrupted", iex);
            } finally {
                if (result) {
                    keyLocker.unlock();
                }
            }
            
            return 0L;
        }
    }
    
    private class SerialKeyGeneratorImpl implements ISerialKeyGenerator
    {
        @Override
        public String nextSerialNo(String typeCode)
        {
            AssertUtils.notEmpty(typeCode, "Miss typeCode arguments");
            IKeyGenerator sequence = KeyGeneratorManager.this.getKeyGenerator(SequenceKey.SERIAL_SEQUENCE);
            //Serial no format: 六位年月日 + 两位数的类型码 + 至少六位顺序号
            String serialNo = DateUtils.format(new Date(), DateUtils.YYMMDD);
            return serialNo.concat(typeCode).concat(String.valueOf(sequence.nextId()));
        }
    }
    
    public enum SequenceKey
    {
        SERIAL_SEQUENCE("SERIAL_SEQUENCE"),

        FUND_TRANSACTION("FUND_TRANSACTION"),

        FUND_ACCOUNT("FUND_ACCOUNT"),

        FUND_FROZEN("FUND_FROZEN");

        private String key;
        
        SequenceKey(String key)
        {
            this.key = key;
        }
        
        @Override
        public String toString()
        {
            return identifier();
        }
        
        public String identifier()
        {
            return key;
        }
    }
    
    private class KeyEntry
    {
        public SequenceKey key;
        public String scope;
        
        public KeyEntry(SequenceKey key, String scope)
        {
            this.key = key;
            this.scope = scope;
        }
        
        @Override
        public int hashCode()
        {
            // Key must be not null
            int hashCode = key.hashCode();
            if (scope != null) {
                hashCode = hashCode + scope.hashCode();
            }
            return hashCode;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj != null && obj instanceof KeyEntry) {
                KeyEntry cachedKey = (KeyEntry) obj;
                // Key must be not null
                if (key.equals(cachedKey.key)) {
                    return StringUtils.equals(scope, cachedKey.scope);
                }
            }
            
            return false;
        }
    }
}