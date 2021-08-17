package com.bai.example;

import com.bai.watcher.MyLock;

public class TicketSeller {

    private static int count = 10;


    private void sell(){
        if (count>0){
            System.out.println("售票开始");
            //线程堆积休眠数毫秒，模拟现实中的费是操作
            int sleepMillis = 100;
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("售票结束！"+Thread.currentThread().getName()+"获取到第票："+count);
            count--;
        }else{
            System.out.println(Thread.currentThread().getName()+"票已售完");
            Thread.currentThread().interrupted();
        }
    }

    public void sellTicketWithLock() throws Exception {
        MyLock lock = new MyLock();
        //获取锁
        lock.acquiredLock();
        sell();
        //释放锁
        lock.releaseLock();
    }


    public static void main(String[] args) {
        TicketSeller seller = new TicketSeller();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                System.out.println(Thread.currentThread().getName()+":"+i);
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
        new Thread(()->{
            for (int i = 1 ; i<=10 ; i++){
                if (Thread.currentThread().isInterrupted()){
                    break;
                }
                try {
                    seller.sellTicketWithLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"E").start();
    }

}
