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
