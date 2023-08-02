package org.wanghailu.mybatismix.keygenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wanghailu.mybatismix.util.ExceptionUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.time.LocalDateTime;
import java.util.Calendar;

/**
 * 雪花算法 （目前设定：每毫秒可以允许4096个id，允许1024个workerId）
 */
public class SnowflakeIDGenImpl {
    
    private static Logger logger = LoggerFactory.getLogger(SnowflakeIDGenImpl.class);
    
    public static final long EPOCH;
    
    static {
        //初始化时间的偏移量的基准值。即现在减去2020年6月1号的值，即是相对时间值
        Calendar calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JUNE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
    }
    
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long WORKER_ID_BITS = 10L;
    
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;
    
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS;
    
    /**
     * 从Id中逆向获得生成时间
     *
     * @param generateKey
     * @return
     */
    public static long getGenerateTime(long generateKey) {
        return (generateKey >> TIMESTAMP_LEFT_SHIFT_BITS) + EPOCH;
    }
    
    /**
     * 从Id中逆向获得workId
     *
     * @param generateKey
     * @return
     */
    public static long getWorkId(long generateKey) {
        long workIdMax = (1 << WORKER_ID_BITS) - 1;
        return (generateKey >> SEQUENCE_BITS) & workIdMax;
    }
    
    /**
     * 从Id中逆向获得sequence
     *
     * @param generateKey
     * @return
     */
    public static long getSequence(long generateKey) {
        return generateKey & SEQUENCE_MASK;
    }
    
    public static void main(String[] args) {
        SnowflakeIDGenImpl snowflakeIDGen = new SnowflakeIDGenImpl();
        for (int i = 0; i < 1000; i++) {
            long generateKey = snowflakeIDGen.generateKey();
            System.out.println("id:" + generateKey + ",time:" + TruckUtils
                    .convertSimpleType(getGenerateTime(generateKey), LocalDateTime.class) + ",wordId:" + getWorkId(
                    generateKey) + ",seq:" + getSequence(generateKey));
        }
    }
    
    
    protected static SnowflakeIDGenImpl INSTANCE = new SnowflakeIDGenImpl();
    
    public static Long generate() {
        return INSTANCE.generateKey();
    }
    
    private long workerId;
    
    private final long originalWorkerId;
    
    /**
     * 时间回拨使用等待策略的时间差，1.5秒
     */
    public long maxWaitTimeDifferenceMilliseconds = 1500;
    
    public SnowflakeIDGenImpl() {
        long workerId = getWorkerIdBySystemProperty();
        if (workerId != -1) {
            this.workerId = workerId;
            this.originalWorkerId = workerId;
        } else {
            workerId = getWorkerIdByRandom();
            this.workerId = workerId;
            this.originalWorkerId = -1;
        }
    }
    
    public SnowflakeIDGenImpl(long workerId) {
        this.workerId = formatWorkId(workerId);
        this.originalWorkerId = workerId;
    }
    
    private long sequence;
    
    private long lastMilliseconds;
    
    private long maxTimeCallbackMilliseconds = -1;
    
    private long startingValue = 0;
    
    public synchronized long generateKey() {
        long currentMilliseconds = System.currentTimeMillis();
        //判断是否发生时间回拨
        if (currentMilliseconds < lastMilliseconds) {
            logger.warn("出现时间回拨, 上次时间戳为 {}, 当前时间戳为 {}", lastMilliseconds, currentMilliseconds);
            long timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds;
            if (timeDifferenceMilliseconds < maxWaitTimeDifferenceMilliseconds) {
                try {
                    Thread.sleep(timeDifferenceMilliseconds + 1);
                } catch (Throwable e) {
                    ExceptionUtils.throwException(e);
                }
                currentMilliseconds = System.currentTimeMillis();
            } else {
                //进行回拨模式，计数起始值不再从0开始，如果workId是采用随机hash的方式，则进行再hash。
                maxTimeCallbackMilliseconds =
                        lastMilliseconds > maxTimeCallbackMilliseconds ? lastMilliseconds : maxTimeCallbackMilliseconds;
                startingValue = ((startingValue * 7) >> 3) + 512;
                if (originalWorkerId == -1) {
                    workerId = formatWorkId(workerId * 31);
                }
                logger.warn("出现时间回拨, 修改起始值为 {}, 回拨模式截至时间为 {}", startingValue, maxTimeCallbackMilliseconds);
            }
        } else if (maxTimeCallbackMilliseconds != -1 && currentMilliseconds > maxTimeCallbackMilliseconds) {
            //时间走到回拨之前的时刻，退出回拨模式，重置计数起始值为0，还原workId。
            logger.warn("回拨模式截至时间为 {},时间回拨后恢复正常", maxTimeCallbackMilliseconds);
            maxTimeCallbackMilliseconds = -1;
            startingValue = 0;
            if (originalWorkerId != -1) {
                workerId = originalWorkerId;
            } else {
                workerId = getWorkerIdByRandom();
            }
        }
        if (lastMilliseconds == currentMilliseconds) {
            if (0L == (sequence = (sequence + 1) & SEQUENCE_MASK)) {
                currentMilliseconds = waitUntilNextTime(currentMilliseconds);
            }
        } else {
            sequence = startingValue;
        }
        lastMilliseconds = currentMilliseconds;
        return ((currentMilliseconds - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | workerId | sequence;
    }
    
    
    private long waitUntilNextTime(final long lastTime) {
        long result = System.currentTimeMillis();
        while (result <= lastTime) {
            result = System.currentTimeMillis();
        }
        return result;
    }
    
    
    protected long getWorkerIdBySystemProperty() {
        //初始化workerId的值。最好是通过配置进行设置，
        try {
            String workerIdStr = System.getProperty("snowflake.workerId");
            if (workerIdStr != null) {
                long workerIdLong = Long.parseLong(workerIdStr);
                logger.info("use workerId from System.getProperty(),value:" + workerIdStr);
                return workerIdLong;
            }
        } catch (Throwable e) {
        }
        return -1;
    }
    
    protected long getWorkerIdByRandom() {
        //初始化workerId的值。默认采用ip+端口号进行hash，再除2的WORKER_ID_BITS次方的余数作为worker(此时就会有极低概率出现workerId冲突，导致id出现冲突)
        String random = TruckUtils.getLocalAddress() + ":" + TruckUtils.getPid();
        int hashCode = random.hashCode();
        long workerIdLong = formatWorkId(hashCode);
        logger.info("use workerId from random:" + random + ",hashValue:" + hashCode);
        return workerIdLong;
    }
    
    protected long formatWorkId(long workerId) {
        long workIdMax = (1 << WORKER_ID_BITS) - 1;
        long value = workIdMax & workerId;
        logger.info("real use workerId value:" + value);
        return value << SEQUENCE_BITS;
    }
}
