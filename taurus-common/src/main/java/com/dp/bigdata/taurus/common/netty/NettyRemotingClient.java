package com.dp.bigdata.taurus.common.netty;

import com.dp.bigdata.taurus.common.netty.codec.NettyDecoder;
import com.dp.bigdata.taurus.common.netty.codec.NettyEncoder;
import com.dp.bigdata.taurus.common.netty.config.NettyClientConfig;
import com.dp.bigdata.taurus.common.netty.exception.RemotingSendRequestException;
import com.dp.bigdata.taurus.common.netty.protocol.Command;
import com.dp.bigdata.taurus.common.netty.remote.RemotingClient;
import com.dp.bigdata.taurus.common.netty.remote.RemotingHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author   mingdongli
 * 16/5/18  上午10:14.
 */
@Component
public class NettyRemotingClient implements RemotingClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRemotingClient.class);

    @Value("${task.executor.port}")
    public int sendPort;

    private NettyClientConfig nettyClientConfig;

    private final Bootstrap bootstrap = new Bootstrap();

    private EventLoopGroup eventLoopGroupWorker;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    @Autowired
    public NettyRemotingClient(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;
        eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    /**
     *
     * @param address
     * @param port
     * @param command
     * @return
     * @throws RemotingSendRequestException
     */
    public boolean send(String address, int port, final Command command) throws
            RemotingSendRequestException{
        Channel channel = getChannel(address, port);
        if (channel == null) {
            logger.error("Please check  specified target address. If the address is ok, check " +
                    "network.");
            throw new RemotingSendRequestException("Network encounter error!");
        }
        try {
            ChannelFuture future = channel.writeAndFlush(command).await();
            if (future.isSuccess()) {
                logger.info("Command : {} Send successfully.", command);
                return true;
            } else {
                logger.info("Command : {} Send failed.", command);
                logger.info("Failed caused by :",future.cause());
                return false;
            }
        } catch (Exception e) {
            logger.error("Send command {} to address {} encounter error.", command, address);
            throw new RemotingSendRequestException("Send command: + " + command + ",to " +
                    "address:" + address + "encounter error.", e);
        }

    }

    @Override
    public boolean send(String address, final Command command) throws RemotingSendRequestException {
        Channel channel = getChannel(address);
        if (channel == null) {
            logger.error("Please check  specified target address. If the address is ok, check " +
                    "network.");
            throw new RemotingSendRequestException("Network encounter error!");
        }
        try {
            ChannelFuture future = channel.writeAndFlush(command).await();
            if (future.isSuccess()) {
                logger.info("Command : {} Send successfully.", command);
                return true;
            } else {
                logger.info("Command : {} Send failed.", command);
                logger.info("Failed caused by :",future.cause());
                return false;
            }
        } catch (Exception e) {
            logger.error("Send command {} to address {} encounter error.", command, address);
            throw new RemotingSendRequestException("Send command: + " + command + ",to " +
                    "address:" + address + "encounter error.", e);
        }
    }

    @PostConstruct
    public void start() {
        this.nettyClientConfig.setConnectPort(sendPort);
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(//
                nettyClientConfig.getClientWorkerThreads(), //
                new ThreadFactory() {

                    private AtomicInteger threadIndex = new AtomicInteger(0);


                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
                    }
                });

        this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)//
                //
                .option(ChannelOption.TCP_NODELAY, true)
                        //
                .option(ChannelOption.SO_SNDBUF, nettyClientConfig.getSocketSndbufSize())
                        //
                .option(ChannelOption.SO_RCVBUF, nettyClientConfig.getSocketSndbufSize())
                        //
                .handler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(//
                                defaultEventExecutorGroup, //
                                new NettyEncoder(), //
                                new NettyDecoder(), //
                                new NettyConnectManageHandler());
                    }
                });
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(ctx.channel());
            logger.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
            logger.warn("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

            ctx.channel().close();
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutdown netty remoting client...");
        try {
            for (Channel cw : this.channels.values()) {
                cw.close();
            }
            this.channels.clear();
            this.eventLoopGroupWorker.shutdownGracefully();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("NettyRemotingClient shutdown exception, ", e);
        }


    }

    public Channel getChannel(String address, int port){
        if (address == null){
            return null;
        }
        Channel id = channels.get(address + ":" + port);
        if (id == null || !id.isActive()) {
            return createNewChannel(address, port);
        }
        return id;
    }


    public Channel getChannel(String address) {
        return getChannel(address, sendPort);
    }

    //TODO 需要枷锁吗？
    private Channel createNewChannel(String address, int port) {
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(new InetSocketAddress(address, port)).await();
        } catch (Exception e) {
            logger.info("Connect to TargetServer encounter error.", e);
            return null;
        }
        if(future.isSuccess()){
            Channel channel = future.channel();
            channels.put(address + ":" + port, channel);
            return channel;
        }else{
            logger.error("Connect to TargetServer failed.", future.cause());
            return null;
        }
    }
}
