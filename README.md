# 分布式券码生成器

## 概述

在典型的团购券、优惠券等应用中常常需要用到一种唯一标识符来标识用户购买到的券，使得用户可以通过这唯一标识符来准确地消费掉这张券，对于这唯一标识符我们称之为"券码"（下文统一用"券码"表示）。一般意义上，"券码"有以下三个特性:

- 无重复。应用平台凭借这个"券码"来标识用户所购买的券，如果存在重复就会造成当用户消费的时候无法识别所要消费的券。
- 无规律。如果应用平台生成的"券码"是很容易被察觉到规律的，这样就很容易让用户通过伪造"券码"去消费，造成不必要的损失。
- 可控的。如果应用平台所生成的"券码"长度太长，那么用户很难通过提供"券码"的方式让商家进行核销或者其它操作。

因此，要是我们想实现一款"券码"生成器，最基本是要保证其生成的"券码"是无重复的、无规律的、可控的。

## 算法

在`coupon-code`项目中，我们采用了线性同余生成器（`LCG`）算法来实现分布式"券码"生成器。经过调研，`LCG`算法是可以满足无重复、无规律和可控的特性的。

线性同余策略（`LCG`）是一个通过不连续的分段线性方程来生成伪随机数的策略，该方程属于最古老、最著名的伪随机数生成算法之一，即:

```text
X(n+1) = (a*X(n) + c) mod m
```

其中，`X(n)`表示伪随机数的值，`a`、`c`和`m`为生成器设定的整形常量，具体含义如下：

- `a`，`0 < a < m`，表示倍率
- `c`，`0 <= c < m`，表示递增量
- `m`，`0 < m`，表示模数

> 在计算第一个伪随机数时方程需要一个起始值`X(0)`，又称之为种子值或者开始值，取值范围为`0 <= X(0) < m`。

根据上述公式，我们可以生成随机"券码"的最大数量为`m`，当然这也需要我们选择合适的参数`a`和参数`c`与其相匹配，如果选择不当的话不但所生成的"券码"数量会大大缩减，而且生成"券码"的效率也会大幅度降低。最简单地，我们可以选择参数`a=1`和参数`c=1`来创建一个"券码"生成器，即：

```text
X(n+1) = (X(n) + 1) mod m
```

虽然这样所能生成的最大"券码"数量为`m`，但是它并不具备随机性。因此，为了能达到最大的随机数周期和更好的随机性，我们一般会选择如下`3`种参数方案：

### 1.`m`为素数，`c=0`

第一种方案是将参数`c`设置为`0`、参数`m`设置为素数(`prime`)，即：

```text
X(n+1) = a*X(n) mod m
```

在这种方案下，如果我们同时将`a`设置为模`m`下的本原元(`primitive element`)，并且`X0`设置在`1`到`m-1`之间（包含），生成随机"券码"的最大周期是可以达到`m-1`的。其中，对于模`m`下的本原元(`primitive element`)可理解为：

```text
对于与模m互质的每个整数a，都有整数k使得g^k ≡ a (mod m)，那么g就被称为模m下的本原元(primitive element)。
```

其中，由于模`m`为素数，使得区间`[1,m-1]`中每个整数`a`都与其互质，也就是说对于区间`[1,m-1]`中每个整数`a`都有`k`使得`g^k ≡ a (mod m)`。

> 因为模`m`为素数，所以模`m`下的本原元`a`一定存在。

### 2.`m`为`2`次幂，`c=0`

第二种方案是将参数`c`设置为`0`、参数`m`设置为`2`的次幂(`a power of two`)，即：

```text
X(n+1) = a*X(n) mod m
```

在这种方案下，我们可以特别有效率的计算`mod`运算，因为当模为`2`的次幂时它可以转换为位运算，即：

```text
X(n+1) = (a*X(n)) mod m 
       = (a*X(n)) & (m-1)
```

通过这种方式，我们就可以通过截断最高有效位来忽略对其的计算了。

