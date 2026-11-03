package com.projects.razorpay.operations_service.settlement;


import com.projects.razorpay.operations_service.client.MerchantServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
@RequiredArgsConstructor
public class SettlementEngine {

    private final MerchantServiceClient merchantServiceClient;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    @Scheduled(cron = "0 0 23 * * *")
    @SchedulerLock(name = "operations-service-settlement-engine", lockAtMostFor = "2h", lockAtLeastFor = "1m")
    public void runScheduler() {
        // This runs everyday at 11 pm
        log.info("Nightly Settlement Running ... ");
        run();
    }

    public void run() {
        List<UUID> merchantIds = merchantServiceClient.listActiveMerchantIds();
        log.info("Processing settlements for {} merchant Ids", merchantIds.size());

        try(ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();

            for(UUID merchantId : merchantIds) {
                futures.add(executorService.submit(() -> {
                    // call the settlement executor to process the settlement for this merchant
                    settlementTransactionExecutor.processForMerchant(merchantId, LocalDate.now());
                }));
            }

            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Settlement batch future failed: ", e);
                    throw new RuntimeException(e);
                }
            }
        }

        log.info("Settlement batch completed!!!");
    }
}
