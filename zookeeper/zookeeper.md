





# zookeeper



## 1.zookeeper介绍

### 1.1、什么是zookeeper

ZooKeeper是用于维护配置信息，命名，提供分布式同步和提供组服务的集中式服务。所有这些类型的服务都以某种形式被分布式应用程序使用。每次实施它们时，都会进行很多工作来修复不可避免的错误和争用条件。由于难以实现这类服务，因此应用程序最初通常会跳过它们，这会使它们在存在更改的情况下变得脆弱，并且难以管理。即使部署正确，这些服务的不同实现也会导致管理复杂。

### 1.2、zookeeper应用场景

zookeeper是经典的分布式数据一致性解决方案，致力于为分布式应用提供一个高新能、高可用，且具有严格顺序访问控制能力的分布式协调存储服务。

- 维护配置信息
- 分布式锁服务
- 集群管理
- 生成分布式唯一ID

**1)、维护配置信息**

​		java变成经常会遇到配置项，比如数据库的url、schema、user和password等。通常这些配置项我们会放置在配置文件中，再将配置文件放置在服务器上当需要更改配置项时，需要去服务器上修改对应的配置文件。但是随着分布式系统的兴起，由于许多服务都需要使用到该配置文件，因此又必须保证该配置服务的高可用性(high availability)和各台服务器上配置数据的一致性。通常会将配置文件部署在一个集群上，然而一个集群动辄上千台服务器，此时在一台台服务器逐个修改配置文件那将是非常繁琐且危险的操作，因此就需要一种服务，能够高效快速且可靠的完成配置项的更改等操作，并能够保证个配置项在每台服务器上的数据一致性。

​		zookeeper就是可以提供这样一种服务，其使用Zab这种一致性协议来保证一致性。现在有很多开源项目使用zookeeper来维护配置，比如在hbase中，客户端就是连接一个zookeeper，获得必要的hbase集群的配置信息，然后才可以进一步操作。还有在开源的消息队列kafka中，也是用了zookeeper来维护broker的信息。在alibaba开源的soa框架dubbo中也广泛的使用zookeeper管理一些配置来实现服务治理。

![](D:\Program Files\Typora\data\zookeeper\image\微信图片_20210314160411.png)

**2)、分布式锁服务**

​		一个集群是一个分布式系统，有多台服务器组成。为了提高并发度和可靠性，多台服务器上运行这同一种服务。当多个服务在运行时就需要协调各服务器的进度，有时候需要保障当某个服务在进行某个操作时，其他服务都不能进行该操作，即对该操作进行加锁，如果当前机器挂掉后，释放锁并fial over到其他的机器继续执行该服务。

![](D:\Program Files\Typora\data\zookeeper\image\微信图片_20210314161026.png)

**3)、集群管理**

​		一个集群有时候会因为各种软硬件故障或者网络故障，出现某些服务器挂掉而被移除集群，而某些服务器加入到集群中的情况，zookeeper会将这些服务器加入/移出的情况通知给集群中的其他正常工作的服务器，以及时调整存储和计算等任务的分配和执行等。此外zookeeper还会对故障的服务器做出诊断并尝试回复。

![](D:\Program Files\Typora\data\zookeeper\image\微信图片_20210314161500.png)

**4)、生成分布式唯一ID**

​		在过去的单裤单表型系统中，通常可以使用数据库字段自带的auto_increment属性来自动为每条记录生成一个唯一的ID。但是分库分表后，就无法在依靠数据库的auto_increment属性来唯一标识一条记录了。此时我们就可以用zookeeper的分布式环境下生成全局唯一id，做法如下：每次要生成一个新id时，创建一个持久顺序节点，创建操作返回的节点序号，即为新Id，然后把比自己节点小的删除即可。



### 1.3、zookeeper的设计目标

zookeeper致力于为分布式应用提供一个高新能、高可用，且具有严格顺序访问控制能力的分布式协调存储服务。

**1)、高性能**

​		zookeeper将全量数据存储在内存中（不是硬盘），并提供服务与客户端的所有非实物请求，尤其适用于读为主的应用场景

**2)、高可用**

​		zookeeper一般以及群的方式对外提供服务，一般3~5太机器就可以组成一个zookeeper集群，每台机器都会在内存中维护当前的服务状态，并且每台机器之间都相互保持着通信。只要集群中超过一半的机器都能正常工作，那么整个集群就都能够正常对外服务。

**3)、严格顺序访问**

​		对于来自客户端的每一个更新请求，zookeeper都会分配一个唯一的递增编号，这些编号反映了所有事物操作的先后顺序。



## 2.Zookeeper的数据模型

​		Zookeeper数据模型类似Linux操作系统的文件系统，也是以树的形式来存储。严格来说是一颗多叉树，每个节点上都可以存储数据，每个节点还可以拥有N个子结点，最上层是根节点以“/”来代表。

![](D:\Program Files\Typora\data\zookeeper\image\微信图片_20210314164018.png)

​		在每个结点上都存储了相应的数据，数据可以是字符串、二进制数。但是默认情况下每个结点的数据大小的上限是1M，这是因为Zookeeper主要是用来协调服务的，而不是存储数据，管理一些配置文件和应用列表之类的数据。虽然可以修改配置文件来改变数据大小的上限，但是为了服务的高效和稳定，建议结点数据不要超过默认值。

![](D:\Program Files\Typora\data\zookeeper\image\735119-20161121140518690-1877317886.png)

​		可以看到，在Zookeeper中存储的创建的结点和存储的数据包含结点的创建时间、修改时间、结点id、结点中存储数据的版本、权限版本、孩子结点的个数、数据的长度等信息。在创建结点的时候还可以选择临时结点、序列化节点等类型，这在应用时就非常方便了。

​		Zookeeper提供了两种客户端，命令行客户端和API客户端，关于命令行客户端的使用可以help一下。



![](D:\Program Files\Typora\data\zookeeper\image\735119-20161121142638050-230928489.png)



## 3.zookeeper单机安装

当前测试系统环境centos7.3

jdk:java8

zookeeper:zookeeper-3.4.10.tar.gz

1).在centos中使用root用户创建zookeeper用户，用户名：zookeeper 密码：zookeeper

```properties
useradd zookeeper
passwd zookeeper
```

2).zookeeper底层依赖于jdk，zookeeper用户登陆后，根目录下先进行jdk的安装，jdk使用jdk-8版本，上传并解压jdk

```properties
//解压jdk
tar -zxvf jdk-8u281-linux-x64.tar.gz
```

3).配置jdk环境变量

```properties
//vi 打开/etc/profile 文件
vi /etc/profile

//在文件最后添加java环境
#JDK全局环境变量配置
export JAVA_HOME=/src/java/jdk1.8.0_281
export CLASSPATH=$:CLASSPATH:$JAVA_HOME/lib/
export PATH=$PATH:$JAVA_HOME/bin

//:wq!  保存推出
:wq!

//重新加载etc/profile文件
source /etc/profile

//严重jdk是否配置成功
javac
java -version
```

![](D:\Program Files\Typora\data\zookeeper\image\微信图片_20210315105906.png)

如同则表示成功

4).zookeeper上传并解压

```properties
//解压zookeeper
tar -zxvf zookeeper-3.4.10.tar.gz
```

5).为zookeeper准备配置文件

```properties
//进入conf目录
cd /src/zookeeper/zookeeper-3.4.14/conf

//复制配置文件
cp zoo_sample.cfg zoo.cfg

//zookeeper根目录下创建data目录
mkdir data

//修改配置文件中的dataDir
//此路径用于存储zookeeper中数据的内存快照，及事务日志文件
dataDir=/src/zookeeper/zookeeper-3.4.14/data
```

6).启动zookeeper

```properties
// 进入zookeeper的bin目录下
cd /src/zookeeper/zookeeper-3.4.14/bin

//启动zookeeper
./zkServer.sh start

//停止zookeeper
./zkServer.sh stop

//查看zookeeper状态
zkSrver.sh status
```



## 4.zookeeper的常用shell命令

### 4.1、新增节点

```shell
create [-s] [-e] path data #其中-s 为有序节点 -e临时节点
```

创建持久化节点并写入数据

```shell
create /hadoop "123456"
```

创建持久化有序节点，此时创建的节点名为指定节点名+自增序号

```shell
[zk: localhost:2181(CONNECTED) 1] create -s /a "a"
Created /a0000000001
[zk: localhost:2181(CONNECTED) 3] get /a0000000001
[zk: localhost:2181(CONNECTED) 4] create -s /b "b"
[zk: localhost:2181(CONNECTED) 5] get /b0000000002


```

创建临时节点，临时节点会在会话过期后被删除：

```shell
[zk: localhost:2181(CONNECTED) 6] create -e /tmp "tmp"
Created /tmp
[zk: localhost:2181(CONNECTED) 7] get /tmp
tmp
cZxid = 0x9
ctime = Mon Mar 15 13:55:08 CST 2021
mZxid = 0x9
mtime = Mon Mar 15 13:55:08 CST 2021
pZxid = 0x9
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x1000042bbb40001
dataLength = 3
numChildren = 0

```

创建临时有序节点，临时节点会在会话结束后被删除：

```shell
[zk: localhost:2181(CONNECTED) 2] create -s -e /aa "aa"
Created /aa0000000004
[zk: localhost:2181(CONNECTED) 3] get /aa0000000004
aa
cZxid = 0xc
ctime = Mon Mar 15 13:59:15 CST 2021
mZxid = 0xc
mtime = Mon Mar 15 13:59:15 CST 2021
pZxid = 0xc
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x1000042bbb40002
dataLength = 2
numChildren = 0

```

### 4.2、更新节点

更新节点的命令是set，可以直接进行修改，如下：

```shell
[zk: localhost:2181(CONNECTED) 5] set /hadoop "345" 
cZxid = 0x4
ctime = Mon Mar 15 11:31:14 CST 2021
mZxid = 0xd
mtime = Mon Mar 15 17:46:21 CST 2021
pZxid = 0x4
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
[zk: localhost:2181(CONNECTED) 6] get /hadoop
345
cZxid = 0x4
ctime = Mon Mar 15 11:31:14 CST 2021
mZxid = 0xd
mtime = Mon Mar 15 17:46:21 CST 2021
pZxid = 0x4
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```

​		也可以基于版本号进行修改，此时类似于乐观锁机制，当你传入的数据版本(dataVersion)和当前节点的数据版本号不符合时，zookeeper会拒绝本次修改：

```shell
[zk: localhost:2181(CONNECTED) 8] set /hadoop "3455" 1
version No is not valid : /hadoop

```

### 4.3、删除节点

