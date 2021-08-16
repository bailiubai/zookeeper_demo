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
        TimeUnit.SECONDS.sleep(50);
    }
}
