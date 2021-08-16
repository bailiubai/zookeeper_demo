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
