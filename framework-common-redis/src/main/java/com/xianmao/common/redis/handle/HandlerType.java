package com.xianmao.common.redis.handle;


/**
 * 助手类型枚举
 * @author wujh
 * @date 2019/6/8
 * @since 1.8
 */
public enum HandlerType {
    /**
     * 数据库助手
     */
    DB(DBHandler.class),
    /**
     * 键助手
     */
    KEY(KeyHandler.class),
    /**
     * 数字助手
     */
    NUMBER(NumberHandler.class),
    /**
     * 字符串助手
     */
    STRING(StringHandler.class),
    /**
     * 列表助手
     */
    LIST(ListHandler.class),
    /**
     * 哈希助手
     */
    HASH(HashHandler.class),
    /**
     * 无序集合助手
     */
    SET(SetHandler.class),
    /**
     * 有序集合助手
     */
    ZSET(ZsetHandler.class),
    /**
     * 位图助手
     */
    BITMAP(BitmapHandler.class),
    /**
     * 地理位置助手
     */
    GEO(GeoHandler.class),
    /**
     * 基数助手
     */
    HYPERLOGLOG(HyperLogLogHandler.class),
    /**
     * lua脚本助手
     */
    SCRIPT(ScriptHandler.class),
    /**
     * 发布与订阅助手
     */
    PUBSUB(PubSubHandler.class),
    /**
     * 流助手
     */
    STREAM(StreamHandler.class),
    /**
     * 分布式锁助手
     */
    REDISLOCK(RedisLockHandler.class),
    /**
     * 哨兵助手
     */
    SENTINEL(SentinelHandler.class),
    /**
     * 集群助手
     */
    CLUSTER(ClusterHandler.class),
    /**
     * 自定义命令助手
     */
    CUSTOMCOMMAND(CustomCommandHandler.class),
    /**
     * 事务助手
     */
    TRANSACTION(TransactionHandler.class);

    /**
     * 对应类型
     */
    private Class typeClass;

    /**
     * 枚举构造
     * @param typeClass 助手类型
     */
    HandlerType(Class typeClass) {
        this.typeClass = typeClass;
    }

    /**
     * 获取助手类型
     * @return 返回助手类型
     */
    public Class getTypeClass() {
        return this.typeClass;
    }
}