然而，在这种方案下生成随机"券码"的最大周期只能达到`m/4`，而要达到最大周期`m/4`需要我们将`a`设置为`a ≡ ±3 (mod 8)`、`X0`设置为奇数。即使在这种最优的情况下，每次生成`Xn`的最低`3`位二进制位也只会在两个数值之间交替（相当于只贡献`1`位二进制有效位）。具体地，在这种情况下`Xn`的最低有效位(`1th bit`)永远不会变化，即`Xn`永远为奇数，而剩余的`2`位最低有效位(`2th bit`和`3th bit`)则在后续的每次计算中只有一位会发生变化。

> 在数学中，表达式`a ≡ ±3 (mod 8)`是一种同余关系，它表示整数`a`除以`8`的余数要么是`3`，要么是`-3`（但通常我们会将其转换为正余数，即`5`，因为`-3`加上`8`的整数倍可以变为`5`）。更具体地，
>
> - `a ≡ 3 (mod 8)`：这表示整数`a`除以`8`的余数是`3`。换句话说，存在某个整数`k`，使得 `a = 8k + 3`。
> - `a ≡ -3 (mod 8)`：这表示整数`a`除以`8`的余数是`-3`。但是，在模运算中，我们通常会将余数转换为正数，因为模运算的结果是一个在模数范围内的数。因此，`-3`可以转换为`8 - 3 = 5`（因为加上`8`的整数倍不会改变余数）。所以，`a ≡ -3 (mod 8)` 等价于 `a ≡ 5 (mod 8)`，表示存在某个整数`k`，使得 `a = 8k + 5`。

实际上，对于上述方案我们完全可以使用模为`m/4`(`2`的次幂)和`c!=0`的`LCG`来代替，具体可看下述第三种方案。

### 3.`m`为`2`次幂，`c!=0`

第三种方案是将参数`c`设置为非`0`、参数`m`设置为`2`的次幂(`a power of two`)，即：

```text
X(n+1) = (a*X(n) + c) mod m
```

根据赫尔-多贝尔(`Hull–Dobell`)定理，在`c != 0`的情况下，只要我们选定的参数符合某种规则，就可以让随机券码的最大周期达到`m`（无论`X0`为任何值），具体规则如下：

1. `m`和`c`互质。
2. `a-1`能被`m`的所有质因子整除。
3. `a-1`能被`4`整除，如果`m`能被`4`整除。

虽然说在这个方案开头声明了`m`需要为`2`的次幂，但实际上`m`可以选取符合以上条件的任何值，只不过当`m`的值存在很多重复的质因子时生成随机数的均匀性和随机性会更好，典型的就是`2`的次幂。 另外，在[《`TABLES OF LINEAR CONGRUENTIAL GENERATORS OF DIFFERENT SIZES AND GOOD LATTICE STRUCTURE`》](doc/Parameters.pdf)论文中也阐述了一种方式让`LCG`的生成周期可以达到最大周期`m`，即当`m`为`2`的次幂，`c`为奇数，`a`为`a ≡ 5（mod 8）`时`LCG`的生成周期能达到最大周期`m`。但实际上，这种参数的选择也是符合赫尔-多贝尔(`Hull–Dobell`)定理的，在使用上我们可通过这种方式来完成参数的选择，避免在参数的选择上因为要进行一系列的验证而耗费大量的时间。

本项目在考虑到算法的生成周期和选择参数校验的难易程度，最终选择了使用方案`3`组成的`LCG`算法来作为券码生成器的算法基础。与此同时，在[《`TABLES OF LINEAR CONGRUENTIAL GENERATORS OF DIFFERENT SIZES AND GOOD LATTICE STRUCTURE`》](doc/Parameters.pdf)论文中对于不同的方案也提供了一系列能达到各自最大周期的可选值，而且在对多维随机数的生成质量（分布均匀性）提供了可量化的数值后标识出其中具有最佳分布均匀性的参数值。虽然多维随机数的分布均匀性对我们当前项目中使用的"券码"生成器（一维随机数）并无直接的影响/关系，但是我们可以直接选用这些能达到最大周期的参数值，从而避免在参数值的选取和校验上浪费了大量的时间。

