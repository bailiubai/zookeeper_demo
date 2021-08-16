package com.bai.example;

import com.bai.watcher.ZkConnectionWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author:liuBai
 * @Time : 2021/8/16 14:34
 */
public class MyConfigCenter implements Watcher {

    //zk的连接
    String IP = "172.20.10.11:2181";
    //计数器
    CountDownLatch countDownLatch = new CountDownLatch(1);
    //连接对象
    static ZooKeeper zooKeeper;

    //用于本地化存储信息
    private String url;
    private String username;
    private String password;

    //构造方法
    public MyConfigCenter(){
        initValue();
    }


    @Override
    public void process(WatchedEvent event) {
        try {
            //捕获事件状态
            if (event.getType() == Event.EventType.None){
                if (event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("连接成功");
                    countDownLatch.countDown();
                }else if (event.getState() == Event.KeeperState.Disconnected){
                    System.out.println("断开连接!");
                }else if (event.getState() == Event.KeeperState.Expired){
                    System.out.println("会话超时");
                    zooKeeper = new ZooKeeper(IP,6000,this);
                }else if (event.getState() == Event.KeeperState.AuthFailed){
                    System.out.println("验证失败");
                }
            }else if (event.getType() == Event.EventType.NodeDataChanged){
                //当配置信息发生变化时
                initValue();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //连接zookeeper服务器，读取配置信息
    public void initValue(){
        try{
            //创建连接对象
            zooKeeper = new ZooKeeper(IP,5000,this);
            //阻塞线程
            countDownLatch.await();
            //读取配置信息
            this.url = new String(zooKeeper.getData("/config/url",true,null));
            this.username = new String(zooKeeper.getData("/config/username",true,null));
            this.password = new String(zooKeeper.getData("/config/password",true,null));

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        try{
            MyConfigCenter configCenter = new MyConfigCenter();
            for (int i=1 ; i<=10 ;i++){
                TimeUnit.SECONDS.sleep(3);
                System.out.println("url:"+configCenter.url);
                System.out.println("username:"+configCenter.username);
                System.out.println("password:"+configCenter.password);
                System.out.println("=============================================");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
