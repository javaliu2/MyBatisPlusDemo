package org.suda.other;

public class SnowflakeIdGenerator {

    private final long twepoch = 1609459200000L; // 起始时间戳（2021-01-01）

    private final long workerIdBits = 5L;   // 工作机器ID占5位
    private final long datacenterIdBits = 5L; // 数据中心ID占5位
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);  // 支持的最大机器ID
    private final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits); // 支持的最大数据中心ID

    private final long sequenceBits = 12L;  // 序列号占12位
    private final long workerIdShift = sequenceBits;  // 机器ID偏移
    private final long datacenterIdShift = sequenceBits + workerIdBits; // 数据中心ID偏移
    private final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    private long workerId;
    private long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException("workerId 超出范围");
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId 超出范围");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = currentTime();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }

        if (timestamp == lastTimestamp) {
            // sequenceMask == 2^12-1 = 4095
            // when sequence == 4095, sequence+1 == 4096
            // 其 & sequenceMask == 0
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {  // 表示其已经加一圈了，回到原点，那么只能是下一个毫秒继续生成了
                // 等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift)
                | (datacenterId << datacenterIdShift)
                | (workerId << workerIdShift)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
//        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);
//        for (int i = 0; i < 10; i++) {
//            System.out.println(generator.nextId());
//        }
        SnowflakeIdGenerator sf = new SnowflakeIdGenerator(1, 1);

        long startTime = System.currentTimeMillis();
        long duration = 1; // 运行时间：1毫秒
        long count = 0;

        while (System.currentTimeMillis() - startTime < duration) {
            sf.nextId();
            count++;
        }

        System.out.println("1毫秒内生成ID数量：" + count);  // 4097，拉满了
        System.out.println("理论最大：" + 4096 * duration + "个");
    }
}

