package cn.thens.xflow.scheduler;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

import cn.thens.xflow.TestX;

/**
 * @author 7hens
 */
public class SchedulerTest {
    @Test
    public void unconfined() {
        TestX.Logger logger = TestX.logger();
        Schedulers.unconfined().schedule(() -> {
            logger.log("A");
        });
        logger.log("B");

        Schedulers.unconfined().schedule(() -> {
            logger.log("C");
        }, 1, TimeUnit.SECONDS);
        logger.log("D");

        Schedulers.unconfined().schedulePeriodically(() -> {
            logger.log("E: " + System.currentTimeMillis());
        }, 2, 1, TimeUnit.SECONDS);
        logger.log("F");
    }

    @Test
    public void processId() {
        TestX.Logger logger = TestX.logger();
        logger.log("threadId: " + Thread.currentThread().getId());

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        logger.log("processName: " + runtimeMXBean.getName());
        logger.log("vmVendor: " + runtimeMXBean.getVmVendor());
    }
}
