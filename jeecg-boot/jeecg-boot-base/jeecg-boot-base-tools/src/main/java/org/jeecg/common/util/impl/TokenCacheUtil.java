package org.jeecg.common.util.impl;

import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.util.AbstractCacheUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class TokenCacheUtil extends AbstractCacheUtil {

    @Cacheable(value = CacheConstant.token_cache,key="#key")
    @Override
    public Object get(String key) {
        return super.get(key);
    }

    @CachePut(value=CacheConstant.token_cache ,key="#key")
    @Override
    public boolean set(String key, Object value) {
        return super.set(key, value);
    }

//    @CacheEvict(value=CacheConstant.token_cache ,key="#key")
    @Override
    public boolean expire(String key, long time) {
        return super.expire(key, time);
    }
}
