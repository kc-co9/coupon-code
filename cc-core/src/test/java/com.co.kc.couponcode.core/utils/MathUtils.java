package com.co.kc.couponcode.core.utils;

public class MathUtils {

    /**
     * 判断是否质数
     */
    public static boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        }
        long sqrtN = (long) Math.sqrt(n);
        for (long i = 2; i <= sqrtN; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个数是否互质
     */
    public static boolean areCoprime(long num1, long num2) {
        return gcd(num1, num2) == 1;
    }

    /**
     * 计算两个数的最大公因数
     */
    public static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
}
