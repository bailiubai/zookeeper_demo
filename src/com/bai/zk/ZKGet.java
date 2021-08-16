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

    //private String ip_port = "172.16.139.113:2181";
    private String ip_port = "172.20.10.11:2181";
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
