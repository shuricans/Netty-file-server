package ru.gb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.message.FileMessage;
import ru.gb.message.Message;
import ru.gb.message.TextMessage;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ClientMessageHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof TextMessage) {
            var message = (TextMessage) msg;
            System.out.println(message.getText());
        }

        if (msg instanceof FileMessage) {
            var message = (FileMessage) msg;
            final String homeDir = System.getProperty("user.home");
            final String path = homeDir + System.getProperty("file.separator") + message.getName();
            try (final RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
                raf.write(message.getContent());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            System.out.printf("The file named \"%s\" was uploaded successfully.%nPath: %s%n%n", message.getName(), path);
        }
    }
}
