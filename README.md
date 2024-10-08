## 简介
如果想生成的红包大小相对均衡，可以同时控制最大最小和总额，那么不妨试试我这个方法。
在此之前我们先简单介绍一下正态分布。

## 什么是正态分布（normal distribution）
复习一下高中知识，正态分布又叫做常态分布、高斯分布（Gaussian distribution）。

![image.png](https://i-blog.csdnimg.cn/blog_migrate/418af955667775b9e147768ac447b6b4.png)

![image.png](https://i-blog.csdnimg.cn/blog_migrate/c95200c8cbbac29230e1ed6a736e472c.png)
其中有两个有两个参数：
mu为学期望，可以看作最高概率的点。
sigma^2为方差（实数），可以看作分布的均匀程度。

## 正态分布的应用
现实中很多现象都符合正态分布，十分神奇，比如下面这个图。
![image.png](https://i-blog.csdnimg.cn/blog_migrate/cca99a161621c8a11bd807a291255926.png)
我们可以利用正态分布对很多分布情况建模。所以我们如果想创建随机分布的红包也可以用使用随机分配红包。

## java中的正态分布
JDK中Random类中的nextGaussian()方法，可以产生服从标准正态分布的随机数。即 X~N(0,1);

如果我们想产生自定义的正态分布呢 X~N(μ,b) b=σ^2;

方差 * 正态分布数据 + 正态分布中心位置
产生N(a,b)的数：
```java
Math.sqrt(b)*random.nextGaussian()+a；
```
即均值为a，方差为b的随机数

## 代码实现
整体思想：
其实就是将数据分为两部分，先用最小值min铺平，上面使用正态分布分配数值。那么如何使用正态分布分配呢？首先可以先用上面提到的公式产生n份服从正态分布的0～1之间的随机数，然后对这些随机数做一个概率计算，即求出每份的占比，使用占比与剩余总数相乘的到红包大小。当然，如果有最大值限制，这个过程需要循环进行，最终会把峰值切掉，补充到后面的红包上。
![image.png](https://i-blog.csdnimg.cn/blog_migrate/0c3796643b7fd1f470cdc6f07ecf6d75.png)

1. 



```java



import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * @Description 红包生成器
 * @Date 2021/1/22
 * @Author yuvenhol
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

```
