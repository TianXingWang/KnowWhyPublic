# KnowWhy

本项目旨在让用户可以像使用 LeetCode 一样通过问答式的题目巩固自己的技术知识，可供面试刷题训练等场景使用

**技术栈：SpringBoot + MySQL + Redis + Elasticsearch + Sa-Token + Sentinel + Nacos + Druid + DeepSeek – R1**

* 基于 Sa-Token + Redis + JWT 实现用户登录和操作鉴权，依据 UserAgent 实现同端登录冲突检测功能；

* 基于 Elasticsearch + ik 分词器实现灵活的题目搜索功能，同时搜索接口平均 RTT 相较于传统 SQL 模糊查询由 96ms 优化为65ms；使用 Spring Scheduler 定时任务实现自 MySQL 至 ES 的更新，保证数据一致性；

* 基于 Redisson + 自定义注解的方式实现了分布式锁，确保了集群服务环境下定时任务不会被重复执行；

* 基于 Redis BitMap 实现每日学习签到功能，相较于使用传统 SQL 数据库表存储，空间占用由 17.52GB 优化至 43.8MB；

* 使用 Sentinel 为获取题库、题目等高并发接口配置限流和熔断降级策略，并通过 blockHandler 实现热门题目自动缓存；

* 为限制恶意用户访问，基于 WebFilter + 布隆过滤器实现 IP 黑名单拦截，并使用 Nacos 配置中心实现动态更新黑名单；

* 使用 AOP + 自定义注解设计了分级反爬虫策略：基于 Redis 实现用户访问题目频率统计，并通过 Lua 脚本实现集群环境下计数操作的原子性；在用户访问题目超 10 次/分时发送警告提醒，超 20 次/分时封禁用户；

* 接入火山引擎 DeepSeek – R1 API，设计高效的系统和用户 prompt，实现了智能“问 AI”功能和 AI 模拟面试功能。