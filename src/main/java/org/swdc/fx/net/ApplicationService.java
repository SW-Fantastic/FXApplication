package org.swdc.fx.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXApplication;
import org.swdc.fx.net.data.ExternalMessage;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationService {

    private AsynchronousServerSocketChannel serverSocket;

    private int port;

    private static Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private FXApplication application;

    private ApplicationHandler applicationHandler;

    public ApplicationService(FXApplication application) {
        this.application = application;
    }

    public boolean startUp() {
        try {
            logger.info("application service starting..");
            Path path = Paths.get(".port");
            if (Files.exists(path)) {
                Integer port = Integer.valueOf(Files.readString(path));
                if (port.equals(this.port)) {
                    if (serverSocket.isOpen()) {
                        return true;
                    } else {
                        serverSocket = AsynchronousServerSocketChannel.open();
                        serverSocket.bind(new InetSocketAddress(port));
                        this.applicationHandler = new ApplicationHandler(serverSocket,application);
                        serverSocket.accept(application, this.applicationHandler);
                        return true;
                    }
                } else {
                    try {
                        serverSocket = AsynchronousServerSocketChannel.open();
                        serverSocket.bind(new InetSocketAddress(port));
                        this.applicationHandler = new ApplicationHandler(serverSocket,application);
                        serverSocket.accept(application, this.applicationHandler);
                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }
            }
            serverSocket = AsynchronousServerSocketChannel.open();
            serverSocket.bind(new InetSocketAddress(0));
            this.applicationHandler = new ApplicationHandler(serverSocket,application);
            serverSocket.accept(application, this.applicationHandler);
            InetSocketAddress address = (InetSocketAddress) serverSocket.getLocalAddress();
            port = address.getPort();
            Files.write(path, (port + "").getBytes());
            logger.info("application service ready.");
            return true;
        } catch (Exception e) {
            logger.error("fail to startup application service", e);
            return false;
        }
    }

    public void shutdown() {
        try {
            if (serverSocket != null && serverSocket.isOpen()) {
                serverSocket.close();
                Path port = Paths.get(".port");
                Files.delete(port);
                logger.info("application service shutdown.");
            }
        } catch (Exception e) {
            logger.error("fail to shutdown application service");
        }
    }

    public void addListener(ExternalHandler handler) {
        applicationHandler.addListener(handler);
    }

    public void pushMessage(ExternalMessage message) {
        try {
            String port = Files.readString(Paths.get(".port"));
            InetSocketAddress socketAddress = new InetSocketAddress("localhost",Integer.parseInt(port));
            Socket socket = new Socket();
            socket.connect(socketAddress);
            ObjectMapper mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(message);
            socket.getOutputStream().write(data.getBytes());
            socket.close();
        } catch (Exception e) {
            logger.error("fail to push message: " + message.getTargetClass().getName(),e);
        }
    }

    public void removeListener(ExternalHandler handler) {
        applicationHandler.removeListener(handler);
    }

}
