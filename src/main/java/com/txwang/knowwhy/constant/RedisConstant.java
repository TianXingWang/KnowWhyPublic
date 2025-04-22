package com.txwang.knowwhy.constant;

public interface RedisConstant {
    String USER_SIGN_IN_REDIS_KEY_PREFIX = "user_signins";

    String QUESTION_REDIS_KEY_PREFIX = "question";

    static String getUserSignInRedisKey(int year, long userId) {
        return String.format("%s:%s:%s", USER_SIGN_IN_REDIS_KEY_PREFIX, year, userId);
    }

    static String getQuestionRedisKey(long questionId) {
        return String.format("%s:%s", QUESTION_REDIS_KEY_PREFIX, questionId);
    }

}
