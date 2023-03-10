package com.xianmao.common.redis.handle;

import com.xianmao.common.redis.util.ConvertUtil;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.Subscription;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 发布与订阅助手
 * @author wujh
 * @date 2019/5/21
 * @since 1.8
 */
public final class PubSubHandler implements RedisHandler {
    /**
     * 对象模板
     */
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * redis连接
     */
    private RedisConnection connection;

    /**
     * 发布与订阅助手构造
     * @param dbIndex 数据库索引
     */
    @SuppressWarnings("unchecked")
    PubSubHandler(Integer dbIndex) {
        this.redisTemplate = HandlerManager.createRedisTemplate(dbIndex);
        this.connection = this.redisTemplate.getRequiredConnectionFactory().getConnection();
    }

    /**
     * 发布与订阅助手构造
     * @param transactionHandler 事务助手
     */
    PubSubHandler(TransactionHandler transactionHandler) {
        this.redisTemplate = transactionHandler.getRedisTemplate();
        this.connection = this.redisTemplate.getRequiredConnectionFactory().getConnection();
    }

    /**
     * 发布消息
     * @see <a href="http://redis.io/commands/publish">Redis Documentation: PUBLISH</a>
     * @since redis 2.0.0
     * @param channel 频道
     * @param message 消息
     */
    public void publish(String channel, Object message) {
        this.redisTemplate.convertAndSend(channel, message);
    }

    /**
     * 订阅频道
     * @see <a href="http://redis.io/commands/subscribe">Redis Documentation: SUBSCRIBE</a>
     * @since redis 2.0.0
     * @param listener 监听器
     * @param channels 频道
     */
    public void subscribe(MessageListener listener, String ...channels) {
        this.connection.subscribe(listener, ConvertUtil.toByteArray(RedisSerializer.string(), channels));
    }

    /**
     * 订阅给定模式的频道
     * @see <a href="http://redis.io/commands/psubscribe">Redis Documentation: PSUBSCRIBE</a>
     * @since redis 2.0.0
     * @param listener 监听器
     * @param patterns 模式
     */
    public void pSubscribe(MessageListener listener, String ...patterns) {
        this.connection.pSubscribe(listener, ConvertUtil.toByteArray(RedisSerializer.string(), patterns));
    }

    /**
     * 添加字符串订阅频道
     * @since redis 2.0.0
     * @param channels 频道
     */
    public void addSubChannels(String ...channels) {
        Subscription subscription = this.connection.getSubscription();
        if ((subscription != null && subscription.isAlive())) {
            subscription.subscribe(ConvertUtil.toByteArray(RedisSerializer.string(), channels));
        }
    }

    /**
     * 添加给定模式字符串订阅频道
     * @since redis 2.0.0
     * @param patterns 模式
     */
    public void addPSubChannels(String ...patterns) {
        Subscription subscription = this.connection.getSubscription();
        if ((subscription != null && subscription.isAlive())) {
            subscription.pSubscribe(ConvertUtil.toByteArray(RedisSerializer.string(), patterns));
        }
    }

    /**
     * 退订频道
     * @see <a href="http://redis.io/commands/unsubscribe">Redis Documentation: UNSUBSCRIBE</a>
     * @since redis 2.0.0
     * @param channels 频道
     */
    public void unsubscribe(String ...channels) {
        Subscription subscription = this.connection.getSubscription();
        if ((subscription != null && subscription.isAlive())) {
            if (channels==null || channels.length==0) {
                subscription.unsubscribe();
            }else {
                subscription.unsubscribe(ConvertUtil.toByteArray(RedisSerializer.string(), channels));
            }
        }
    }

    /**
     * 退订给定模式频道
     * @see <a href="http://redis.io/commands/punsubscribe">Redis Documentation: PUNSUBSCRIBE</a>
     * @since redis 2.0.0
     * @param patterns 模式
     */
    public void pUnsubscribe(String ...patterns) {
        Subscription subscription = this.connection.getSubscription();
        if ((subscription != null && subscription.isAlive())) {
            if (patterns==null || patterns.length==0) {
                subscription.pUnsubscribe();
            }else {
                subscription.pUnsubscribe(ConvertUtil.toByteArray(RedisSerializer.string(), patterns));
            }
        }
    }

    /**
     * 获取订阅频道
     * @since redis 2.0.0
     * @return 返回频道列表
     */
    public List<String> getChannels() {
        return this.getChannelOrPatterns(true);
    }

    /**
     * 获取订阅模式
     * @since redis 2.0.0
     * @return 返回模式列表
     */
    public List<String> getPatterns() {
        return this.getChannelOrPatterns(false);
    }

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param <T> 返回类型
     * @return 返回对象
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(byte[] bytes) {
        return (T) ConvertUtil.toJavaType(this.redisTemplate.getValueSerializer().deserialize(bytes), Object.class);
    }

    /**
     * 反序列化
     * @param type 返回值类型
     * @param bytes 字节数组
     * @param <T> 返回类型
     * @return 返回对象
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(Class<T> type, byte[] bytes) {
        return (T) ConvertUtil.toJavaType(this.redisTemplate.getValueSerializer().deserialize(bytes), type);
    }

    /**
     * 获取spring redis模板
     * @return 返回对象模板
     */
    public RedisTemplate<String, Object> getRedisTemplate() {
        return this.redisTemplate;
    }

    /**
     * 获取频道或模式
     * @param isChannels 是否频道
     * @return 返回列表
     */
    private List<String> getChannelOrPatterns(boolean isChannels) {
        Subscription subscription = this.connection.getSubscription();
        if ((subscription != null && subscription.isAlive())) {
            Collection<byte[]> channels;
            if (isChannels) {
                channels = subscription.getChannels();
            }else {
                channels = subscription.getPatterns();
            }
            List<String> list = new ArrayList<>(channels.size());
            RedisSerializer<String> serializer = RedisSerializer.string();
            for (byte[] channel : channels) {
                list.add(serializer.deserialize(channel));
            }
            return list;
        }
        return new ArrayList<>();
    }
}
