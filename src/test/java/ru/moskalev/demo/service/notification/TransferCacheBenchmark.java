package ru.moskalev.demo.service.notification;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@State(Scope.Benchmark)
@Threads(8)
public class TransferCacheBenchmark {

    public static class Transfer {
        public final String from, to;

        public Transfer(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }

    private List<Transfer> transfers;
    private final Random random = new Random(123);

    private Map<String, Deque<Transfer>> chmCache;
    private Map<String, Deque<Transfer>> syncCache;

    @Setup
    public void setUp() {
        List<String> accounts = IntStream.range(0, 10_000)
                .mapToObj(i -> "acc_" + i)
                .toList();
        this.transfers = IntStream.range(0, 20_000)
                .mapToObj(i -> new Transfer(
                        accounts.get(random.nextInt(accounts.size())),
                        accounts.get(random.nextInt(accounts.size()))
                )).toList();
        chmCache = new ConcurrentHashMap<>();
        syncCache = Collections.synchronizedMap(new HashMap<>());
    }

    @Benchmark
    public void concurrentHashMapTest(Blackhole h) {
        for (Transfer t : transfers) {
            chmCache.compute(t.from, (key, deque) -> {
                if (deque == null) {
                    deque = new ArrayDeque<>(10);
                }
                deque.offerFirst(t);
                while (deque.size() > 10) {
                    deque.pollLast();
                }
                return deque;
            });

            chmCache.compute(t.to, (key, deque) -> {
                if (deque == null) {
                    deque = new ArrayDeque<>(10);
                }
                deque.offerFirst(t);
                while (deque.size() > 10) {
                    deque.pollLast();
                }
                return deque;
            });
        }
    }

    @Benchmark
    public void syncHashMapTest(Blackhole h) {
        for (Transfer t : transfers) {
            syncCache.compute(t.from, (key, deque) -> {
                if (deque == null) {
                    deque = new ArrayDeque<>(10);
                }
                deque.offerFirst(t);
                while (deque.size() > 10) {
                    deque.pollLast();
                }
                return deque;
            });

            syncCache.compute(t.to, (key, deque) -> {
                if (deque == null) {
                    deque = new ArrayDeque<>(10);
                }
                deque.offerFirst(t);
                while (deque.size() > 10) {
                    deque.pollLast();
                }
                return deque;
            });
        }
    }

}
