package com.oa.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.oa.gateway.common.Result;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration
public class SentinelGatewayConfig {

    private static final String BUSINESS_SERVICE_ROUTE_ID = "business-service";

    private static final String JAVACHAIN_HISTORY_API_NAME = "javachain-session-history";
    private static final String JAVACHAIN_HISTORY_PATH_PATTERN = "^/api/javachain/session/[^/]+/history$";

    private static final int TOO_MANY_REQUESTS_CODE = 429;
    private static final String TOO_MANY_REQUESTS_MESSAGE = "请求过于频繁，请稍后再试";

    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    @Value("${gateway.sentinel.flow.business-service.count:50}")
    private double businessServiceQps;

    @Value("${gateway.sentinel.flow.javachain-session-history.count:10}")
    private double javachainHistoryQps;

    @Value("${gateway.sentinel.flow.interval-sec:1}")
    private long intervalSec;

    public SentinelGatewayConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                 ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    }

    @PostConstruct
    public void initSentinelGatewayRules() {
        registerApiDefinitions();
        registerFlowRules();
        registerBlockHandler();
    }

    private void registerApiDefinitions() {
        GatewayApiDefinitionManager.loadApiDefinitions(Set.of(createJavachainHistoryApi()));
    }

    private ApiDefinition createJavachainHistoryApi() {
        return new ApiDefinition(JAVACHAIN_HISTORY_API_NAME)
                .setPredicateItems(Set.of(regexPath(JAVACHAIN_HISTORY_PATH_PATTERN)));
    }

    private ApiPathPredicateItem regexPath(String pathPattern) {
        return new ApiPathPredicateItem()
                .setPattern(pathPattern)
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_REGEX);
    }

    private void registerFlowRules() {
        GatewayRuleManager.loadRules(Set.of(
                createRouteRule(BUSINESS_SERVICE_ROUTE_ID, businessServiceQps),
                createCustomApiRule(JAVACHAIN_HISTORY_API_NAME, javachainHistoryQps)
        ));
    }

    private GatewayFlowRule createRouteRule(String routeId, double qps) {
        return createBaseRule(routeId, qps);
    }

    private GatewayFlowRule createCustomApiRule(String apiName, double qps) {
        return createBaseRule(apiName, qps)
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME);
    }

    private GatewayFlowRule createBaseRule(String resourceName, double qps) {
        return new GatewayFlowRule(resourceName)
                .setCount(qps)
                .setIntervalSec(intervalSec);
    }

    private void registerBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, throwable) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Result.error(TOO_MANY_REQUESTS_CODE, TOO_MANY_REQUESTS_MESSAGE))
        );
    }
}