> 对于多维随机数，我们可以理解为在`t`维空间中的随机坐标。具体地，对于选定的参数`a`、`c`和`m`，使用每个`x0`连续生成`t`个随机数用来表示`T`维空间的一个坐标，即`T={xn=(x(n),..,x(n+t-1))}`，然后根据定义的规则计算出在选定参数下生成的每个坐标分布的均匀值（衡量均匀性），以此来找到具有最佳分布均匀性的参数值。

## 设计

考虑到分布式环境下"券码生成器"的生成效率和性能，在设计上`coupon-code`采取了去中心化的方式来实现，即不存在一个"大"券码池在分布式环境下提供给各个服务共同使用，而是每个服务各自w维护一个"小"券码池提供给自己使用，但这也会带来分布式环境下生成"券码"唯一性的问题，因此需要对每个"小"券码池生成的"券码"添加一个唯一的"序号"`No`。另外，基于`LCG`生成全周期的"券码"是需要记录上一次生成的"券码"`Xn`，因此在设计时也需要考虑到`Xn`的持久化问题。也就是说，为了保证多实例下的唯一性和单实例下的唯一性，在设计时我们需要对"序号"`No`和上一次生成的"券码"`Xn`进行持久化。

假设我们基于数据库来作持久化处理，那我们的表结构将设计为下面这样：

```sql
CREATE TABLE `coupon_code_generator`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `no`           BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '编号',
    `a`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'multiplier',
    `c`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'addend',
    `m`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'modulo',
    `x0`           BIGINT           NOT NULL DEFAULT -1 COMMENT 'x0',
    `xn`           BIGINT           NOT NULL DEFAULT -1 COMMENT 'xn',
    `cnt`          BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT 'xn数量',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态 0-未知 1-待激活 2-激活中 3-已失效',
    `heartbeat_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '心跳时间',
    `created_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY uk_no (`no`) USING BTREE
) ENGINE = InnoDB COMMENT ='券码xn生成器';
```

> 考虑到需要检测通过`LCG`生成的"券码"是否已经完成了一个周期的循环，这里也记录下`x0`来进一步判断，从而避免生成重复的"券码"。另外，也考虑到不同实例可能会使用不同参数来构建`LCG`，从而实现不同业务背景的"券码"能力，这里也记录下`a`、`c`和`m`参数来进行相对应的查询匹配。

另一方面，考虑到持久化过程中可能存在的`IO`操作会影响到"券码"生成的效率，因此在`coupon-code`项目中通过冷池（`Cold pool`）和热池（`Hot pool`）将`Xn`的持久化操作和客户端请求"券码"的操作隔离开来，以此来提高整体的生成效率。具体地，在项目启动时首先会基于设定的`LCG`生成大量的"券码"到冷池中，并在后续不断检测冷池是否处于满盈状态，如果不处于则继续生成"券码"填充进去；与此同时，热池也会不断地从冷池中拉取"券码"直至热池处于满盈状态。其中，"券码"从冷池传输到热池时需要将传输的"券码"进行持久化，以保证服务重启后也能保证"券码"生成的唯一性。

> 根据"券码"的生成机制，冷池和热池的大小会影响到最终"券码"生成器的效率与性能，其中热池的大小更是直接决定了它的最大并发量。一般情况，我们会将冷池设置得相对较大，而热池则设置得相对较小，这是因为`Xn`的持久化操作只会发生在"券码"从冷池传输到热池的过程，也就是说在每次服务重启时都会将存储在热池中的"券码"丢失。因此，在对冷池和热池的大小进行调整时，不但需要考虑生成器并发量的问题，还需要考虑它的使用寿命问题（避免频繁重启导致大量生成的"券码"被浪费）。

综上所述，对`coupon-code`最终设计的架构图如下所示:

