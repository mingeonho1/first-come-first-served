package com.example.api.service;

import com.example.api.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    public void 한번만응모() {
        applyService.apply(1L);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000; // 1000개의 요청
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 병렬작업을 간단하게 도와주는 자바의 API
        CountDownLatch latch = new CountDownLatch(threadCount); // 다른 스레드에서 수행하는 작업을 기다리게 도와주는 클래스

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Consumer가 데이터를 처리하는데 시간이 있음
        Thread.sleep(5000);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(100);
    }

    @Test
    public void 한명당_한개의_쿠폰만_발급() throws InterruptedException {
        int threadCount = 1000; // 1000개의 요청
        ExecutorService executorService = Executors.newFixedThreadPool(32); // 병렬작업을 간단하게 도와주는 자바의 API
        CountDownLatch latch = new CountDownLatch(threadCount); // 다른 스레드에서 수행하는 작업을 기다리게 도와주는 클래스

        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Consumer가 데이터를 처리하는데 시간이 있음
        Thread.sleep(5000);

        long count = couponRepository.count();

        assertThat(count).isEqualTo(1);
    }
}