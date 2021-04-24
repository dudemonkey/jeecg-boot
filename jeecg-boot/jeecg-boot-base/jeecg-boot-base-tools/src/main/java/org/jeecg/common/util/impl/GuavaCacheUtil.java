package org.jeecg.common.util.impl;

import org.jeecg.common.util.AbstractCacheUtil;
import org.springframework.stereotype.Component;

@Component
public class GuavaCacheUtil extends AbstractCacheUtil {

    @Override
    public Object get(String key) {
        return super.get(key);
    }

    @Override
    public boolean set(String key, Object value) {
        return super.set(key, value);
    }

    @Override
    public boolean expire(String key, long time) {
        return super.expire(key, time);
    }
}
