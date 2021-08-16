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
