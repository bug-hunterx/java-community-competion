package com.epam.coderunner.storage;

import com.epam.coderunner.InternalUtils;
import com.epam.coderunner.model.Task;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
final class RedisTasksStorage implements TasksStorage {

    private static final Logger LOG = LoggerFactory.getLogger(RedisTasksStorage.class);

    private final Jedis jedis;

    RedisTasksStorage(@Value("${redis.host}") final String redisHost){
        LOG.debug("Redis host: {}", redisHost);
        jedis = new Jedis(redisHost);
    }

    @Override
    public Task getTask(final long taskId){
        return InternalUtils.fromJson(jedis.get("task:"+taskId), Task.class);
    }

    @VisibleForTesting
    @Override
    public void saveTask(final long taskId, final Task task) {
        jedis.set("task:"+ taskId, InternalUtils.toJson(task));
    }
}
