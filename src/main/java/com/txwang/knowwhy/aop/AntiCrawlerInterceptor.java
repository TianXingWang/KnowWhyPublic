package com.txwang.knowwhy.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.txwang.knowwhy.common.ErrorCode;
import com.txwang.knowwhy.exception.BusinessException;
import com.txwang.knowwhy.exception.ThrowUtils;
import com.txwang.knowwhy.manager.CounterManager;
import com.txwang.knowwhy.model.entity.User;
import com.txwang.knowwhy.service.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(5)
public class AntiCrawlerInterceptor {
    @Resource
    private CounterManager counterManager;

    @Resource
    private UserService userService;


    @Pointcut("@annotation(com.txwang.knowwhy.annotation.AntiCrawler)")
    public void pointcutAnnotation() {
    }


    /**
     * 检测爬虫
     *
     */
    @Before(value = "pointcutAnnotation()")
    private void crawlerDetect(JoinPoint joinPoint) {
        /*Object[] params = joinPoint.getArgs();
        long loginUserId = -1;
        for (Object o : params) {
            if (o instanceof HttpServletRequest) {
                loginUserId = userService.getLoginUserPermitNull((HttpServletRequest)o).getId();
                break;
            }
        }
        ThrowUtils.throwIf(loginUserId == -1, ErrorCode.SYSTEM_ERROR);*/

        long loginUserId = StpUtil.getLoginIdAsLong();
        // 调用多少次时告警
        final int WARN_COUNT = 10;
        // 超过多少次封号
        final int BAN_COUNT = 20;
        // 拼接访问 key
        String key = String.format("user:access:%s", loginUserId);
        // 一分钟内访问次数，180 秒过期
        long count = counterManager.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 180);
        // 是否封号
        if (count > BAN_COUNT) {
            // 踢下线
            StpUtil.kickout(loginUserId);
            // 封号
            User updateUser = new User();
            updateUser.setId(loginUserId);
            updateUser.setUserRole("ban");
            userService.updateById(updateUser);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "访问太频繁，已被封号");
        }
        // 是否告警
        if (count == WARN_COUNT) {
            // 可以改为向管理员发送邮件通知
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "警告：访问太频繁");
        }
    }


}
