package com.bai.watcher;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MyLock{

    //zk的连接串
    String IP = "192.168.124.10:2181";
    //计数器对象
    CountDownLatch countDownLatch = new CountDownLatch(1);
    //连接对象
    ZooKeeper zooKeeper ;
    private static final String LOCK_ROOT_PATH="/Locks";
    private static final String LOCK_NODE_NAME="Lock_";
    private String lockPath;

    //创建连接对象
    public MyLock(){
        try {
            zooKeeper = new ZooKeeper(IP, 5000, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        //捕获事件状态
                        if (event.getType() == Watcher.Event.EventType.None){
                            if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                                System.out.println("连接成功！");
                                countDownLatch.countDown();
                            }else if (event.getState() == Watcher.Event.KeeperState.Disconnected){
                                System.out.println("连接断开！");
                            }else if (event.getState() == Watcher.Event.KeeperState.Expired){
                                System.out.println("连接超时！");
                                zooKeeper = new ZooKeeper(IP,6000,this);
                            }else if (event.getState() == Watcher.Event.KeeperState.AuthFailed){
                                System.out.println("认证失败！");
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取锁
    public void acquiredLock() throws Exception {
        //创建所节点
        createLock();
        //尝试获取锁
        attemptLock();
    }

    //创建所节点
    private void createLock() {
        try {
            //判断Locks是否存在，不存在创建
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH, false);
            if (null == stat){
                zooKeeper.create(LOCK_ROOT_PATH,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            }
            //创建临时有序节点
            lockPath = zooKeeper.create(LOCK_ROOT_PATH+"/"+LOCK_NODE_NAME,new byte[0],ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("节点创建成功:"+lockPath);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //监视器对象，监视上一个节点是否被删除
    Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent.getType() == Event.EventType.NodeDeleted){
                synchronized (this){
                    watcher.notify();
                }
            }
        }
    };

    //尝试获取锁
    private void attemptLock() throws Exception{
        //获取Locks节点下的所有子节点
        List<String> children = zooKeeper.getChildren(LOCK_ROOT_PATH, false);
        //对子节点进行排序
        Collections.sort(children);
        // /Locks/Lock_0000000000001
        int index = children.indexOf(lockPath.substring(LOCK_ROOT_PATH.length()+1));
        if (index == 0){
            System.out.println("获取锁成功！！！！");
            return;
        }else{
            //获取上一个节点的路径
            String path = children.get(index-1);
            Stat stat = zooKeeper.exists(LOCK_ROOT_PATH + "/" + path, watcher);
            if (stat == null){
                acquiredLock();
            }else{
                synchronized (watcher){
                    watcher.wait();
                }
                attemptLock();
            }
        }
    }

    //释放锁
    public void releaseLock() throws KeeperException, InterruptedException {
        zooKeeper.delete(this.lockPath,-1);
        zooKeeper.close();
        System.out.println("锁以释放："+this.lockPath);
    }

    public static void main(String[] args) {
        MyLock lock = new MyLock();
        lock.createLock();
    }

}
