package ru.moskalev.demo.service.notification;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 1, warmups = 1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Threads(8)
public class SimpleBanchMark {

    private static final int MAP_SIZE = 100_000;

    // Предзаполненная мапа для SynchronizedMap
    @State(Scope.Benchmark)
    public static class SynchronizedMapState {
        Map<String, Integer> map;

        @Setup
        public void setup() {
            Map<String, Integer> raw = new HashMap<>();
            for (int i = 0; i < MAP_SIZE; i++) {
                raw.put("key" + i, i);
            }
            this.map = Collections.synchronizedMap(raw);
        }
    }

    // Предзаполненная мапа для ConcurrentHashMap
    @State(Scope.Benchmark)
    public static class CHMState {
        Map<String, Integer> map;

        @Setup
        public void setup() {
            Map<String, Integer> raw = new ConcurrentHashMap<>();
            for (int i = 0; i < MAP_SIZE; i++) {
                raw.put("key" + i, i);
            }
            this.map = raw;
        }
    }

    @Benchmark
    public Integer synchronizedMapGet(SynchronizedMapState state, Blackhole bh) {
        String key = "key" + ThreadLocalRandom.current().nextInt(MAP_SIZE);
        Integer value = state.map.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public void synchronizedMapPut(SynchronizedMapState state) {
        String key = "key" + ThreadLocalRandom.current().nextInt(MAP_SIZE);
        state.map.put(key, ThreadLocalRandom.current().nextInt());
    }

    @Benchmark
    public Integer concurrentHashMapGet(CHMState state, Blackhole bh) {
        String key = "key" + ThreadLocalRandom.current().nextInt(MAP_SIZE);
        Integer value = state.map.get(key);
        bh.consume(value);
        return value;
    }

    @Benchmark
    public void concurrentHashMapPut(CHMState state) {
        String key = "key" + ThreadLocalRandom.current().nextInt(MAP_SIZE);
        state.map.put(key, ThreadLocalRandom.current().nextInt());
    }

}
