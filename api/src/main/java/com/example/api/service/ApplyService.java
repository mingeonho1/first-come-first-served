package com.example.api.service;

import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRepository;
import com.example.api.repository.CouponRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplyService {

    private final CouponRepository couponRepository;

    private final CouponCountRepository couponCountRepository;

    private final CouponCreateProducer couponCreateProducer;

    private final AppliedUserRepository appliedUserRepository;

    public ApplyService(CouponRepository couponRepository, CouponCountRepository couponCountRepository, CouponCreateProducer couponCreateProducer, AppliedUserRepository appliedUserRepository) {
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.appliedUserRepository = appliedUserRepository;
    }

    public void apply(Long userId) {
        /**
         * 요구사항 변경
         * 1인당 1개의 쿠폰만 생성가능
         * **/
        Long apply = appliedUserRepository.add(userId);

        if (apply != 1) {
            return;
        }

        // 멀티스레드에서 문제 발생
//        long count = couponRepository.count();

        // redis incr 사용
        /** redis는 싱글스레기 기반으로 동작하기 때문에
         *  첫 번째 쓰레드가 작업이 끝날 때 까지 기다린다.
         *  그래서 언제나 최신 count를 가져가기 때문에 쿠폰이 더 많이 생성될 일이 없다.
         * **/
        Long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

        // 트래픽이 몰리게 되면 문제 발생
//        couponRepository.save(new Coupon(userId));

        // kafka로 변경
        couponCreateProducer.create(userId);

    }
}