删除节点的语法如下：

```shell
delete path [version]

```



和更新节点数据一样，也可以传入版本号，当你传入的数据版本好（dataVersion）和当前节点的数据版本号不符合时，zookeeper不会执行删除操作。

```shell
[zk: localhost:2181(CONNECTED) 1] delete /hadoop 0
version No is not valid : /hadoop
[zk: localhost:2181(CONNECTED) 2] delete /hadoop 3
[zk: localhost:2181(CONNECTED) 3] get /hadoop
Node does not exist: /hadoop
```

### 4.4、查看节点

```shell
get path
```



```shell
[zk: localhost:2181(CONNECTED) 0] get /hadoop
123456
cZxid = 0x15
ctime = Mon Mar 15 18:05:13 CST 2021
mZxid = 0x15
mtime = Mon Mar 15 18:05:13 CST 2021
pZxid = 0x16
cversion = 1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 1

```

节点各个属性如下，其中一个重要的概念是Zxid(Zookeeper Transaction Id),Zookeeper节点的每一次更改都具有唯一的Zxid，如果Zxid1小于Zxid2，则Zxid1的更改发生在Zxid2之前。

| 状态属性       | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| cZxid          | 数据节点创建时的事物id                                       |
| ctime          | 数据节点创建时的时间                                         |
| mZxid          | 数据节点最后一次更改时的事物id                               |
| mtime          | 数据节点最后一次更新时的时间                                 |
| pZxid          | 数据节点的子节点最后一次被修改时的事物ID                     |
| cversion       | 子节点的更改次数                                             |
| dataVersion    | 节点数据的更改次数                                           |
| aclVersion     | 节点的ACL的更改次数                                          |
| ephemeralOwner | 如果节点是临时节点，则表示创建该节点的会话的SessionID;如果节点是持久节点，则该属性值为0 |
| dataLength     | 数据内容的长度                                               |
| numChildren    | 数据节点当前的子节点个数                                     |

### 4.5、查看节点状态

可以使用stat命令查看节点状态，他的返回值与get类似，但不会返回节点数据

```shell
[zk: localhost:2181(CONNECTED) 1] stat /hadoop
cZxid = 0x15
ctime = Mon Mar 15 18:05:13 CST 2021
mZxid = 0x15
mtime = Mon Mar 15 18:05:13 CST 2021
pZxid = 0x16
cversion = 1
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 1

```

### 4.6、查看节点列表

​		查看节点列表有ls path和ls2 path两个命令，后者是前者的增强，不仅可以查看指定路径下的所有节点，还可以查看点钱节点的信息

```shell
[zk: localhost:2181(CONNECTED) 2] ls /
[b0000000002, a0000000001, hadoop, zookeeper]
[zk: localhost:2181(CONNECTED) 3] ls2 /
[b0000000002, a0000000001, hadoop, zookeeper]
cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x15
cversion = 8
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 4

```

### 4.7、监听器get path[watch]

使用get path [watch]注册的监听器能够在节点内容发生改变的时候，向客户端发出通告。需要注意的是zookeeper的触发器是一次性的(One-time trigger),即触发一次后就会立即失效。

```shell
[zk: localhost:2181(CONNECTED) 0] get /hadoop watch
[zk: localhost:2181(CONNECTED) 1] set /hadoop "123456"
cZxid = 0x2
ctime = Wed Mar 17 23:01:53 CST 2021
mZxid = 0x6
mtime = Wed Mar 17 23:07:37 CST 2021
pZxid = 0x2
cversion = 0
dataVersion = 1
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
[zk: localhost:2181(CONNECTED) 1] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop
```

### 4.8、监听器 stat path [watch]

​		使用stat path [watch] 注册的监听器能够在节点状态发生改变的时候，向客户端发送通知

```shell
[zk: localhost:2181(CONNECTED) 1] stat /hadoop watch
[zk: localhost:2181(CONNECTED) 2] set /hadoop "hadoop"
cZxid = 0x2
ctime = Wed Mar 17 23:01:53 CST 2021
mZxid = 0x7
mtime = Wed Mar 17 23:12:58 CST 2021
pZxid = 0x2
cversion = 0
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
[zk: localhost:2181(CONNECTED) 2] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop

```

### 4.9、监听器ls/ls2 path [watch]

​		使用ls path [watch]或ls2 path [watch]注册监听器能够监听该节点下所有子节点的增加和删除操作

```shell
[zk: localhost:2181(CONNECTED) 16] ls2 /hadoop watch
[node2, node1]
cZxid = 0x2
ctime = Wed Mar 17 23:01:53 CST 2021
mZxid = 0x7
mtime = Wed Mar 17 23:12:58 CST 2021
pZxid = 0x9
cversion = 2
dataVersion = 2
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 2
[zk: localhost:2181(CONNECTED) 4] delete /hadoop/node2
[zk: localhost:2181(CONNECTED) 17] 
WATCHER::

WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hadoop

```



## 5、Zookeeper的acl权限控制

### 5.1、概念

​		zookeeper类似文件系统，client可以创建文件节点、更新节点、删除节点，那么如何做到节点的权限控制呢？zookeeper的access control list 访问控制列表可以做到这一点。

​		acl权限控制，使用scheme : id : permission 来标识，主要涵盖3个方面：

- 权限模式(scheme)：权限的策略

- 权限对象(id)：权限的对象

- 权限(permission)：授予的权限

  其特性如下：

- zookeeper的权限控制是基于每个znode节点的，需要对每个节点设置权限

- 每个znode支持设置多中权限控制方案和多个权限

- 子节点不会继承父节点的权限，客户端不能访问某个节点，但可能可以访问他的某个子节点

例如：

```shell
setAcl /test2 ip:192.168.60.130:crwda //将节点权限设置为ip：192.168.60.130的客户端可以对节点进行增删改查管理权限
```

### 5.2、权限模式

​		采用何种方式授权

| 方案   | 描述l                                                   |
| ------ | ------------------------------------------------------- |
| world  | 只有一个用户：anyone，代表登录的zookeeper所有人（默认） |
| ip     | 对客户端使用ip地址认证                                  |
| auth   | 使用已添加认证的用户认证                                |
| digest | 使用“用户名:密码”方式认证                               |

### 5.3、授权的对象

给谁授予权限

授权对象id是指，权限赋予的实体，例如：ip地址或用户

### 5.4、授予的权限

​		授予什么权限

​		create、delete、read、writer、admin也就是增删改查管理权限，这五种权限简写为cdrwa，注意：这五种权限中，delete值对节点的删除权限，其他4中指对节点自身的操作权限

| 权限   | Acl简写 | 描述                         |
| ------ | ------- | ---------------------------- |
| create | c       | 可以创建节点                 |
| delete | d       | 可以删除节点（仅下一级节点） |
| read   | r       | 可以读取数据及显示子节点列表 |
| writer | w       | 可以设置节点数据             |
| admin  | a       | 可以设置节点访问控制列表权限 |

### 5.5、授权的相关命令

| 命令    | 使用方式                | 描述         |
| ------- | ----------------------- | ------------ |
| getAcl  | getAcl <path>           | 读取acl权限  |
| setAcl  | setAcl <path> <acl>     | 设置acl权限  |
| addauth | addauth <schmem> <auth> | 添加认证用户 |

### 5.6、案例

- world授权模式:

  命令

  ```shell
  setAcl <path> world:anyone:<acl>
  ```

  案例

  ```shell
  [zk: localhost:2181(CONNECTED) 9] create /node1 "node1"
  Created /node1
  [zk: localhost:2181(CONNECTED) 10] getAcl /node1
  'world,'anyone
  : cdrwa
  [zk: localhost:2181(CONNECTED) 11] create /node1/node11 "node11"
  Created /node1/node11
  [zk: localhost:2181(CONNECTED) 12] create /node1/node22 "node22"
  Created /node1/node22
  [zk: localhost:2181(CONNECTED) 13] ls /node1
  [node11, node22]
  [zk: localhost:2181(CONNECTED) 14] get /node1/node
  
  node11   node22
  [zk: localhost:2181(CONNECTED) 14] get /node1/node11
  node11
  cZxid = 0xf
  ctime = Sun Apr 04 14:00:03 CST 2021
  mZxid = 0xf
  mtime = Sun Apr 04 14:00:03 CST 2021
  pZxid = 0xf
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 15] get /node1/node22
  node22
  cZxid = 0x10
  ctime = Sun Apr 04 14:00:18 CST 2021
  mZxid = 0x10
  mtime = Sun Apr 04 14:00:18 CST 2021
  pZxid = 0x10
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 16] setAcl /node1 world:anyone:drwa
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0xe
  mtime = Sun Apr 04 13:58:50 CST 2021
  pZxid = 0x10
  cversion = 2
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 2
  [zk: localhost:2181(CONNECTED) 17] getAcl /node1
  'world,'anyone
  : drwa
  [zk: localhost:2181(CONNECTED) 18] create /node1/node33 "node33"
  Authentication is not valid : /node1/node33
  [zk: localhost:2181(CONNECTED) 19] 
  [zk: localhost:2181(CONNECTED) 19] 
  [zk: localhost:2181(CONNECTED) 19] 
  [zk: localhost:2181(CONNECTED) 19] delete /node1/node22
  [zk: localhost:2181(CONNECTED) 20]   
  [zk: localhost:2181(CONNECTED) 20] 
  [zk: localhost:2181(CONNECTED) 20] 
  [zk: localhost:2181(CONNECTED) 20] ls /node1 
  [node11]
  [zk: localhost:2181(CONNECTED) 21] setAcl /node1 world:anyone:rwa
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0xe
  mtime = Sun Apr 04 13:58:50 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 0
  aclVersion = 2
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 22] getAcl /node1
  'world,'anyone
  : rwa
  [zk: localhost:2181(CONNECTED) 23] delete /node1/node11
  Authentication is not valid : /node1/node11
  [zk: localhost:2181(CONNECTED) 24] get /node1
  node1
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0xe
  mtime = Sun Apr 04 13:58:50 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 0
  aclVersion = 2
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 25] setAcl /node1 world:anyone:wa
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0xe
  mtime = Sun Apr 04 13:58:50 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 0
  aclVersion = 3
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 26] getAcl /node1
  'world,'anyone
  : wa
  [zk: localhost:2181(CONNECTED) 27] get /node1
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 28] get /node1/node11
  node11
  cZxid = 0xf
  ctime = Sun Apr 04 14:00:03 CST 2021
  mZxid = 0xf
  mtime = Sun Apr 04 14:00:03 CST 2021
  pZxid = 0xf
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 29] set /node1 "node11"
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0x17
  mtime = Sun Apr 04 14:06:20 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 1
  aclVersion = 3
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 30] get /node1
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 31] setAcl /node1 world:anyone:a
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0x17
  mtime = Sun Apr 04 14:06:20 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 1
  aclVersion = 4
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 32] set /node1 "node111"
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 33] rmr /node1
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 34] setAcl /node1 world:anyone:
  cZxid = 0xe
  ctime = Sun Apr 04 13:58:50 CST 2021
  mZxid = 0x17
  mtime = Sun Apr 04 14:06:20 CST 2021
  pZxid = 0x13
  cversion = 3
  dataVersion = 1
  aclVersion = 5
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 1
  [zk: localhost:2181(CONNECTED) 35] getAcl /node1
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 36] setAcl /node1 world:anyone:cdrwa
  Authentication is not valid : /node1
  [zk: localhost:2181(CONNECTED) 37] 
  
  ```
  
