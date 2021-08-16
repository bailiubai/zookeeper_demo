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
