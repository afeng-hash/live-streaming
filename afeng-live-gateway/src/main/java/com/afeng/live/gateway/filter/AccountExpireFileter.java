package com.afeng.live.gateway.filter;

import com.afeng.live.account.interfaces.IAccountTokenRPC;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

/**
 * 拦截所有请求，给有token的续期
 */
@Component
@Slf4j
public class AccountExpireFileter  implements GlobalFilter, Ordered {

    @DubboReference
    private IAccountTokenRPC accountTokenRPC;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        List<HttpCookie> httpCookieList = request.getCookies().get("qytk");

        if (CollectionUtils.isEmpty(httpCookieList)) {
            log.debug("请求没有qytk cookie，跳过续期");
            return chain.filter(exchange);
        }

        String afengTokenCookieValue = httpCookieList.get(0).getValue();
        if (StringUtils.isEmpty(afengTokenCookieValue) || StringUtils.isEmpty(afengTokenCookieValue.trim())) {
            log.debug("qytk cookie值为空，跳过续期");
            return chain.filter(exchange);
        }

        String token = afengTokenCookieValue.trim();

        Mono<Void> continueFilter = chain.filter(exchange);

        Mono.fromRunnable(() -> {
                    try {
                        accountTokenRPC.expireToken(token);
                        log.debug("Token续期成功，token: {}", maskToken(token));
                    } catch (Exception e) {
                        log.warn("Token续期失败，不影响主流程，token: {}, error: {}",
                                maskToken(token), e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return continueFilter;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
