package ru.gb;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.gb.message.DownloadFileRequestMessage;
import ru.gb.message.FileMessage;
import ru.gb.message.Message;
import ru.gb.message.TextMessage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ServerMessageHandler extends SimpleChannelInboundHandler<Message> {

    private static final FileResourcesUtils resources = new FileResourcesUtils();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        TextMessage textMessage = new TextMessage();
        textMessage.setText("Welcome :)");
        ctx.writeAndFlush(textMessage);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message msg) {

        if (msg instanceof DownloadFileRequestMessage) {
            var message = (DownloadFileRequestMessage) msg;

            if (message.getPath() == null) {
                sendFileListRequest(ctx);
                return;
            }

            String path = message.getPath();
            File file = resources.getFileFromResource(path);
            if (file == null) {
                TextMessage errorMessage = new TextMessage();
                errorMessage.setText("ERROR: Path [" + path + "] not found.");
                ctx.writeAndFlush(errorMessage);
                return;
            }
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                final FileMessage fileMessage = new FileMessage();
                byte[] content = new byte[(int) raf.length()];
                raf.read(content);
                fileMessage.setName(file.getName());
                fileMessage.setContent(content);
                ctx.writeAndFlush(fileMessage);
            } catch (IOException e) {
                e.printStackTrace();
                TextMessage errorMessage = new TextMessage();
                errorMessage.setText("ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
                ctx.writeAndFlush(errorMessage);
            }
        }

        if (msg instanceof TextMessage) {
            var message = (TextMessage) msg;
            System.out.println("Message from client: " + message.getText());
        }
    }

    private void sendFileListRequest(ChannelHandlerContext ctx) {
        TextMessage textMessage = new TextMessage();
        StringBuilder sb = new StringBuilder();
        sb
                .append("Enter the name of the download file or type \"q\" to exit.\n")
                .append("Available files on server:\n");
        for (String fileName : resources.getResourceFiles()) {
            sb.append(fileName).append("\n");
        }
        textMessage.setText(sb.toString());
        ctx.writeAndFlush(textMessage);
    }
}