```text
 ┌────────────────────────────────────────────────────────────────────────┐
 │                           CouponCodePool                               │
 │ ┌────────────┐  ┌─────────────┐  ┌──────────────────┐  ┌─────────────┐ │
 │ │            │  │             │  │                  │  │             │ │
 │ │            │  │             ◄──┼       LCG        │  │             │ │
 │ │            │  │             │  │                  │  │             │ │
 │ │            │  │             │  └──────────────────┘  │             │ │
◄┼─┼  Hot pool  ◄──┼  Cold pool  │                        │ Report cfg  │ │
 │ │            │  │             │  ┌──────────────────┐  │             │ │
 │ │            │  │             ┼──►                  │  │             │ │
 │ │            │  │             │  │ State memorizer  │  │             │ │
 │ │            │  │             ◄──┼                  │  │             │ │
 │ └────────────┘  └─────────────┘  └──────────────────┘  └─────────────┘ │
 └────────────────────────────────────────────────────────────────────────┘
```

## 使用

在使用时，首先我们需要创建出`CouponCodePool`实例，这就需要先创建出`ICodeGen`实例让我们可以对"券池"的状态进行实例化，具体实现可参考对应的实现类。接着，我们再将`ICodeGen`实例传入到`CouponCodePool`完成实例的创建，即：

```java
ICodeGen codeGen = ...;
CouponCodePool couponCodePool = new CouponCodePool(codeGen);
```

> 除此之外，我们还可以在创建`CouponCodePool`实例时指定热池和冷池的大小。

在完成`CouponCodePool`实例的创建后，我们就需要调用`CouponCodePool#init`方法来执行"券池"的初始化，即：

```java
CouponCodePool couponCodePool = ...;
couponCodePool.init();
```

> 与此同时，在实例销毁或者服务关闭时我们也需要调用`CouponCodePool#destroy`方法来执行"券池"的销毁，

最后，在完成初始化后我们就可以通过`CouponCodePool#next`方法来执行"券码"的生成了，即：

```sql
String couponCode = couponCodePool.next();
```

当然，如果我们使用`Spring`容器来管理`CouponCodePool`实例的话，整体的使用流程就简单的多（在声明`bean`实例时可以指定其中的初始化方法和销毁方法），即：

```java
@Bean(initMethod = "init", destroyMethod = "destroy")
public CouponCodePool couponCodePool(...) {
    ICodeGen codeGen = ...;
    return new CouponCodePool(codeGen);
}
```

在需要获取"券码"的地方注入`CouponCodePool`的`bean`实例，然后调用`CouponCodePool#next`方法获取即可：

```java
@Service
public class XxxBizService {

    @Autowired
    private CouponCodePool couponCodePool;
    
    public void bizMethod() {
        
        // 业务逻辑...
        
        String couponCode = couponCodePool.next();
        
        // 业务逻辑... 
        
    }
}
```

## 扩展

### 可选的生成策略

除此之外，在设计过程中还考虑过以下的候选方案，只不过它们并没有很好地符合当前的需求。

- UUID策略
- 雪花策略

#### UUID策略

UUID策略是一种用于生成唯一标识符的生成策略，其中对该策略生成标识符我们可称之为`UUID`（`Universally Unique Identifier`）。在构造上，`UUID`是一个由`128`位二进制组成，以最经典的`OSF`版`UUID`为例，其结构如下所示:

| Name          | Offset | Length               | Description                           |
|---------------|--------|----------------------|---------------------------------------|
| time_low      | 0x00   | 4 octets / 32 bits   | The low field of the timestamp.       |
| time_mid      | 0x04   | 2 octets / 16 bits   | The middle field of the timestamp.    |
| version       | 0x06   | 1/4 octets / 4 bits  | The version number.                   |
| time_hi       | 0x06   | 3/4 octets / 12 bits | The high field of the timestamp.      |
| variant       | 0x08   | 1/4 octets / 2 bits  | The variant.                          |
| clock_seq_hi  | 0x08   | 3/4 octets / 6 bits  | The high field of the clock sequence. |
| clock_seq_low | 0x09   | 1 octet / 8 bits     | The low field of the clock sequence.  |
| node          | 0x0A   | 6 octets / 48 bits   | The spatially unique node identifier. |

