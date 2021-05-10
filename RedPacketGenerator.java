package com.dls.futureapi.util;

import com.dls.future.common.db.entity.RedPacket;
import com.dls.future.common.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @Description 红包生成器
 * @Date 2021/1/22
 * @Author yuvenhol:601136
 */
@Slf4j
public class RedPacketGenerator {

    public static final Random random = new Random();

    public static BigDecimal[] genPackets(Param param) {
        ExceptionUtil.check(!check(param), "红包参数不正常");
        BigDecimal leftMoney = param.total;//可分配的金额
        BigDecimal min = param.min;// 最小
        BigDecimal max = param.max;// 最大
        Integer count = param.count.intValue();// 份数
        BigDecimal[] result = new BigDecimal[count];
        //数组所有元素都赋最小值 铺底
        IntStream.range(0, count).forEach(x -> result[x] = min);
        leftMoney = leftMoney.subtract(min.multiply(param.count));
        //定义正态分布的随机数组
        BigDecimal[] normalNumbers = new BigDecimal[count];
        //给数组赋值
        IntStream.range(0, count).forEach(x -> normalNumbers[x] = NormalDistribution(param.variance));
        //统计随机数总合
        BigDecimal countNormalNumbers = Arrays.stream(normalNumbers).reduce((x1, x2) -> x1.add(x2)).get();
        //每份金额, 这里提高精度减小误差
        BigDecimal perAmount = leftMoney.divide(countNormalNumbers, 10, BigDecimal.ROUND_HALF_UP);
        //此时 perAmount * countNormalNumbers = leftMoney,
        int i = -1;
        while (leftMoney.compareTo(BigDecimal.ZERO) > 0) {
            i = ++i % count;
            // 取随机安全值
            BigDecimal addAmount = perAmount.multiply(normalNumbers[i]).setScale(2, BigDecimal.ROUND_HALF_UP);
            //超越上限
            if (addAmount.add(result[i]).compareTo(max) > 0) {
                continue;
            }
            //防止剩余钱数减超
            if (leftMoney.subtract(addAmount).compareTo(BigDecimal.ZERO) <= 0) {
                addAmount = leftMoney;
                leftMoney = BigDecimal.ZERO;
            } else {
                leftMoney = leftMoney.subtract(addAmount);
            }
            result[i] = result[i].add(addAmount);
        }
        return result;
    }

    public static boolean check(Param param) {
        if (param == null) {
            log.error("参数为空");
            return false;
        }
        if (param.max.compareTo(param.min) < 0) {
            log.error("最大最小值不匹配");
            return false;
        }
        if (param.max.multiply(param.count).compareTo(param.total) < 0) {
            log.error("最大值不能超过总值");
            return false;
        }
        return true;
    }

    public static boolean check(RedPacket redPacket) {
        return check(new RedPacketGenerator.Param(redPacket.getRealTotalMoney(), redPacket.getRealCount(), redPacket.getMax(), redPacket.getMin()));
    }


    /**
     * 获取 0-1正态分布的随机值
     *
     * @param v 方差
     * @return 随机值
     */
    public static BigDecimal NormalDistribution(double v) {
        double r;
        int varianceRange = 3; //这里正态分布N中三倍标准差 (99.7%)。
        do {
            r = Math.sqrt(v) * random.nextGaussian();
        }
        while (r < -varianceRange || r > varianceRange);//过滤掉超过规定范围的特殊值
        r = (r / varianceRange + 1) / 2;//整理成 0-1之间的数
        return new BigDecimal(r);
    }

    public static class Param {
        BigDecimal total;// 总金额
        BigDecimal min;// 最小
        BigDecimal max;// 最大
        BigDecimal count;// 份数
        double variance;//方差


        public Param(BigDecimal totalMoney, int count, BigDecimal max, BigDecimal min) {
            this(totalMoney, count, 10, max, min);
        }

        public Param(BigDecimal totalMoney, int count, double variance, BigDecimal max, BigDecimal min) {
            this.total = totalMoney;
            this.min = min;
            this.max = max;
            this.count = BigDecimal.valueOf(count);
            this.variance = variance;
        }
    }
}

