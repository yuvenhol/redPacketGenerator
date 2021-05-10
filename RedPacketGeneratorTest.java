package com.dls.futureapi.util;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Date 2021/1/22
 * @Author yuvenhol:601136
 */
public class RedPacketGeneratorTest {
    @Test
    public void test01() {
        for (int i = 0; i < 10; i++) {
            BigDecimal total = BigDecimal.valueOf(520);// 总金额
            BigDecimal min = BigDecimal.valueOf(0.07);// 最小
            BigDecimal max = BigDecimal.valueOf(0.19);// 最大
            int count = 4000;// 份数
            double variance = 9;//方差
            RedPacketGenerator.Param param = new RedPacketGenerator.Param(total, count, variance, max, min);
            BigDecimal[] resultCountMoney = RedPacketGenerator.genPackets(param);
            AtomicInteger j = new AtomicInteger();
            Arrays.stream(resultCountMoney).forEach(x -> System.out.print(x + (j.incrementAndGet() % 20 == 0 ? "\n" : ",")));
            System.out.println();
            Arrays.stream(resultCountMoney).reduce(((x1, x2) -> x1.add(x2))).ifPresent(System.out::println);
            long c1 = Arrays.stream(resultCountMoney).filter(x -> x.doubleValue() == 0.19).count();
            System.out.println(c1);

        }
    }
}