- ip授权模式

  命令：

  ```shell
  setAcl <path>   ip:<ip>:<acl>
  ```

  注：远程登陆zookeeper服务器命令：./zkCli.sh -server <ip>

  注：这里远程服务器的防火墙如果没有关闭，需要在防火墙中打开zookeeper服务的端口2181	

  ```shell
  #例
  ./zkCli.sh -server 192.168.10.6	
  ```

  ```shell
  #192.168.10.6
  [zk: localhost:2181(CONNECTED) 7] ls /
  [hadoop, zookeeper, node1]
  [zk: localhost:2181(CONNECTED) 8] create /node2 "node2"
  Created /node2
  [zk: localhost:2181(CONNECTED) 9] getAcl /node2
  'world,'anyone
  : cdrwa
  [zk: localhost:2181(CONNECTED) 10] get /node2
  node2
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 11] setAcl /node2 ip:192.168.10.6:cdrwa,ip:192.168.10.7:cdrwa
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  
  #192.168.10.7
  [root@192 bin]# ./zkCli.sh -server 192.168.10.6
  Connecting to 192.168.10.6
  2021-04-04 15:56:39,349 [myid:] - INFO  [main:Environment@100] - Client environment:zookeeper.version=3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  2021-04-04 15:56:39,354 [myid:] - INFO  [main:Environment@100] - Client environment:host.name=192.168.10.7
  2021-04-04 15:56:39,355 [myid:] - INFO  [main:Environment@100] - Client environment:java.version=1.8.0_281
  2021-04-04 15:56:39,356 [myid:] - INFO  [main:Environment@100] - Client environment:java.vendor=Oracle Corporation
  2021-04-04 15:56:39,356 [myid:] - INFO  [main:Environment@100] - Client environment:java.home=/src/java/jdk1.8.0_281/jre
  2021-04-04 15:56:39,356 [myid:] - INFO  [main:Environment@100] - Client environment:java.class.path=/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/classes:/src/zookeeper/zookeeper-3.4.14/bin/../build/classes:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../build/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-log4j12-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-api-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/netty-3.10.6.Final.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/log4j-1.2.17.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/jline-0.9.94.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/audience-annotations-0.5.0.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-3.4.14.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/src/main/resources/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../conf:$:CLASSPATH:/src/java/jdk1.8.0_281/lib/
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:java.io.tmpdir=/tmp
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:java.compiler=<NA>
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:os.name=Linux
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:os.arch=amd64
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:os.version=3.10.0-1160.el7.x86_64
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:user.name=root
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:user.home=/root
  2021-04-04 15:56:39,357 [myid:] - INFO  [main:Environment@100] - Client environment:user.dir=/src/zookeeper/zookeeper-3.4.14/bin
  2021-04-04 15:56:39,358 [myid:] - INFO  [main:ZooKeeper@442] - Initiating client connection, connectString=192.168.10.6 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@446cdf90
  Welcome to ZooKeeper!
  JLine support is enabled
  2021-04-04 15:56:39,398 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1025] - Opening socket connection to server 192.168.10.6/192.168.10.6:2181. Will not attempt to authenticate using SASL (unknown error)
  2021-04-04 15:56:39,402 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@879] - Socket connection established to 192.168.10.6/192.168.10.6:2181, initiating session
  2021-04-04 15:56:39,458 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1299] - Session establishment complete on server 192.168.10.6/192.168.10.6:2181, sessionid = 0x1000058ba9f0000, negotiated timeout = 30000
  
  WATCHER::
  
  WatchedEvent state:SyncConnected type:None path:null
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] 
  [zk: 192.168.10.6(CONNECTED) 0] get /node2
  node2
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x1f
  mtime = Sun Apr 04 15:12:13 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 1] getAcl /node2
  'world,'anyone
  : cdrwa
  [zk: 192.168.10.6(CONNECTED) 2] getAcl /node2
  'ip,'192.168.10.7
  : cdrwa
  [zk: 192.168.10.6(CONNECTED) 3] get /node2
  node2
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x1f
  mtime = Sun Apr 04 15:12:13 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 4] set /node2 "node21"
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x21
  mtime = Sun Apr 04 15:15:50 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 1
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 5] get /node2
  node21
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x21
  mtime = Sun Apr 04 15:15:50 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 1
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 6
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 6] set /node2 ip:192.168.10.6:cdrwa
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x22
  mtime = Sun Apr 04 15:17:17 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 2
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 21
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 7] getAcl /node2
  'ip,'192.168.10.7
  : cdrwa
  [zk: 192.168.10.6(CONNECTED) 8] setAcl /node2 ip:192.168.10.6:cdrwa
  cZxid = 0x1f
  ctime = Sun Apr 04 15:12:13 CST 2021
  mZxid = 0x22
  mtime = Sun Apr 04 15:17:17 CST 2021
  pZxid = 0x1f
  cversion = 0
  dataVersion = 2
  aclVersion = 2
  ephemeralOwner = 0x0
  dataLength = 21
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 9] getAcl /node2
  Authentication is not valid : /node2
  [zk: 192.168.10.6(CONNECTED) 10] getAcl /node2
  Authentication is not valid : /node2
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] 
  [zk: 192.168.10.6(CONNECTED) 11] ls /
  [hadoop, zookeeper, node1]
  [zk: 192.168.10.6(CONNECTED) 12] get /node2
  node2
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 13] get /node2
  node2
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 14] getAcl /node2
  'ip,'192.168.10.6
  : cdrwa
  'ip,'192.168.10.7
  : cdrwa
  [zk: 192.168.10.6(CONNECTED) 15] 
  
  ```

  Auth授权模式

  命令

  ```shell
  addauth digest <user>:<password>#添加认证用户
  setAcl <path> auth:<user><acl>
  ```

  

  案例

  ```shell
  [zk: localhost:2181(CONNECTED) 13] ls /
  [node2, hadoop, zookeeper, node1]
  [zk: localhost:2181(CONNECTED) 14] create /node3 "node3"
  Created /node3
  [zk: localhost:2181(CONNECTED) 15] get /node3
  node3
  cZxid = 0x2e
  ctime = Sun Apr 04 15:34:16 CST 2021
  mZxid = 0x2e
  mtime = Sun Apr 04 15:34:16 CST 2021
  pZxid = 0x2e
  cversion = 0
  dataVersion = 0
  aclVersion = 0
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 16] getAcl /node3
  'world,'anyone
  : cdrwa
  [zk: localhost:2181(CONNECTED) 17] addauth digest admin:123456
  [zk: localhost:2181(CONNECTED) 18] setAcl /node3 auth:admin:cdrwa
  cZxid = 0x2e
  ctime = Sun Apr 04 15:34:16 CST 2021
  mZxid = 0x2e
  mtime = Sun Apr 04 15:34:16 CST 2021
  pZxid = 0x2e
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 19] getAcl /node3
  'digest,'admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
  : cdrwa
  [zk: localhost:2181(CONNECTED) 20] get /node2
  Authentication is not valid : /node2
  [zk: localhost:2181(CONNECTED) 21] get /node3
  node3
  cZxid = 0x2e
  ctime = Sun Apr 04 15:34:16 CST 2021
  mZxid = 0x2e
  mtime = Sun Apr 04 15:34:16 CST 2021
  pZxid = 0x2e
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: localhost:2181(CONNECTED) 22] quit
  Quitting...
  2021-04-04 15:36:53,612 [myid:] - INFO  [main:ZooKeeper@693] - Session: 0x1000058ba9f0002 closed
  2021-04-04 15:36:53,614 [myid:] - INFO  [main-EventThread:ClientCnxn$EventThread@522] - EventThread shut down for session: 0x1000058ba9f0002
  [root@192 bin]# ./zkCli.sh
  Connecting to localhost:2181
  2021-04-04 15:37:00,655 [myid:] - INFO  [main:Environment@100] - Client environment:zookeeper.version=3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  2021-04-04 15:37:00,658 [myid:] - INFO  [main:Environment@100] - Client environment:host.name=192.168.10.6
  2021-04-04 15:37:00,658 [myid:] - INFO  [main:Environment@100] - Client environment:java.version=1.8.0_281
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.vendor=Oracle Corporation
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.home=/src/java/jdk1.8.0_281/jre
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.class.path=/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/classes:/src/zookeeper/zookeeper-3.4.14/bin/../build/classes:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../build/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-log4j12-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-api-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/netty-3.10.6.Final.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/log4j-1.2.17.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/jline-0.9.94.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/audience-annotations-0.5.0.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-3.4.14.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/src/main/resources/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../conf:$:CLASSPATH:/src/java/jdk1.8.0_281/lib/
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.io.tmpdir=/tmp
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:java.compiler=<NA>
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:os.name=Linux
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:os.arch=amd64
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:os.version=3.10.0-1160.el7.x86_64
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:user.name=root
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:user.home=/root
  2021-04-04 15:37:00,661 [myid:] - INFO  [main:Environment@100] - Client environment:user.dir=/src/zookeeper/zookeeper-3.4.14/bin
  2021-04-04 15:37:00,662 [myid:] - INFO  [main:ZooKeeper@442] - Initiating client connection, connectString=localhost:2181 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@446cdf90
  2021-04-04 15:37:00,687 [myid:] - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@1025] - Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (unknown error)
  2021-04-04 15:37:00,692 [myid:] - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@879] - Socket connection established to localhost/127.0.0.1:2181, initiating session
  Welcome to ZooKeeper!
  JLine support is enabled
  2021-04-04 15:37:00,798 [myid:] - INFO  [main-SendThread(localhost:2181):ClientCnxn$SendThread@1299] - Session establishment complete on server localhost/127.0.0.1:2181, sessionid = 0x1000058ba9f0004, negotiated timeout = 30000
  
  WATCHER::
  
  WatchedEvent state:SyncConnected type:None path:null
  [zk: localhost:2181(CONNECTED) 0] get /node3
  Authentication is not valid : /node3
  [zk: localhost:2181(CONNECTED) 1] quit
  Quitting...
  2021-04-04 15:37:30,660 [myid:] - INFO  [main:ZooKeeper@693] - Session: 0x1000058ba9f0004 closed
  2021-04-04 15:37:30,662 [myid:] - INFO  [main-EventThread:ClientCnxn$EventThread@522] - EventThread shut down for session: 0x1000058ba9f0004
  [root@192 bin]# ./zkCli.sh -server 192.168.10.6
  Connecting to 192.168.10.6
  2021-04-04 15:37:46,690 [myid:] - INFO  [main:Environment@100] - Client environment:zookeeper.version=3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  2021-04-04 15:37:46,692 [myid:] - INFO  [main:Environment@100] - Client environment:host.name=192.168.10.6
  2021-04-04 15:37:46,693 [myid:] - INFO  [main:Environment@100] - Client environment:java.version=1.8.0_281
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.vendor=Oracle Corporation
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.home=/src/java/jdk1.8.0_281/jre
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.class.path=/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/classes:/src/zookeeper/zookeeper-3.4.14/bin/../build/classes:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../build/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-log4j12-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-api-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/netty-3.10.6.Final.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/log4j-1.2.17.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/jline-0.9.94.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/audience-annotations-0.5.0.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-3.4.14.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/src/main/resources/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../conf:$:CLASSPATH:/src/java/jdk1.8.0_281/lib/
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.io.tmpdir=/tmp
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:java.compiler=<NA>
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:os.name=Linux
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:os.arch=amd64
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:os.version=3.10.0-1160.el7.x86_64
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:user.name=root
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:user.home=/root
  2021-04-04 15:37:46,695 [myid:] - INFO  [main:Environment@100] - Client environment:user.dir=/src/zookeeper/zookeeper-3.4.14/bin
  2021-04-04 15:37:46,696 [myid:] - INFO  [main:ZooKeeper@442] - Initiating client connection, connectString=192.168.10.6 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@446cdf90
  Welcome to ZooKeeper!
  JLine support is enabled
  2021-04-04 15:37:46,744 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1025] - Opening socket connection to server 192.168.10.6/192.168.10.6:2181. Will not attempt to authenticate using SASL (unknown error)
  2021-04-04 15:37:46,750 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@879] - Socket connection established to 192.168.10.6/192.168.10.6:2181, initiating session
  2021-04-04 15:37:46,802 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1299] - Session establishment complete on server 192.168.10.6/192.168.10.6:2181, sessionid = 0x1000058ba9f0005, negotiated timeout = 30000
  
  WATCHER::
  
  WatchedEvent state:SyncConnected type:None path:null
  [zk: 192.168.10.6(CONNECTED) 0] get /node2
  node2
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 1] get /node3
  Authentication is not valid : /node3
  [zk: 192.168.10.6(CONNECTED) 2] addauth digest admin:123456
  [zk: 192.168.10.6(CONNECTED) 3] get /node3
  node3
  cZxid = 0x2e
  ctime = Sun Apr 04 15:34:16 CST 2021
  mZxid = 0x2e
  mtime = Sun Apr 04 15:34:16 CST 2021
  pZxid = 0x2e
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 4] get /node2
  node2
  cZxid = 0x2a
  ctime = Sun Apr 04 15:24:33 CST 2021
  mZxid = 0x2a
  mtime = Sun Apr 04 15:24:33 CST 2021
  pZxid = 0x2a
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 5] 
  
  ```

  Digest授权模式

  命令

  ```shell
  setAcl <path> digest:<user>:<password>:<acl>
  ```

  这里的密码是经过SHA1及BASE64处理的密文，在SHELL中可以通过一下命令计算:

  ```shell
  echo -n <user>:<password> | openssl dgst -binary -sha1 | openssl base64
  ```

  先来计算一个密文

  ```shell
  echo -n admin:123456 | openssl dgst -binary -sha1 | openssl base64
  ```

  案例

  ```shell
  [root@192 ~]# echo -n admin:123456 | openssl dgst -binary -sha1 | openssl base64
  0uek/hZ/V9fgiM35b0Z2226acMQ=
  [root@192 ~]# 
  
  
  [zk: 192.168.10.6(CONNECTED) 10] create /node4 "node4"
  Created /node4
  [zk: 192.168.10.6(CONNECTED) 11] setAcl /node4 digest:admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
  Unknown perm type: 0
  Unknown perm type: u
  Unknown perm type: e
  Unknown perm type: k
  Unknown perm type: /
  Unknown perm type: h
  Unknown perm type: Z
  Unknown perm type: /
  Unknown perm type: V
  Unknown perm type: 9
  Unknown perm type: f
  Unknown perm type: g
  Unknown perm type: i
  Unknown perm type: M
  Unknown perm type: 3
  Unknown perm type: 5
  Unknown perm type: b
  Unknown perm type: 0
  Unknown perm type: Z
  Unknown perm type: 2
  Unknown perm type: 2
  Unknown perm type: 2
  Unknown perm type: 6
  Unknown perm type: M
  Unknown perm type: Q
  Unknown perm type: =
  Acl is not valid : /node4
  [zk: 192.168.10.6(CONNECTED) 12] setAcl /node4 digest:admin:0uek/hZ/V9fgiM35b0Z2226acMQ=:cdrwa
  cZxid = 0x35
  ctime = Sun Apr 04 15:57:20 CST 2021
  mZxid = 0x35
  mtime = Sun Apr 04 15:57:20 CST 2021
  pZxid = 0x35
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 13] getAcl /node4
  'digest,'admin:0uek/hZ/V9fgiM35b0Z2226acMQ=
  : cdrwa
  [zk: 192.168.10.6(CONNECTED) 14] get /node4
  node4
  cZxid = 0x35
  ctime = Sun Apr 04 15:57:20 CST 2021
  mZxid = 0x35
  mtime = Sun Apr 04 15:57:20 CST 2021
  pZxid = 0x35
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 15] quit
  Quitting...
  2021-04-04 16:00:11,076 [myid:] - INFO  [main:ZooKeeper@693] - Session: 0x1000058ba9f0005 closed
  2021-04-04 16:00:11,078 [myid:] - INFO  [main-EventThread:ClientCnxn$EventThread@522] - EventThread shut down for session: 0x1000058ba9f0005
  [root@192 bin]# ./zkCli.sh -server 192.168.10.6
  Connecting to 192.168.10.6
  2021-04-04 16:00:32,511 [myid:] - INFO  [main:Environment@100] - Client environment:zookeeper.version=3.4.14-4c25d480e66aadd371de8bd2fd8da255ac140bcf, built on 03/06/2019 16:18 GMT
  2021-04-04 16:00:32,514 [myid:] - INFO  [main:Environment@100] - Client environment:host.name=192.168.10.6
  2021-04-04 16:00:32,514 [myid:] - INFO  [main:Environment@100] - Client environment:java.version=1.8.0_281
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.vendor=Oracle Corporation
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.home=/src/java/jdk1.8.0_281/jre
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.class.path=/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/classes:/src/zookeeper/zookeeper-3.4.14/bin/../build/classes:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/target/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../build/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-log4j12-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/slf4j-api-1.7.25.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/netty-3.10.6.Final.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/log4j-1.2.17.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/jline-0.9.94.jar:/src/zookeeper/zookeeper-3.4.14/bin/../lib/audience-annotations-0.5.0.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-3.4.14.jar:/src/zookeeper/zookeeper-3.4.14/bin/../zookeeper-server/src/main/resources/lib/*.jar:/src/zookeeper/zookeeper-3.4.14/bin/../conf:$:CLASSPATH:/src/java/jdk1.8.0_281/lib/
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.library.path=/usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.io.tmpdir=/tmp
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:java.compiler=<NA>
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:os.name=Linux
  2021-04-04 16:00:32,516 [myid:] - INFO  [main:Environment@100] - Client environment:os.arch=amd64
  2021-04-04 16:00:32,517 [myid:] - INFO  [main:Environment@100] - Client environment:os.version=3.10.0-1160.el7.x86_64
  2021-04-04 16:00:32,517 [myid:] - INFO  [main:Environment@100] - Client environment:user.name=root
  2021-04-04 16:00:32,517 [myid:] - INFO  [main:Environment@100] - Client environment:user.home=/root
  2021-04-04 16:00:32,517 [myid:] - INFO  [main:Environment@100] - Client environment:user.dir=/src/zookeeper/zookeeper-3.4.14/bin
  2021-04-04 16:00:32,518 [myid:] - INFO  [main:ZooKeeper@442] - Initiating client connection, connectString=192.168.10.6 sessionTimeout=30000 watcher=org.apache.zookeeper.ZooKeeperMain$MyWatcher@446cdf90
  Welcome to ZooKeeper!
  JLine support is enabled
  2021-04-04 16:00:32,566 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1025] - Opening socket connection to server 192.168.10.6/192.168.10.6:2181. Will not attempt to authenticate using SASL (unknown error)
  2021-04-04 16:00:32,571 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@879] - Socket connection established to 192.168.10.6/192.168.10.6:2181, initiating session
  2021-04-04 16:00:32,631 [myid:] - INFO  [main-SendThread(192.168.10.6:2181):ClientCnxn$SendThread@1299] - Session establishment complete on server 192.168.10.6/192.168.10.6:2181, sessionid = 0x1000058ba9f0006, negotiated timeout = 30000
  
  WATCHER::
  
  WatchedEvent state:SyncConnected type:None path:null
  [zk: 192.168.10.6(CONNECTED) 0] get /node4
  Authentication is not valid : /node4
  [zk: 192.168.10.6(CONNECTED) 1] addauth digest admin:123456
  [zk: 192.168.10.6(CONNECTED) 2] get /node4
  node4
  cZxid = 0x35
  ctime = Sun Apr 04 15:57:20 CST 2021
  mZxid = 0x35
  mtime = Sun Apr 04 15:57:20 CST 2021
  pZxid = 0x35
  cversion = 0
  dataVersion = 0
  aclVersion = 1
  ephemeralOwner = 0x0
  dataLength = 5
  numChildren = 0
  [zk: 192.168.10.6(CONNECTED) 3] 
  
  ```

  多种授权模式

  同一个节点可以同时拥有多种模式授权

