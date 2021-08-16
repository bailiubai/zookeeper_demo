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
