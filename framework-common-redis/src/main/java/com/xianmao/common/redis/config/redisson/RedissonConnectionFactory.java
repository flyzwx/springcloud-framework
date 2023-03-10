package com.xianmao.common.redis.config.redisson;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.connection.SentinelConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.reactive.CommandReactiveService;
import org.redisson.spring.data.connection.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.redis.ExceptionTranslationStrategy;
import org.springframework.data.redis.PassThroughExceptionTranslationStrategy;
import org.springframework.data.redis.connection.*;
import org.springframework.util.Assert;

/**
 * redisson连接工厂
 * 重写org.redisson.spring.data.connection.RedissonConnectionFactory
 * @author wujh
 * @date 2019/5/14
 * @since 1.8
 */
public class RedissonConnectionFactory implements RedisConnectionFactory,
        ReactiveRedisConnectionFactory, InitializingBean, DisposableBean {

    private final static Log log = LogFactory.getLog(RedissonConnectionFactory.class);

    public static final ExceptionTranslationStrategy EXCEPTION_TRANSLATION =
            new PassThroughExceptionTranslationStrategy(new RedissonExceptionConverter());

    private RedissonConnectionConfiguration configuration;
    private RedissonClient redisson;

    public RedissonConnectionFactory() {
        this(Redisson.create());
    }

    public RedissonConnectionFactory(RedissonClient redisson) {
        this.redisson = redisson;
        this.configuration = (RedissonConnectionConfiguration) this.redisson.getConfig();
    }

    public RedissonConnectionFactory(RedissonConnectionConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return EXCEPTION_TRANSLATION.translate(ex);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void afterPropertiesSet() {
        if (configuration != null) {
            redisson = Redisson.create(configuration);
        }
    }

    @Override
    public RedisConnection getConnection() {
        return new RedissonConnection(redisson);
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        if (!redisson.getConfig().isClusterConfig()) {
            throw new InvalidDataAccessResourceUsageException("Redisson is not in Cluster mode");
        }
        return new RedissonClusterConnection(redisson);
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return true;
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        if (!redisson.getConfig().isSentinelConfig()) {
            throw new InvalidDataAccessResourceUsageException("Redisson is not in Sentinel mode");
        }

        SentinelConnectionManager manager = ((SentinelConnectionManager)((Redisson)redisson).getConnectionManager());
        for (RedisClient client : manager.getSentinels()) {
            org.redisson.client.RedisConnection connection = client.connect();
            try {
                String res = connection.sync(RedisCommands.PING);
                if ("pong".equalsIgnoreCase(res)) {
                    return new RedissonSentinelConnection(connection);
                }
            } catch (Exception e) {
                log.warn("Can't connect to " + client, e);
                connection.closeAsync();
            }
        }

        throw new InvalidDataAccessResourceUsageException("Sentinels are not found");
    }

    @Override
    public ReactiveRedisConnection getReactiveConnection() {
        return new RedissonReactiveRedisConnection(new CommandReactiveService(((Redisson)redisson).getConnectionManager(),new RedissonObjectBuilder(redisson)));
    }

    @Override
    public ReactiveRedisClusterConnection getReactiveClusterConnection() {
        return new RedissonReactiveRedisClusterConnection(new CommandReactiveService(((Redisson)redisson).getConnectionManager(), new RedissonObjectBuilder(redisson)));
    }

    public int getDatabase() {
        return RedisConfiguration.getDatabaseOrElse(configuration, configuration::getDatabase);
    }

    public void setDatabase(int index) {
        Assert.isTrue(index >= 0, "invalid DB index (a positive index required)");
        if (RedisConfiguration.isDatabaseIndexAware(configuration)) {
            ((RedisConfiguration.WithDatabaseIndex) configuration).setDatabase(index);
            return;
        }
        this.configuration.setDatabase(index);
    }
}
