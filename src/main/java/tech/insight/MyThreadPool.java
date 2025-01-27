package tech.insight;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class MyThreadPool {

    private final int corePoolSize;
    private final int maxSize;
    private final int timeout;
    private final TimeUnit timeUnit;
    public final BlockingQueue<Runnable> blockingQueue;
    private final RejectHandle rejectHandle;

    public MyThreadPool(int corePoolSize, int maxSize, int timeout, TimeUnit timeUnit,
                        BlockingQueue<Runnable> blockingQueue, RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }


    List<Thread> coreList = new ArrayList<>();

    List<Thread> supportList = new ArrayList<>();


    void execute(Runnable command) {
        if (coreList.size() < corePoolSize) {
            Thread thread = new CoreThread();
            coreList.add(thread);
            thread.start();
        }
        if (blockingQueue.offer(command)) {
            return;
        }
        if (coreList.size() + supportList.size() < maxSize) {
            Thread thread = new SupportThread();
            supportList.add(thread);
            thread.start();
        }
        if (!blockingQueue.offer(command)) {
            rejectHandle.reject(command, this);
        }
    }


    class CoreThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable command = blockingQueue.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class SupportThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null) {
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "线程结束了！");
        }
    }
}
