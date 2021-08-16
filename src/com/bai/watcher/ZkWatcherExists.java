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