### 5.7、acl超级管理员

zookeeper的权限管理模式有一种叫做super，该模式提供一个超管可以方便的访问任何权限的节点假设这个超管是：super:admin,需要先为超管生成密码的密文

```shell
echo -n super:admin | openssl dgst -binary -sha1 | openssl base64
```

那么打开zookeeper目录下的/bin/zkServer.sh服务器脚本文件，找到如下行：

```shell
nohup $JAVA "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}"
```



## 6、zookeeper JavaAPI

​		znode是zookeeper集合的核心组件，zookeeper API提供了以小组方法使用zookeeper集合来操纵znode的所有细节。

客户端应该遵循以步骤，与zookeeper服务器进行清晰和干净的交互。

- 链接到zookeeper服务器。zookeeper服务器为客户端分配绘画ID。
- 定期向服务器发送心跳。否则zookeeper服务器将过期绘画ID，客户端需要重新连接。
- 只要会话ID处于活动状态，就可以获取/设置znode。
- 所有任务完成后，断开zookeeper服务器的链接。如果客户端长时间不活动，则zookeeper服务器将自动断开客户端。

### 6.1、链接到zookeeper

```java
Zookeeper(String connectionString,int sessionTimeout,Watcher watcher)
```

- **connectionString** - zookeeper主机
- **sesseionTimeout** - 会话超时（以毫秒为单位）
- **watcher** - 实现“监视器”对象。zookeeper集合通过监视器对象返回连接状态。

