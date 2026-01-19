//package com.ecommerce.service;
//
//import org.springframework.stereotype.Service;
//
//import com.ecommerce.entity.Role;
//
//import java.math.BigDecimal;
//
//@Service
//public class DiscountService {
//    
//    public BigDecimal calculateDiscount(Role role, BigDecimal orderTotal) {
//        DiscountStrategy strategy = getDiscountStrategy(role, orderTotal);
//        return strategy.calculate(orderTotal);
//    }
//
//    private DiscountStrategy getDiscountStrategy(Role role, BigDecimal orderTotal) {
//        if (role == Role.PREMIUM_USER) {
//            return new PremiumUserDiscountStrategy(orderTotal);
//        } else if (role == Role.USER) {
//            return new RegularUserDiscountStrategy(orderTotal);
//        } else {
//            return new NoDiscountStrategy();
//        }
//    }
//
//    interface DiscountStrategy {
//        BigDecimal calculate(BigDecimal amount);
//    }
//
//    static class NoDiscountStrategy implements DiscountStrategy {
//        @Override
//        public BigDecimal calculate(BigDecimal amount) {
//            return BigDecimal.ZERO;
//        }
//    }
//
//    static class RegularUserDiscountStrategy implements DiscountStrategy {
//        private final BigDecimal orderTotal;
//
//        RegularUserDiscountStrategy(BigDecimal orderTotal) {
//            this.orderTotal = orderTotal;
//        }
//
//        @Override
//        public BigDecimal calculate(BigDecimal amount) {
//            // USER: Orders > $500 get extra 5% discount
//            if (orderTotal.compareTo(new BigDecimal("500")) > 0) {
//                return amount.multiply(new BigDecimal("0.05"));
//            }
//            return BigDecimal.ZERO;
//        }
//    }
//
//    static class PremiumUserDiscountStrategy implements DiscountStrategy {
//        private final BigDecimal orderTotal;
//
//        PremiumUserDiscountStrategy(BigDecimal orderTotal) {
//            this.orderTotal = orderTotal;
//        }
//
//        @Override
//        public BigDecimal calculate(BigDecimal amount) {
//            // PREMIUM_USER: 10% off total order
//            BigDecimal discount = amount.multiply(new BigDecimal("0.10"));
//            
//            // PREMIUM_USER: Orders > $500 get extra 5% discount
//            if (orderTotal.compareTo(new BigDecimal("500")) > 0) {
//                discount = discount.add(amount.multiply(new BigDecimal("0.05")));
//            }
//            
//            return discount;
//        }
//    }
//}