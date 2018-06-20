package com.epam.coderunner.storage;

import com.epam.coderunner.model.Task;
import com.epam.coderunner.model.TestingStatus;
import com.google.common.annotations.VisibleForTesting;
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
    public void updateTestStatus(final long submissionId, final TestingStatus testingStatus){
        final String json = gson.toJson(testingStatus);
        LOG.debug("Update testing status: {}, submissionId={}", json, submissionId);
        jedis.set(generateSubmissionKey(submissionId), json);
    }

    @VisibleForTesting
    @Override
    public void saveTask(final long taskId, final Task task) {
        jedis.set("task:"+ taskId, gson.toJson(task));
    }
    @VisibleForTesting
    @Override
    public TestingStatus getTestStatus(final long submissionId) {
        final String json = jedis.get(generateSubmissionKey(submissionId));
        LOG.debug("Get testing status: {}, submissionId={}", json, submissionId);
        return gson.fromJson(json, TestingStatus.class);
    }

    private static String generateSubmissionKey(final long submissionId){
        return "submission:" + submissionId;
    }

}