案例：

```java
package com.bai.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/5 15:34
 */
public class ZookeeperConnection {

    public static void main(String[] args) {
        try {
            //计数器对象
            CountDownLatch countDownLatch = new CountDownLatch(1);
            //arg1:服务器的ip和端口
            //arg2:客户端与服务器之间的会话超时时间，以毫秒为单位
            //arg3:监视器对象
            ZooKeeper zooKeeper = new ZooKeeper("192.168.10.6:2181", 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState()==Event.KeeperState.SyncConnected){
                        System.err.println("链接创建成功!");
                        countDownLatch.countDown();
                    }
                }
            });
            //主线程阻塞等待连接对象的创建成功
            countDownLatch.await();
            //打印会话编号
            System.err.println(zooKeeper.getSessionId());
            zooKeeper.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

```

### 6.2、新增节点

```java
//同步方式
create(String path,byte[] data,List<ACL> acl,CreateMode createMode)
//异步方式
create(String path,byte[] data,List<ACL> acl,CreateMode createmode,AsyncCallback.StringCallback callback,Object ctx)
```

- path - znode路径。例如，/node1/node1/node11
- data - 要存储在指定znode路径中的数据
- acl - 要创建节点的访问控制列表。zookeeperAPI提供一个静态接口ZooDefs.Ids来回去一些基本的acl列表。例如，ZooDefs.Ids.OPEN_ACL_UNSAFF返回打开znode的acl列表。
- createMode - 节点的类型，这是一个枚举。
- callBack - 异步回调接口
- ctx - 传递上下文参数

案例：

```java
package com.bai.zk;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.acl.Acl;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 10:53
 */
public class ZKCreate {

    private String IP_port = "172.16.139.113:2181";
    private ZooKeeper zooKeeper = null;

    @Before
    public void before()throws Exception{
        System.out.println("Before");
        //计数器对象
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //arg1:服务器的ip和端口
        //arg2:客户端与服务器之间的会话超时时间，以毫秒为单位
        //arg3:监视器对象
        zooKeeper = new ZooKeeper(IP_port, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState()==Event.KeeperState.SyncConnected){
                    System.err.println("链接创建成功!");
                    countDownLatch.countDown();
                }
            }
        });
        //主线程阻塞等待连接对象的创建成功
        countDownLatch.await();
        //打印会话编号
        System.err.println(zooKeeper.getSessionId());
    }


    @Test
    public void test()throws Exception{
        System.out.println("create1");
        //arg1:节点路径
        //arg2:节点数据
        //arg3:节点的权限 world:anyone:cdrwa
        //arg4:节点类型 持久化节点
        zooKeeper.create("/create/node1","node1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @Test
    public void test2() throws Exception{
        //ZooDefs.Ids.READ_ACL_UNSAFE : world:anyone:r
        zooKeeper.create("/create/node2","node2".getBytes(),ZooDefs.Ids.READ_ACL_UNSAFE,CreateMode.PERSISTENT);
    }

    @Test
    public void test3() throws Exception{
        //world授权模式
        //权限列表
        List<ACL> acls = new ArrayList<ACL>();
        //授权模式和授权对象
        Id id = new Id("world","anyone");
        //设置权限
        acls.add(new ACL(ZooDefs.Perms.READ,id));
        acls.add(new ACL(ZooDefs.Perms.WRITE,id));
        zooKeeper.create("/create/node3","node3".getBytes(),acls,CreateMode.PERSISTENT);
    }


    @Test
    public void test4()throws Exception{
        //ip授权模式
        // 权限列表
        List<ACL> acls = new ArrayList<>();
        //授权模式和授权对象
        Id id = new Id("ip","172.16.139.108");
        //权限设置
        acls.add(new ACL(ZooDefs.Perms.ALL,id));
        zooKeeper.create("/create/node4","node4".getBytes(),acls,CreateMode.PERSISTENT);
    }

    @Test
    public void test5()throws Exception{
        //auth授权模式
        //添加用户
        zooKeeper.addAuthInfo("digest","admin:123456".getBytes());
        zooKeeper.create("/create/node5","node5".getBytes(),ZooDefs.Ids.CREATOR_ALL_ACL,CreateMode.PERSISTENT);
    }

    @Test
    public void test6()throws Exception{
        //auth授权模式
        //添加用户
        zooKeeper.addAuthInfo("digest","admin:123456".getBytes());
        //权限列表
        List<ACL> acls = new ArrayList<>();
        //授权模式与授权对象
        Id id = new Id("auth","admin");
        acls.add(new ACL(ZooDefs.Perms.READ,id));
        zooKeeper.create("/create/node6","node6".getBytes(),acls,CreateMode.PERSISTENT);
    }

    @Test
    public void test7() throws Exception{
        //digest授权模式
        //授权列表
        List<ACL> acls = new ArrayList<>();
        //授权模式和授权对象
        Id id = new Id("digest","admin:0uek/hZ/V9fgiM35b0Z2226acMQ=");
        //设至权限
        acls.add(new ACL(ZooDefs.Perms.ALL,id));
        zooKeeper.create("/create/node7","node7".getBytes(),acls,CreateMode.PERSISTENT);
    }

    @Test
    public void test8()throws Exception{
        //持久化顺序节点
        //Ids.OPEN_ACL_UNSAFE world:anyone:cdrwa
        String result = zooKeeper.create("/create/node8","node8".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT_SEQUENTIAL);
        System.out.println(result);
    }

    @Test
    public void test9()throws Exception{
        //临时节点
        //Ids.OPEN_ACL_UNSAFE world:anyone:cdrwa
        String result = zooKeeper.create("/create/node9","node9".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        System.out.println(result);
    }

    @Test
    public void test10()throws Exception{
        //临时有序节点
        //Ids.OPEN_ACL_UNSAFE world:anyone:cdrwa
        String result = zooKeeper.create("/create/node10","node10".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(result);
    }

    @Test
    public void test11()throws Exception{
        //异步方式创建节点
        zooKeeper.create("/create/node11", "node11".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new AsyncCallback.StringCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, String name) {
                //0代表创建成功
                System.out.println(rc);
                //节点路径
                System.out.println(path);
                //节点路径
                System.out.println(name);
                //上下文参数
                System.out.println(ctx);
            }
        },"I am context");
        Thread.sleep(10000);
        System.out.println("结束");
    }

    @After
    public void after()throws Exception{
        System.out.println("After");
        zooKeeper.close();
    }

}

```

### 6.3、更新节点

```java
//同步方法
setData(String path,byte[] data,int version)
//异步方法
setData(String path,byte[] data,int version,AsyncCallback.StatCallback callBack,Object ctx)
```

- path - znode路径
- data - 要存储在指定znode路径中的数据
- version - znode的当前版本。每次数据更改时，Zookeeper会更新znode的版本号。
- callBck  - 异步回调接口
- ctx - 传递上下文参数

案例：

```java
package com.bai.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 15:07
 */
public class ZKSet {

    private String ip_port = "172.16.139.113:2181";
    private ZooKeeper zooKeeper = null;

    @Before
    public void before()throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(ip_port, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @Test
    public void set1()throws Exception{
        //arg1:节点的路径
        //arg2:修改的数据
        //arg3:数据版本号 -1代表版本号不参与更新
        Stat stat = zooKeeper.setData("/set/node1", "node13".getBytes(), 2);
        System.out.println(stat);
        System.out.println(stat.getVersion());

    }

    @Test
    public void set2()throws Exception{
        zooKeeper.setData("/set/node1", "node13".getBytes(), -1, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                //0代表修改成
                System.out.println(rc);
                //节点的路径
                System.out.println(path);
                //上下文对象
                System.out.println(ctx);
                //属性描述对象
                System.out.println(stat.getVersion());
            }
        },"I am Context");
    }

    @After
    public void after()throws Exception{
        zooKeeper.close();
    }


}

```

### 6.4、删除节点

```java
//同步方法
delete(String path,int version)
//异步方法
delete(String path,int version,AsyncCallback.voidCallback callBack,Object ctx)
```

- path - znode路径
- version - znode的当前版本
- callBack - 异步回调接口
- ctx - 传递的上下文

案例:

```java
package com.bai.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 15:33
 */
public class ZKDelete {


    private String IP_port = "172.16.139.113:2181";
    private ZooKeeper zooKeeper = null;

    @Before
    public void before()throws Exception{
        System.out.println("Before");
        //计数器对象
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //arg1:服务器的ip和端口
        //arg2:客户端与服务器之间的会话超时时间，以毫秒为单位
        //arg3:监视器对象
        zooKeeper = new ZooKeeper(IP_port, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState()==Event.KeeperState.SyncConnected){
                    System.err.println("链接创建成功!");
                    countDownLatch.countDown();
                }
            }
        });
        //主线程阻塞等待连接对象的创建成功
        countDownLatch.await();
        //打印会话编号
        System.err.println(zooKeeper.getSessionId());
    }

    @Test
    public void delete1() throws Exception{
        //arg1:节点路径
        //arg2:删除节点的版本号 -1表示不考虑版本号
        zooKeeper.delete("/delete/node1",-1);
    }

    @Test
    public void delete2() throws Exception{
        //异步方式
        zooKeeper.delete("/delete/node2", -1, new AsyncCallback.VoidCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx) {
                System.out.println(rc);
                System.out.println(path);
                System.out.println(ctx);
            }
        },"I am Context");
        Thread.sleep(10000);
        System.out.println("结束");
    }



    @After
    public void after()throws Exception{
        System.out.println("After");
        zooKeeper.close();
    }

}

```

