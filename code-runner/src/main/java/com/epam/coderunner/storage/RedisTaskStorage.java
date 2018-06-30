package com.epam.coderunner.storage;

import com.epam.coderunner.support.InternalUtils;
import com.epam.coderunner.model.Task;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import javax.annotation.PreDestroy;

@Service
@EnableCaching
@CacheConfig(cacheNames = "taskCache")
class RedisTaskStorage implements TaskStorage {

    private static final Logger LOG = LoggerFactory.getLogger(RedisTaskStorage.class);

    private final Jedis jedis;

    RedisTaskStorage(@Value("${redis.host}") final String redisHost){
        LOG.debug("Redis host: {}", redisHost);
        jedis = new Jedis(redisHost);
    }

    @PreDestroy
    private void close(){
        jedis.close();
    }

    @Cacheable
    @Override
    public synchronized Task getTask(final long taskId){
        return InternalUtils.fromJson(jedis.get("task:"+taskId), Task.class);
    }

    @VisibleForTesting
    @CachePut(key = "#taskId")
    @Override
    public synchronized Task saveTask(final long taskId, final Task task) {
        jedis.set("task:"+ taskId, InternalUtils.toJson(task));
        return task;
    }
}
