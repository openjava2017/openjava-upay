package org.openjava.upay.shared.dao;

import org.openjava.upay.shared.model.PersistentSequenceKey;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository("sequenceKeyDao")
public interface ISequenceKeyDao extends MybatisMapperSupport
{
    PersistentSequenceKey loadSequenceKey(String key);
    
    PersistentSequenceKey loadScopeSequenceKey(Map<String, Object> params);
    
    Long getSequenceKeyValue(Long id);
    
    int compareAndSet(Map<String, Object> params);
}
