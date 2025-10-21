package ru.moskalev.demo.service.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import ru.moskalev.demo.BaseIntegrationTest;
import ru.moskalev.demo.cache.TransactionCache;
import ru.moskalev.demo.data.TransferGeneratorService;
import ru.moskalev.demo.service.CacheableService;
import ru.moskalev.demo.service.exproblem.StreamTransferService;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext
public class UnsafeCacheConcurrencyTest extends BaseIntegrationTest {

    @Qualifier("concurrentTransactionCache")
    @Autowired
    private TransactionCache cache;

    @Autowired
    private StreamTransferService streamTransferService;

    private static final int THREAD_COUNT = 50;

    @Test
    void testUnsafeHashMapCacheThrowsConcurrentModificationException() throws Exception {
        // Запускаем нагрузку на небезопасный кэш
        AtomicReference<Throwable> exceptionRef = new AtomicReference<>();

        CacheableService service = new CacheableService(cache);
        List<TransferGeneratorService.TransferOperation> transfers = streamTransferService.getOperations();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(transfers.size());

        for (TransferGeneratorService.TransferOperation op : transfers) {
            executor.submit(() -> {
                try {
                    service.executeTransfer(op);
                } catch (Throwable t) {
                    // Запоминаем первую ошибку
                    exceptionRef.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS); // ждём завершения или таймаута
        executor.shutdown();

        // Проверяем: если была ошибка — это ожидаемо для небезопасного кэша
        Throwable error = exceptionRef.get();
//        assertTrue(error instanceof ConcurrentModificationException ||
//                error.getCause() instanceof ConcurrentModificationException);
        assertTrue(error==null);
        //assertTrue(error.getClass().getSimpleName().contains("ConcurrentModificationException"))
        //

    }
}
