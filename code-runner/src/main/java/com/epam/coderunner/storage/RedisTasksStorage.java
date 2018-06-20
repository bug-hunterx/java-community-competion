package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
final class RedisTasksStorage implements TasksStorage {

    private static final Logger LOG = LoggerFactory.getLogger(RedisTasksStorage.class);

    private final Jedis jedis;
    private final Gson gson = new Gson();


    RedisTasksStorage(@Value("${redis.host}") final String redisHost){
        LOG.debug("Redis host: {}", redisHost);
        jedis = new Jedis(redisHost);
    }

    @Override
    public Task getTask(final long taskId){
        return gson.fromJson(jedis.get("task:"+taskId), Task.class);
    }

    @Override
    public void updateTestStatus(String submissionId, TestingStatus testingStatus){
        String json = gson.toJson(testingStatus);
        LOG.debug("testing status: {}", json);
        jedis.set(generateSubmissionKey(submissionId), json);
    }


    private static String generateSubmissionKey(String submissionId){
        return "submission:" + submissionId;
    }

}
