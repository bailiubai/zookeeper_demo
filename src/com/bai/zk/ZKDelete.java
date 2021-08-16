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
