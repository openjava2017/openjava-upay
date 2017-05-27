package org.openjava.upay.shared.sequence;

import org.openjava.upay.shared.dao.ISequenceKeyDao;
import org.openjava.upay.shared.model.PersistentSequenceKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service("keySynchronizer")
public class DefaultKeySynchronizer implements IKeySynchronizer
{
    @Resource
    private ISequenceKeyDao sequenceKeyDao;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public PersistentSequenceKey loadSequenceKey(String key, String scope)
    {
        if (scope == null) {
            return sequenceKeyDao.loadSequenceKey(key);
        } else {
            Map<String, Object> params = new HashMap<String, Object>(2);
            params.put("key", key);
            params.put("scope", scope);
            return sequenceKeyDao.loadScopeSequenceKey(params);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Long getSequenceKeyValue(Long id)
    {
        return sequenceKeyDao.getSequenceKeyValue(id);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean compareAndSet(Long id, Long oldValue, Long newValue)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("oldValue", oldValue);
        params.put("newValue", newValue);
        return sequenceKeyDao.compareAndSet(params) > 0;
    }
}
