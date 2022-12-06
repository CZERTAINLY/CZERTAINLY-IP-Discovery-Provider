package com.czertainly.discovery.ip.config;

import com.czertainly.api.model.core.discovery.DiscoveryStatus;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;
import com.czertainly.discovery.ip.service.DiscoveryHistoryService;
import com.czertainly.discovery.ip.util.MetaDefinitions;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class CustomAsyncConfigurer implements AsyncConfigurer {

    private DiscoveryHistoryService discoveryHistoryService;

    @Autowired
    public void setDiscoveryHistoryService(DiscoveryHistoryService discoveryHistoryService) {
        this.discoveryHistoryService = discoveryHistoryService;
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("CertificateDiscovery-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            if (method.getName().equals("discoverCertificate")) {
                DiscoveryHistory history = (DiscoveryHistory) params[1];
                history.setStatus(DiscoveryStatus.FAILED);
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("reason", ex.getMessage());
                history.setMeta(MetaDefinitions.serialize(meta));
                discoveryHistoryService.setHistory(history);
            }
        };
    }
}