### 6.5、查看节点

```java
//同步方法
getData(String path,boolean b,Stat stat)
//异步方法
getData(String path,boolean b,AsyncCallback.DataCallback callBack,Object ctx)
```

- path - znode路径
- b - 是否使用连接对象中注册的监视器。
- stat - 返回znode的元数据
- callBack - 异步回调接口
- ctx - 传递上下文参数

案例：

```java
package com.bai.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 15:47
 */
public class ZKGet {

    private String ip_port = "172.16.139.113:2181";
    private ZooKeeper zooKeeper = null;

    @Before
    public void before()throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(ip_port, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @Test
    public void get1()throws Exception{
        Stat stat = new Stat();
        //arg1:节点路径
        //arg3:读取节点属性的对象
        byte[] data = zooKeeper.getData("/get/node1", false, stat);
        //打印数据
        System.out.println(new String(data));
        //版本信息
        System.out.println(stat.getVersion());
    }

    @Test
    public void get2()throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //异步方式
        Stat stat = new Stat();
        zooKeeper.getData("/get/node1", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
                System.out.println(rc);
                System.out.println(path);
                System.out.println(ctx);
                System.out.println(new String(data));
                System.out.println(stat.getVersion());
                countDownLatch.countDown();
            }
        },"I am Context");
        countDownLatch.await();
        System.out.println("结束");
    }


    @After
    public void after()throws Exception{
        zooKeeper.close();
    }

}

```

### 6.6、查看子节点

```java
//同步方法
getChildren(String path,boolean b);
//异步方法
getChildren(String path,bookean b,AsyncCallback.ChildrenCallback callBack,Object ctx)
```

- path - znode路径
- b - 是否使用连接对象中注册的监视器。
- callBack - 异步回调接口
- ctx - 传递上下文参数

案例:

```java
package com.bai.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 16:05
 */
public class ZKGetChildren {

        private String ip_port = "172.16.139.113:2181";
        private ZooKeeper zooKeeper = null;

        @Before
        public void before()throws Exception{
            CountDownLatch countDownLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(ip_port, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected){
                        System.out.println("连接成功");
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        }


        @Test
        public void getC1()throws Exception {
            List<String> children = zooKeeper.getChildren("/get", false);
            children.forEach(x->{
                System.out.println(x);
            });
        }

        @Test
        public void getC2()throws Exception {
            CountDownLatch downLatch = new CountDownLatch(1);
            zooKeeper.getChildren("/get", false, new AsyncCallback.ChildrenCallback() {
                @Override
                public void processResult(int rc, String path, Object ctx, List<String> children) {
                    System.out.println(rc);
                    System.out.println(path);
                    System.out.println(ctx);
                    children.forEach(x->{
                        System.out.println(x);
                    });
                    downLatch.countDown();
                }
            },"I am Context");
            downLatch.await();
            System.out.println("结束");
        }

        @After
        public void after()throws Exception{
            zooKeeper.close();
        }



}

```

### 6.7、检查节点是否存在

```java
//同步方法
exists(String path,boolean b);
//异步方法
exists(String path,boolean b,AsyncCallback.StatCallback callBck,Object ctx);
```

- path - znode路径
- b - 是否使用连接对象中注册的监视器。
- callBack - 异步回调接口
- ctx - 传递上下文参数

案例：

```java
package com.bai.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * @Author:liuBai
 * @Time : 2021/4/9 16:15
 */
public class ZKExists {

    private String ip_port = "172.16.139.113:2181";
    private ZooKeeper zooKeeper = null;

    @Before
    public void before()throws Exception{
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(ip_port, 5000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
    }

    @Test
    public void exists1()throws Exception{
        Stat exists = zooKeeper.exists("/exists1", false);
        System.out.println(exists.getVersion());
        System.out.println(exists);
    }

    @Test
    public void exists2()throws Exception{
        CountDownLatch downLatch = new CountDownLatch(1);
        zooKeeper.exists("/exists1", false, new AsyncCallback.StatCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, Stat stat) {
                System.out.println(rc);
                System.out.println(path);
                System.out.println(ctx);
                System.out.println(stat.getVersion());
                downLatch.countDown();
            }
        },"I am Context");
        downLatch.await();
        System.out.println("结束");
    }

    @After
    public void after()throws Exception{
        zooKeeper.close();
    }

}

```



>zookeeper 事件监听机制
>
>zookeeper 集群搭建
>
>一致性协议:zab协议 
>
>zookeeper的leader选举
>
>observer角色及其配置
>
>zookeeperAPI连接集群

## 7、zookeeper 事件监听机制

### 7.1、watcher概念

​		zookeeper提供了数据的发布/订阅功能，多个订阅者可同时监听某一特定主题对 象，当该主题对象的自身状态发生变化时(例如节点内容改变、节点下的子节点列表改变 等)，会实时、主动通知所有订阅者 

​		zookeeper采用了Watcher机制实现数据的发布/订阅功能。该机制在被订阅对 象发生变化时会异步通知客户端，因此客户端不必在Watcher注册后轮询阻塞，从而减轻 了客户端压力。

​		 watcher机制实际上与观察者模式类似，也可看作是一种观察者模式在分布式场 景下的实现方式。

### 7.2 watcher架构

Watcher实现由三个部分组成：

- Zookeeper服务端 
- Zookeeper客户端 
- 客户端的ZKWatchManager对象 

​		客户端首先将Watcher注册到服务端，同时将Watcher对象保存到客户端的Watch管 理器中。当ZooKeeper服务端监听的数据状态发生变化时，服务端会主动通知客户端， 接着客户端的Watch管理器会触发相关Watcher来回调相应处理逻辑，从而完成整体的数 据发布/订阅流程

![](C:\Users\liuba\Desktop\study_source\zookeeper\image\微信图片_20210409163301.png)

### 7.3、watcher特性

| 特性           | 说明                                                         |
| -------------- | ------------------------------------------------------------ |
| 一次性         | watcher是一次性的，一旦触发就会移除，再次使用时需要重新注册  |
| 客户端顺序回调 | watcher回调是顺序串行化执行的，只有回调后客户端才能看到最新的数据状态。一个watcher回调逻辑不应该太多，以免影响别的watcher执行 |
| 轻量级         | WatchEvent是最小的通信单元，结构上只包含通知状态、事件类型和节点路径，并不会告诉数据节点变化前后的具体内容； |
| 时效性         | watcher只有在当前session彻底失效时才会无效，若在session有效期内快速重连成功，则watcher依然存在，乃可接收到通知； |

### 7.4、watcher接口设计

​	Watcher是一个接口，任何实现了Watcher接口的类就是一个新的Watcher。Watcher内部包含两个枚举类：KeeperState、EventType

![](C:\Users\liuba\Desktop\study_source\zookeeper\image\微信图片_20210816100451.png)

- Watcher通知状态(KeeperState)

  KeeperState是客户端与服务端连接状态发生变化时对应的通知类型。路径org.apache.zookeeper.Watcher.Event.KeeperState，是一个枚举类，其他枚举属性如下：

  | 枚举属性      | 说明                     |
  | ------------- | ------------------------ |
  | SyncConnected | 客户端与服务端正常连接时 |
  | Disconnected  | 客户端与服务端断开连接时 |
  | Expired       | 会话session失效时        |
  | AuthFailed    | 身份认证失败时           |

- Watcher事件类型(EventType)

  EventType时数据系节点(znode)发生变化时对应的通知类型。EventType变化是KeeperState永远处于SyncConnected通知状态下；当KeeperState发生变化时，EventType永远为None。其路径为org.apache.zookeeper.Watcher.Event.EventType，是一个枚举类，枚举属性如下：

  | 枚举属性            | 说明                                                      |
  | ------------------- | --------------------------------------------------------- |
  | None                | 无                                                        |
  | NodeCreated         | Watcher监听的数据节点被创建时                             |
  | NodeDeleted         | Watcher监听的数据节点被删除时                             |
  | NodeDataChanaged    | Watcher监听的数据节点内容发生变更时(无论内容数据是否变化) |
  | NodeChildrenChanged | Watcher监听的数据节点的子节点列表发生变更时               |

  注：客户端接收到相关事件通知中包含状态及类型等信息，不包括节点变化前后的具体内容，变化前的数据需要业务自身存储，变化后的数据需要调用get等方法重新获取；

### 7.5、捕获相应的事件

​	上面讲到zookeeper客户端连接状态和zookeeper对znode节点监听的事件类型，下面我们来讲解如何建立zookeeper的watcher监听。在zookeeper中采用zk.getChildren(path,watch)、zk.exists(path,watch)、zk.getData(path,watcher,stat)这样的方式为某个znode注册监听。

​	下表以node-x节点为例，说明调用的注册方法和可监听事件间的关系：

​	

| 注册方式                          | created | childrenChanged | Changed | Deleted |
| --------------------------------- | ------- | --------------- | ------- | ------- |
| zk.exists("/node-x",watcher)      | 可监控  |                 | 可监控  | 可监控  |
| zk.getData("/node-x",watcher)     |         |                 | 可监控  | 可监控  |
| zk.getChildren("/node-x",watcher) |         | 可监控          |         | 可监控  |

### 7.6、注册watcher的方法

#### 7.6.1 客户端与服务器的连接状态

```java
KeeperState 通知状态
SyncConnected:客户端与服务器正常连接
DisConnected:客户端与服务器断开连接
Expired:绘画session失效时
AuthFailed:身份认证失败
    
事件类型：None
```

案例：

```java
package com.bai.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author:liuBai
 * @Time : 2021/8/16 10:34
 */
public class ZkConnectionWatcher implements Watcher {

    //计数器对象
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    //连接对象
    static ZooKeeper zookeeper;

    @Override
    public void process(WatchedEvent event) {
        try {
            //事件类型
            if (event.getType()==Event.EventType.None){
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接创建成功！");
                    countDownLatch.countDown();
                }else if (event.getState() == Event.KeeperState.Disconnected){
                    System.out.println("断开连接！");
                }else if (event.getState() == Event.KeeperState.Expired){
                    System.out.println("会话超时！");
                    zookeeper = new ZooKeeper("172.20.10.11:2181",5000,new ZkConnectionWatcher());
                }else if (event.getState() == Event.KeeperState.AuthFailed){
                    System.out.println("认证失败!");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            zookeeper = new ZooKeeper("172.20.10.11:2181",5000,new ZkConnectionWatcher());
            //阻塞线程
            countDownLatch.await();
            System.out.println(zookeeper.getSessionId());
            //添加授权用户
            zookeeper.addAuthInfo("digest1","admin:admin1".getBytes());
            byte[] data = zookeeper.getData("/node1", false, null);
            System.out.println(new String(data));
            TimeUnit.SECONDS.sleep(1);
            zookeeper.close();
            System.out.println("结束!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}

```

