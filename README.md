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