除去区分不同`UUID`版本和变体的`version`字段与`variant`字段外，其主要结构由前`60`位时间戳（基于`UTC`时间计算自`1582年10月15日00:00:00.00`起`100`纳秒（`nanosecond`
）的间隔次数（每隔`100`纳秒计数一次））、中间`14`位时钟序列（避免重复`ID`的生成）和后`48`位节点标识（区分每个生成`UUID`的节点）共同组成。

> 关于时钟序列（`clock sequence`），它主要是在时钟回拨或者`node ID`发生变化时发挥作用，以避免重复`ID`的生成：
>
> - 如果发生了时钟回拨，`UUID`生成器并不能确保在大于回拨值的时间戳下没有`UUID`被生成，这时候时钟序列（`clock sequence`）必须被改变。
> - 如果`node ID`发生了改变，重新设置时钟序列（`clock sequence`）可以最大程度地降低重复`ID`生成的可能性，因为不同机器的时钟设置可能会有稍微的不同。

通过`UUID`的构造分析，我们不难得出它能在一定并发量的前提下保证生成序列的唯一性，但是对于它所生成序列的无规律性和可控性就并没有那么强了，因为在总体上看`UUID`是基于时间戳生成的，在仔细分析下也是可以发现其中的规律。另外，对于`UUID`的可控性，由于它是通过固定的`128`位二进制数共同组成的，因此我们无法将它控制在一个固定的长度上。

总的来说，`UUID`能很大程度地保持生成序列的唯一性，但是对无规律性和可控性的要求则无法被很好满足，因此它并不是"券码"生成器的最佳策略。

#### 雪花策略

雪花策略是一种被用在分布式系统生成唯一性标识符的生成策略，其中对该策略生成标识符我们可称之为雪花`ID`（`Snowflake ID`）。

在构造上，雪花`ID`是由`64`位二进制数组成，前`41`位是时间戳（从选定`epoch`算起的毫秒数）；中间`10`位是机器`ID`（在分布式环境下防止机器之间发生冲突）；最后`12`位是每台机器中的序列号（同一个时间戳下可生成`2^(13)-1`个雪花`ID`）。通过这样的结构，我们就可以在分布式环境下对每台机器每毫秒生成`2^(13)-1`个雪花`ID`（十进制数字）。更详细的结构图如下所示：

<table>
    <thead>
        <td colspan="34">Fixed header format</td>        
    </thead>
    <tbody>
        <tr>
            <td colspan="1">Offsets</td>
            <td colspan="1">Octet</td>
            <td colspan="8">0</td>
            <td colspan="8">1</td>
            <td colspan="8">2</td>
            <td colspan="8">3</td>
        </tr>
        <tr>
            <td colspan="1">Octet</td>
            <td colspan="1">Bit</td>
            <td colspan="1">0</td>
            <td colspan="1">1</td>
            <td colspan="1">2</td>
            <td colspan="1">3</td>
            <td colspan="1">4</td>
            <td colspan="1">5</td>
            <td colspan="1">6</td>
            <td colspan="1">7</td>
            <td colspan="1">8</td>
            <td colspan="1">9</td>
            <td colspan="1">10</td>
            <td colspan="1">11</td>
            <td colspan="1">12</td>
            <td colspan="1">13</td>
            <td colspan="1">14</td>
            <td colspan="1">15</td>
            <td colspan="1">16</td>
            <td colspan="1">17</td>
            <td colspan="1">18</td>
            <td colspan="1">19</td>
            <td colspan="1">20</td>
            <td colspan="1">21</td>
            <td colspan="1">22</td>
            <td colspan="1">23</td>
            <td colspan="1">24</td>
            <td colspan="1">25</td>
            <td colspan="1">26</td>
            <td colspan="1">27</td>
            <td colspan="1">28</td>
            <td colspan="1">29</td>
            <td colspan="1">30</td>
            <td colspan="1">31</td>
        </tr>
        <tr>
            <td colspan="1">0</td>
            <td colspan="1">0</td>
            <td colspan="32">Timestamp</td>
        </tr>
        <tr>
            <td colspan="1">4</td>
            <td colspan="1">32</td>
            <td colspan="10"></td>
            <td colspan="10">Machine ID</td>
            <td colspan="22">Machine Sequence Number</td>
        </tr>
    </tbody>
