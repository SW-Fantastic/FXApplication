package org.swdc.fx.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXApplication;
import org.swdc.fx.net.data.ExternalMessage;
import org.swdc.fx.net.data.MainParameter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ApplicationHandler implements CompletionHandler<AsynchronousSocketChannel,FXApplication> {

    private AsynchronousServerSocketChannel serverSocket;

    private List<ExternalHandler> handlers = new ArrayList<>();

    private static final Logger logger = LoggerFactory.getLogger(ApplicationHandler.class);

    private FXApplication application;

    ApplicationHandler(AsynchronousServerSocketChannel serverSocketChannel, FXApplication application) {
        this.serverSocket = serverSocketChannel;
        this.application = application;
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
                    .forEach(h -> Platform.runLater(() -> h.accept(attachment, message)));
            String name = System.getProperty("os.name");
            if (name.toLowerCase().contains("win")) {
                if (message instanceof MainParameter) {
                    MainParameter mainParameter = (MainParameter)message;
                    String path = mainParameter.getArgs()[0];
                    File target = new File(path);
                    if (target.exists()&&target.isFile()) {
                        Platform.runLater(() -> this.application.onFileOpenRequest(target));
                    }
                }
            }
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
