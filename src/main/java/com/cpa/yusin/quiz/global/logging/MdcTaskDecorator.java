package com.cpa.yusin.quiz.global.logging;

import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> callerContext = LogMdcContext.copyContextMap();

        return () -> {
            Map<String, String> executorContext = LogMdcContext.copyContextMap();

            try {
                LogMdcContext.replaceContextMap(callerContext);
                runnable.run();
            } finally {
                LogMdcContext.replaceContextMap(executorContext);
            }
        };
    }
}