</table>

> 需要注意，在雪花`ID`的`64`位二进制数组成上，由于有符号数其第`1`位是符号位（默认是`0`表示正数），只有后`63`位被使用来存储数据，即`1`位符号段+`41`位时间段+`10`位机器`ID`段+`12`位序列号段。而无符号数并没有符号位，因此`64`位都可被使用来存储数据，即`42`位时间段+`10`位机器`ID`段+`12`位序列号段。除此之外，对于机器`ID`段和序列号段的位数也不是固定的，我们可以根据项目的实际情况对它们进行一定的调整。

通过雪花`ID`的构造分析，我们不难得出它也能在一定并发量的前提下保证生成序列的唯一性，但是对于它所生成序列的无规律性和可控性就并没有那么强了，因为在总体上看雪花`ID`呈现出了基于时间的有序性，这种有序性的规律对于使用雪花`ID`的开发者们都是十分显眼的。另外，对于雪花`ID`的可控性，由于它是通过固定的`64`位二进制数共同组成的，因此我们也无法将它控制在一个固定的长度上。

总的来说，雪花`ID`能很大程度地保持生成序列的唯一性，但是对无规律性和可控性的要求则无法被很好满足，因此它也并不是"券码"生成器的最佳策略。

### 本原元(`primitive element`)

对于本原元(`primitive element`)的概念，如果缺少与之相关的数学基础看起来会十分迷惑，下面我们将结合一些数学的基础概念对本原元(`primitive element`)进行阐述。

#### 阿贝尔群(`Abelian group`)

在数学中，如果在集合`A`中存在一种操作`·`（`·`为具体操作的一个占位符）使得`A`中任意两个元素`a`和`b`可以在操作`·`下（可表示为`a · b`）生成`A`中的另一个元素，那么这个集合就被称为阿贝尔群(`Abelian group`)，即`(A, ·)`。除此之外，如果一个集合可被称为为阿贝尔群(`Abelian group`)，那么它需要同时符合以下条件：

- 结合律：对于集合`A`中的所有元素都有 (a · b) · c = a · (b · c).
- 交换律：对于集合`A`中的所有元素都有 a · b = b · a.
- 单位元：在集合`A`中存在一个元素`e`使得集合`A`中所有元素`a`都有 e · a = a · e = a.
- 逆元：对于集合`A`中每一个元素`a`都存在一个集合`A`中的元素`b`使得 a · b = b · a = e，其中`e`为单位元.

#### 域（`Field`）

在数学中，一个域（`field`）是一个具有加、减、乘、除操作定义的集合`F`，在`F`中每个有序的"元素对"在`F`定义的加、减、乘、除操作下都有唯一一个在`F`中的元素与之相对应，即`F × F → F`。除此之外，这些操作还需要满足下列属性，称为域公理：

- 加法结合律和乘法结合律：a + (b + c) = (a + b) + c； a * (b * c) = (a * b) * c.
- 加法交换律和乘法交换律：a + b = b + a； a * b = b * a.
- 乘法分配律(基于加法)：a * (b + c) = (a * b) + (a * c).
- 加法恒等式和乘法恒等式：在`F`存在两个不同的元素`0`和`1`使得 a + 0 = a; a * 1 = a.
- 加法逆元：在`F`中的每一个`a`都有与之相对应一个`-a`(`a`的加法逆元)使得 a + (−a) = 0.
- 乘法逆元：在`F`中的每一个`a`(`a != 0`)都有与之相对应一个`a^(-1)`(`a`的乘法逆元)使得 a * a^(-1) = 1.

举个例子，我们过去学习的有理数和实数也是一种域，即有理数域和实数域；在有理数集合中，我们将任意的"有理数对"传入加、减、乘、除操作中进行运算都能得到一个有理数，而且对于上述域公理有理数也能满足，即：

