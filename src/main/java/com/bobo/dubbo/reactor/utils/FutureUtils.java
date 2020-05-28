package com.bobo.dubbo.reactor.utils;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcResult;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 常量类
 *
 * @author lizhibo
 * @version 1.0.0, 2020/5/23
 * @since 1.0.0, 2020/5/23
 */
public class FutureUtils {

    private static final Logger logger = LoggerFactory.getLogger(FutureUtils.class);

    /**
     * 针对老版本dubbo 从Future 转换成CompletableFuture
     */
    public static CompletableFuture<?> convert(Future<?> future) {
        if (future instanceof FutureAdapter) {
            FutureAdapter<?> futureAdapter = (FutureAdapter<?>) future;
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            futureAdapter.getFuture().setCallback(new ResponseCallback() {
                @Override
                public void done(Object o) {
                    if (o instanceof DecodeableRpcResult) {
                        completableFuture.complete(((DecodeableRpcResult) o).getValue());
                    } else {
                        logger.warn("result is not a DecodeableRpcResult type,result type is ==== {}", o.getClass().getName());
                        completableFuture.complete(o);
                    }
                }

                @Override
                public void caught(Throwable throwable) {
                    completableFuture.completeExceptionally(throwable);
                }
            });
            return completableFuture;
        } else {
            logger.warn("future is not a FutureAdapter type, future type is ==== {}", future.getClass().getName());
            try {
                //这里其实不会走的到,只不过为了能让编译通过
                return CompletableFuture.completedFuture(future.get());
            } catch (Exception e) {
                CompletableFuture<Object> exceptionFuture = new CompletableFuture<>();
                exceptionFuture.completeExceptionally(e);
                return exceptionFuture;
            }
        }
    }

    /**
     * 新版本dubbo会调这个方法，只是为了兼容老版本用，不做处理
     */
    public static CompletableFuture<?> convert(CompletableFuture<?> future) {
        return future;
    }
}