7.6.2、检查节点是否存在

```java
//使用连接对象监视器
exists(String path,boolean b)
//自定义监视器
exists(String path,watcher w)
    
//NodeCreated:节点创建
//NodeDeleted:节点删除
//NodeDataChanged:节点内容发生变化
```

- path - znode路径
- b - 是否使用连接对象中注册的监视器
- w - 监视器对象

案例：

```java
package com.bai.watcher;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author:liuBai
 * @Time : 2021/8/16 11:14
 */
public class ZkWatcherExists {

    String ip  = "172.20.10.11:2181";
    ZooKeeper zooKeeper  = null;

    @Before
    public void before() throws IOException,InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        //连接zookeeper客户端
        zooKeeper = new ZooKeeper(ip, 6000, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("连接对象的参数!");
                //连接成功
                if (event.getState() == Event.KeeperState.SyncConnected){
                    countDownLatch.countDown();
                }
                System.out.println("path="+event.getPath());
                System.out.println("eventType"+event.getType());
            }
        });
        countDownLatch.await();
    }

    @After
    public void after() throws InterruptedException {
        zooKeeper.close();
    }

    @Test
    public void watcherExists1() throws KeeperException, InterruptedException {
        //arg1:节点路径
        //arg2:使用连接对象中的watcher
        zooKeeper.exists("/watcher1",true);
        TimeUnit.SECONDS.sleep(50);
        System.out.println("结束");
    }

    @Test
    public void watcherExists2() throws KeeperException, InterruptedException {
        //arg1:节点路径
        //arg2:自定义watcher对象
        zooKeeper.exists("/watcher1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("自定义watcher");
                System.out.println("path="+event.getPath());
                System.out.println("eventType"+event.getType());
            }
        });
        TimeUnit.SECONDS.sleep(50);
        System.out.println("结束");
    }

    @Test
    public void watcherExists3() throws KeeperException, InterruptedException {
        //watcher一次性
        Watcher watcher= new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("自定义watcher");
                System.out.println("path="+event.getPath());
                System.out.println("eventType"+event.getType());
                try {
                    zooKeeper.exists("/watcher1",this);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        zooKeeper.exists("/watcher1",watcher);
        TimeUnit.SECONDS.sleep(80);
        System.out.println("结束");
    }

    @Test
    public void watcherExists4() throws KeeperException, InterruptedException {
        //注册多个监听器
        zooKeeper.exists("/watcher1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("自定义watcher===>1");
                System.out.println("path="+event.getPath());
                System.out.println("eventType"+event.getType());
            }
        });

        zooKeeper.exists("/watcher1", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                System.out.println("自定义watcher===>2");
                System.out.println("path="+event.getPath());
                System.out.println("eventType"+event.getType());
            }
        });

        TimeUnit.SECONDS.sleep(80);
        System.out.println("结束");
    }

}

```

### 7.7、配置中心案例

​	工作中有这样的一个场景：数据库用户名和密码信息放在一个配置文件中，应用读取该配置文件，配置文件信息放入缓存。

​	若数据库的用户名和密码改变的时候，还需要重新加载缓存，比较麻烦，通过ZooKeeper可以轻松完成，当数据库发生变化时自动完成缓存同步。

设计思路：

1. ​	连接zookeeper服务器
2. 读取zookeeper中的配置信息，注册watcher监听器，存入本地变量
3. 当zookeeper中的配置信息发生变化时，通过watcher的回调方法捕获数据变化事件
4. 重新获取配置信息

案例：

```java
package com.bai.example;

import com.bai.watcher.ZkConnectionWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author:liuBai
 * @Time : 2021/8/16 14:34
 */
public class MyConfigCenter implements Watcher {

    //zk的连接
    String IP = "172.20.10.11:2181";
    //计数器
    CountDownLatch countDownLatch = new CountDownLatch(1);
    //连接对象
    static ZooKeeper zooKeeper;

    //用于本地化存储信息
    private String url;
    private String username;
    private String password;

    //构造方法
    public MyConfigCenter(){
        initValue();
    }


    @Override
    public void process(WatchedEvent event) {
        try {
            //捕获事件状态
            if (event.getType() == Event.EventType.None){
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }else if (event.getState() == Event.KeeperState.Disconnected){
                    System.out.println("断开连接!");
                }else if (event.getState() == Event.KeeperState.Expired){
                    System.out.println("会话超时");
                    zooKeeper = new ZooKeeper(IP,6000,this);
                }else if (event.getState() == Event.KeeperState.AuthFailed){
                    System.out.println("验证失败");
                }
            }else if (event.getType() == Event.EventType.NodeDataChanged){
                //当配置信息发生变化时
                initValue();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //连接zookeeper服务器，读取配置信息
    public void initValue(){
        try{
            //创建连接对象
            zooKeeper = new ZooKeeper(IP,5000,this);
            //阻塞线程
            countDownLatch.await();
            //读取配置信息
            this.url = new String(zooKeeper.getData("/config/url",true,null));
            this.username = new String(zooKeeper.getData("/config/username",true,null));
            this.password = new String(zooKeeper.getData("/config/password",true,null));

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try{
            MyConfigCenter configCenter = new MyConfigCenter();
            for (int i=1 ; i<=10 ;i++){
                TimeUnit.SECONDS.sleep(3);
                System.out.println("url:"+configCenter.url);
                System.out.println("username:"+configCenter.username);
                System.out.println("password:"+configCenter.password);
                System.out.println("=============================================");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

```

### 7.8、生成分布式唯一ID

​	在过去的单裤单表系统中，通常可以使用数据库自带的auto_increment属性来自动为每一奥记录生成一个唯一的ID。但是分库分表后，就无法依靠数据库的auto_increment属性来唯一标识一条记录了。此时我们就可以用zookeeper在分布式环境中生成全局唯一ID。

设计思路：

1. 链接zookeeper服务器
2. 指定路径生成临时有序节点
3. 取序列号及为分布式环境下的唯一ID

案例：

```java
package com.bai.watcher;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GloballyUniqueId implements Watcher {

    //zk的连接串
    String IP = "192.168.124.10:2181";
    //计数器对象
    CountDownLatch countDownLatch = new CountDownLatch(1);
    //用户生成序列号的节点
    String defaultPath = "/uniqueId";
    //连接对象
    ZooKeeper zooKeeper ;


    @Override
    public void process(WatchedEvent event) {
        try {
            //捕获事件状态
            if (event.getType() == Event.EventType.None){
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功！");
                    countDownLatch.countDown();
                }else if (event.getState() == Event.KeeperState.Disconnected){
                    System.out.println("连接断开！");
                }else if (event.getState() == Event.KeeperState.Expired){
                    System.out.println("连接超时！");
                    zooKeeper = new ZooKeeper(IP,6000,this);
                }else if (event.getState() == Event.KeeperState.AuthFailed){
                    System.out.println("认证失败！");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //构造方法
    public GloballyUniqueId(){
        try {
            //打开连接
            zooKeeper = new ZooKeeper(IP,5000,this);
            //阻塞线程，等待链接的创建成功!
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    //生成id的方法
    public String getUniqueId(){
        String path = "";
        try {
            //创建临时有序节点
            path=zooKeeper.create(defaultPath,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path.substring(9);
    }

    public static void main(String[] args) throws InterruptedException {
        GloballyUniqueId globallyUniqueId = new GloballyUniqueId();
        for (int i = 1 ; i<=5 ; i++){
            String uniqueId = globallyUniqueId.getUniqueId();
            System.out.println(uniqueId);
        }
        globallyUniqueId.close();
        TimeUnit.SECONDS.sleep(10);
    }
}

```

### 7.9、分布式锁

​	分布式锁有多种实现方式，比如通过数据库，redis都可以实现，作为分布式协同工具zookeeper，当然也有这标准的实现方式。下面介绍zookeeper中如何实现排他锁。

设计思路：

1. 每个客户端往/Locks下创建临时有序节点/Locks/Lock_,创建成功后/Lock下面会有每个客户端对应的节点，如：/Locks/Lock_000000001
2. 客户端取得/Locks下子节点，并进行排序，判断排在最前面的是否为自己，如果自己的所节点在第一位，代表获取所成功
3. 如果自己的锁节点不在第一位，则监听自己前一位的锁节点。例如：自己锁节点Lock_000002,那么则监听Lock_0000001
4. 当前以为锁节点(Lock_00000001)对应的客户端执行完成，释放锁，将会触发监听客户端(Lock_00000002)的逻辑
5. 监听客户端重新执行第2步逻辑，判断自己是否得到了锁。

案例：