- 有理数 + 有理数 = 有理数
- 有理数 - 有理数 = 有理数
- 有理数 * 有理数 = 有理数
- 有理数 / 有理数 = 有理数

那么该有理数集合就是一个域，我们可称之为有理数域。

同理，对于实数集合也能符合上述条件，即：

- 实数 + 实数 = 实数
- 实数 - 实数 = 实数
- 实数 * 实数 = 实数
- 实数 / 实数 = 实数

因此实数集合也是一个域，我们可称之为实数域。

综合上述，结合域（`Field`）和阿贝尔群(`Abelian group`)的定义，不难得出一个域在加法运算下是一个阿贝尔群(`Abelian group`)，对此我们称其为域的`additive group`。同理，一个域的非零元素在乘法运算下也是一个阿贝尔群(`Abelian group`)，我们称其为域的`multiplicative group`。

#### 有限域(`Finite fields`)

在数学中，如果一个域(`Field`)包含的元素数量是有限的，那么它就被称为有限域(`Finite fields`)。 有限域(`Finite fields`)的元素数量被称为阶(`order`)，并且只有当阶(`order`)为素数的幂(`p^k`，`p`为素数，`k`为正整数)时，有限域(`Finite fields`)才会存在。

> 假设有限域(`Finite fields`)的阶(`order`)`q=p^k`，那么这个有限域(`Finite fields`)就可以表示为`GF(q)`。

如果在有限域的`multiplicative group`中存在一个元素可以通过它的次幂来表示`multiplicative group`中所有非零元素，则称它为这个有限域域的本原元(`primitive element`)，即有限域域的`multiplicative group`中所有非零元素都可以表示为`a^i`。

> 一般来说，对于给定的有限域中是存在多个本原元(`primitive element`)的。

一般情况下，如果有限域的阶`n`为素数时，那么它也可以被表示为整数集合在模`n`下映射，即`GF(n)`或者`Z/nZ`，`{0,1,2,...,n-1}`。在这种情况下，有限域的本原元(`primitive element`)也被称为模`n`下的原根(`Primitive root`)。

#### 模`n`下的原根(`Primitive root`)

在模运算中，对于与模`n`互质的每个整数`a`(`a ∈ [1,n-1]`)，都有整数`k`使得`g^k ≡ a (mod n)`，那么`g`就被称为模`m`下的原根(`Primitive root`)。不难得出，当`n`为素数时在区间`[1,n-1]`中每个整数`a`都与其互质，也就是说在模`m`下每个整数`a`都有整数`k`使得`g^k ≡ a (mod n)`（若本原元`g`存在）。

> 当且仅当`n`为`1`,`2`,`4`,`p^k`或者`2*(p^k)`时，模`n`下的原根是存在的。其中，`p`为奇素数(`odd prime`)，`k`大于`0`(`k > 0`).

## 参考

- [Wiki《Snowflake ID》](https://en.wikipedia.org/wiki/Snowflake_ID)
- [Wiki《Universally unique identifier》](https://en.wikipedia.org/wiki/Universally_unique_identifier)
- [Wiki《Linear congruential generator》](https://en.wikipedia.org/wiki/Linear_congruential_generator)
- [Wiki《Lehmer random number generator》](https://en.wikipedia.org/wiki/Lehmer_random_number_generator)
- [Wiki《Abelian group》](https://en.wikipedia.org/wiki/Abelian_group)
- [Wiki《Multiplicative group》](https://en.wikipedia.org/wiki/Multiplicative_group)
- [Wiki《Multiplicative group of integers modulo n》](https://en.wikipedia.org/wiki/Multiplicative_group_of_integers_modulo_n)
- [Wiki《Field (mathematics)》](https://en.wikipedia.org/wiki/Field_(mathematics))
- [Wiki《Finite field》](https://en.wikipedia.org/wiki/Finite_field)
- [Wiki《Primitive element (finite field)》](https://en.wikipedia.org/wiki/Primitive_element_(finite_field))
- [Wiki《Primitive root modulo n》](https://en.wikipedia.org/wiki/Primitive_root_modulo_n)
