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
