package ru.gb;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import ru.gb.handler.JsonDecoder;
import ru.gb.handler.JsonEncoder;
import ru.gb.message.DownloadFileRequestMessage;

import java.util.Scanner;

public class Client {

    private static final int PORT = 9000;

    public static void main(String[] args) throws Exception {

        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ClientMessageHandler()
                            );
                        }
                    });
            // Start the client.
            Channel channel = bootstrap.connect("localhost", PORT).sync().channel();
            Scanner scanner = new Scanner(System.in);
            while (true) {
                // Request with empty body for show available files list
                channel.writeAndFlush(new DownloadFileRequestMessage());

                String input = scanner.nextLine();
                if ("q".equals(input)) {
                    break;
                }

                final DownloadFileRequestMessage requestMessage = new DownloadFileRequestMessage();
                requestMessage.setPath(input);
                channel.writeAndFlush(requestMessage);
            }
            channel.close();

            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } finally {
            System.out.println("By ;)");
            workerGroup.shutdownGracefully();
        }
    }
}