```java
package com.bai.watcher;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyLock{

    //zk的连接串
    String IP = "192.168.124.10:2181";
    //计数器对象
    CountDownLatch countDownLatch = new CountDownLatch(1);
    //连接对象
    ZooKeeper zooKeeper ;
    private static final String LOCK_ROOT_PATH="/Locks";
    private static final String LOCK_NODE_NAME="Lock_";
    private String lockPath;

    //创建连接对象
    public MyLock(){
        try {
            zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        //捕获事件状态
                        if (event.getType() == Watcher.Event.EventType.None){
                            if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                                System.out.println("连接成功！");
                                countDownLatch.countDown();
                            }else if (event.getState() == Watcher.Event.KeeperState.Disconnected){
                                System.out.println("连接断开！");
                            }else if (event.getState() == Watcher.Event.KeeperState.Expired){
                                System.out.println("连接超时！");
                                zooKeeper = new ZooKeeper(IP,6000,this);
                            }else if (event.getState() == Watcher.Event.KeeperState.AuthFailed){
                                System.out.println("认证失败！");
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取锁
    public void acquiredLock() throws Exception {
        //创建所节点
        createLock();
        //尝试获取锁
        attemptLock();
    }

    //创建所节点
    private void createLock() {
        try {
            //判断Locks是否存在，不存在创建
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH, false);
            if (null == stat){
                zooKeeper.create(LOCK_ROOT_PATH,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            //创建临时有序节点
            lockPath = zooKeeper.create(LOCK_ROOT_PATH+"/"+LOCK_NODE_NAME,new byte[0],ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("节点创建成功:"+lockPath);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //监视器对象，监视上一个节点是否被删除
    Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getType() == Event.EventType.NodeDeleted){
                synchronized (this){
                    watcher.notify();
                }
            }
        }
    };

    //尝试获取锁
    private void attemptLock() throws Exception{
        //获取Locks节点下的所有子节点
        List<String> children = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
        //对子节点进行排序
        Collections.sort(children);
        // /Locks/Lock_0000000000001
        int index = children.indexOf(lockPath.substring(LOCK_ROOT_PATH.length()+1));
        if (index == 0){
            System.out.println("获取锁成功！！！！");
            return;
        }else{
            //获取上一个节点的路径
            String path = children.get(index-1);
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH + "/" + path, watcher);
            if (stat == null){
                acquiredLock();
            }else{
                synchronized (watcher){
                    watcher.wait();
                }
                attemptLock();
            }
        }
    }

    //释放锁
    public void releaseLock() throws KeeperException, InterruptedException {
        zooKeeper.delete(this.lockPath,-1);
        zooKeeper.close();
        System.out.println("锁以释放："+this.lockPath);
    }

    public static void main(String[] args) {
        MyLock lock = new MyLock();
        lock.createLock();
    }

}



package com.bai.example;

import com.bai.watcher.MyLock;

public class TicketSeller {

    private static int count = 10;


    private void sell(){
        if (count>0){
            System.out.println("售票开始");
            //线程堆积休眠数毫秒，模拟现实中的费是操作
            int sleepMillis = 100;
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("售票结束！"+Thread.currentThread().getName()+"获取到第票："+count);
            count--;
        }else{
            System.out.println(Thread.currentThread().getName()+"票已售完");
            Thread.currentThread().interrupted();
        }
    }

    public void sellTicketWithLock() throws Exception {
        MyLock lock = new MyLock();
        //获取锁
        lock.acquiredLock();
        sell();
        //释放锁
        lock.releaseLock();
    }


    public static void main(String[] args) {
        TicketSeller seller = new TicketSeller();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                System.out.println(Thread.currentThread().getName()+":"+i);
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"E").start();
    }

}

```




## 8、zookeeper集群搭建

​	单机环境下，jdk、zookeeper安装完毕，集于一台虚拟机，进行zookeeper伪集群搭建，zookeeper集群中包含3个节点，节点对外提供服务端口号分别为2181，2182，2183

1. 基于zookeeper-3.4.10复制3份zookeeper安装好的服务器文件，目录名称分别为zookeeper2181、zookeeper2182、zookeeper2183

   ```shell
   cp -r zookeeper-3.4.10 zookeeper2181
   cp -r zookeeper-3.4.10 zookeeper2182
   cp -r zookeeper-3.4.10 zookeeper2183
   ```

2. 修改zookeeper2181服务器对应配置文件。

   ```shell
   #服务器对应端口号
   clientPort=2181
   #数据快照文件所在路径
   dataDir=/home/zookeeper/zookeeper2181/data
   #集群配置信息
   	#server.A=B:C:D
   	#A:是一个数字，表示这个服务器的编号
   	#B:是这个服务器的ip地址
   	#C:Zookeeper服务器之间的通信端口
   	#D:Leader选举的端口
   server.1=192.168.60.130:2287:3387
   server.2=192.168.60.130:2288:3388
   server.3=192.168.60.130:2289:3389
   ```

3. 在上一步dataDir指定的目录下，创建myid文件，然后在该文件中添加上一步server配置的对应A数字。

   ```shell
   #zookeeper2181对应的数字为1
   #/home/zookeeper/zookeeper2181/data目录下
   echo "1" > myid
   ```

4. zookeeper2181、zookeeper2183参照步骤2/3进行相应配置

5. 分别启动三台服务器，检测集群状态

   登录命令：

   ```shell
   ./zkCli.sh -server 192.168.60.130:2181
   ./zkCli.sh -server 192.168.60.130:2182
   ./zkCli.sh -server 192.168.60.130:2183
   ```



## 9、一致性协议：zab协议

​	zab协议全程是Zookeeper Atomic Broadcast(zookeeper原子广播)。zookeeper是通过zab协议来保证分布式事务的最终一致性

​	基于zab协议，zookeeper集群中的角色主要有以下三类，如下表所示：

​	![](D:\project_java\my_project\demo\zookeeper_demo\zookeeper\image\qq202108171055.png)

​	zab广播模式工作原理，通过类似两阶段提交协议的方式解决数据一致性：

​		![](D:\project_java\my_project\demo\zookeeper_demo\zookeeper\image\qq202108171058.png)

1. ​	leader从客户端收到一个请求
2. leader生成一个新的事物并为这个事务生成一个唯一的ZXID
3. leader将这个事务提议(propose)发送给所有的follows节点
4. follower节点将收到的事务请求加入到历史队列(history queue)中，并发送ack给leader
5. 当leader收到大多数follower（半数以上节点）的ack消息，leader会发送commit请求
6. 当follwer收到commit请求时，从历史队列中将事务请求commit；



## 10、zookeeper的leader选举

### 10.1、服务器状态

​	looking:寻找leader状态。当服务器处于该状态时，他会认为当前集群中没有leader，因此需要进入leader选举状态

​	leading:领导者状态。表明当前服务器角色是leader

​	following:更随着状态，表明当前服务器角色是follower

​	observing:观察者状态。表明当前服务器角色是observer

### 10.2、服务器启动时的leader选举

​	在集群初始化阶段，当有一天服务器server1启动时，其他单独无法进行和完成leader选举，当第二台服务器server2启动时，此时两台服务器可以互相通信，每台机器都试图找到leader，于是进行leader选举过程。选举过程如下：

1. 每个server发出一个投票，由于是初始情况，server1和server2都会将自己作为leader服务器进行投票，每次投票包含所推举的服务器的myid和zxid，使用（myid，zxid）来表示，此时server1的投票为（1，0），server2的投票为（2，0），然后各自将这个投票发给集群中其他机器。

2. 集群中的每台服务器接受来自集群中各个服务器的投票

3. 处理投票。针对每一个投票服务器都需要将别人的投票和自己的投票进行pk，pk规则如下

   - 优先检查zxid，zxid比较大的服务器有限作为leader。

   - 如果zxid相同，那么就比较myid，myid比较大的服务器作为leader

     对于Server1而言，他的投票是（1，0）接收Server2的投票为（2，0），首先比较两者的zxid，均为0，在比较myid，此时的server2的myid比最大，于是更新自己的投票为（2，0），然后重新投票，对于server2而言，其无需更新自己的投票，只是再次向集群中所有机器发出上一次投票信息即可。

4. 统计票数。每次投票后，服务器都会统计投票信息，判断是否已经有过半机器的投票信息，对于server1、server2而言，都统计处集群中已经有两台机器接受了（2，0）的投票信息，此时便认为已经选举出leader

5. 改变服务器状态，一旦确定leader，每个服务器都会更新自己的状态，如果是follower，那么就变更为following，如果是leader，就变更为leading。

### 10.2、服务器运行时期的leader选举

​	在zookeeper运行期间，leader与非leader各司其职，即便当有非leader服务器宕机或加入，此时也不会影响leader，但是一旦leader服务器挂掉，那么整个集群将暂停服务，进入新一轮的leader选举，其过程和启动时期的leader选举过程基本一致。

​	加入正在运行的有server1、server2、server3三台服务器，当前leader是server2，如某一时刻leader挂掉了，此时便开始leader选举，选举过程如下：

1. 变更状态。leader挂后，余下的服务器都会将自己的服务器状态变更为looking，然后开始进入leader选举过程
2. 每个server都会发出一个投票，在运行期间，每个服务器上的zxid可能不同，如此假设server1的zxid为122，server3的zxid为122，在第一轮投票中，server1和server3都会投自己，产生（1，122），（3，122），然后各自将投票发给集群中所有机器。
3. 接受来自各个服务器的投票，与启动时过程相同
4. 处理投票。与启动时过程相同，此时server3将会成为leader
5. 统计投票，与启动过程相同
6. 改变服务器状态，与启动时过程相同

## 11、observer角色机器配置

​	observer角色特点：

1. 不参与集群的leader选举

2. 不参与集群中写数据的ack反馈

   为了使用observer角色，在任何想变成observer角色的配置文件中加入如下配置

   ```shell
   peerType=observer
   ```

   并在所有server的配置文件中，配置observer模式的server的那行配置追加observer，例如：

   ```shell
   server.3=192.168.124.10:2289:3389:observer
   ```

## 12、zookeeper Api连接集群 

```java
ZooKeeper(String connectionString,int sessionTimeout,Watcher watcher)
```

- connectionString - zookeeper集合主机。
- sessionTimeout - 会话超时（一毫秒为单位）
- watcher - 实现“监控器”界面的对象。zookeeper集合通过监视器对象返回链接状态。

案例：



## 13、zookeeper开源客户端curator介绍

- zookeeper开源客户端curator介绍
- zookeeper四字监控命令
- zookeeper图形化的客户端工具（zooinspector）
- taokeeper监控工具的使用

### 13.1、zookeeper开源客户端curator介绍

#### 13.1.1. curator简介

​	curator是Netflix公司开源的一个zookeeper客户端，后捐献给apache，curator框架在zookeeper原生api接口上进行了包装，解决了很多zookeeper客户端底层的细节开发。提供zookeeper各种应用场景（比如：分布式锁，集群领导选举，共享计数器，缓存机制，分布式队列等）的抽象封装，实现了Fluent风格的API接口，是最好用，最流行的zookeeper的客户端。

​	原生zookeeperAPI的不足

- 连接对象异步创建，需要开发人员自行编码等待

- 链接没有自动重连超时机制

- watcher一次注册生肖一次

- 不支持递归创建树节点

  curator特点：

- 解决session绘画超市重连

- watcher反复注册

- 简化开发api

- 遵循fluent风格api

- 提供了分布式锁服务、共享计数器、缓存机制等机制

**maven依赖**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.bai</groupId>
    <artifactId>zookeeper-curator</artifactId>
    <version>1.0-SNAPSHOT</version>


    <dependencies>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.7</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>2.6.0</version>
            <type>jar</type>
            <exclusions>
                <exclusion>
                    <groupId>org.apache.zookeeper</groupId>
                    <artifactId>zookeeper</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

        <dependency>
            <groupId>org.apache.zookeeper</groupId>
            <artifactId>zookeeper</artifactId>
            <version>3.4.10</version>
        </dependency>

        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>2.6.0</version>
            <type>jar</type>
        </dependency>

    </dependencies>

</project>
```

#### 13.1.2. 连接到zookeeper

