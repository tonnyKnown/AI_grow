package com.oa.business.job;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DemoJobHandler {

    @XxlJob("demoJobHandler")
    public ReturnT<String> demoJobHandler(String param) {
        XxlJobHelper.log("XXL-JOB, Hello World.");
        // 编写自己的业务逻辑
        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("beat at:" + i);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return new ReturnT<>(200, "SUCCESS");
    }
}
