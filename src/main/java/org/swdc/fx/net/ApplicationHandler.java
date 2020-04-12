package org.swdc.fx.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXApplication;
import org.swdc.fx.net.data.ExternalMessage;
import org.swdc.fx.util.Util;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class ApplicationHandler implements CompletionHandler<AsynchronousSocketChannel,FXApplication> {

    private AsynchronousServerSocketChannel serverSocket;

    private List<ExternalHandler> handlers = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(ApplicationHandler.class);

    ApplicationHandler(AsynchronousServerSocketChannel serverSocketChannel) {
        this.serverSocket = serverSocketChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel result, FXApplication attachment) {
        try {
            ByteBuffer data = ByteBuffer.allocate(2048);
            result.read(data).get();
            data.flip();
            String msg = new String(data.array(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            ExternalMessage externalMessage = mapper.readValue(msg, ExternalMessage.class);
            Class clazz = externalMessage.getTargetClass();
            Object message = mapper.readValue(externalMessage.getData(), clazz);
            handlers.stream()
                    .filter(h ->h.support(clazz))
                    .forEach(h -> h.accept(attachment, message));
        } catch (Exception e) {
            logger.error("fail to resolve message: ",e);
        }
        serverSocket.accept(attachment,this);
    }

    @Override
    public void failed(Throwable exc, FXApplication attachment) {
        serverSocket.accept(attachment,this);
    }

    public void addListener(ExternalHandler handler) {
        handlers.add(handler);
    }

    public void removeListener(ExternalHandler handler) {
        handlers.remove(handler);
    }

}
