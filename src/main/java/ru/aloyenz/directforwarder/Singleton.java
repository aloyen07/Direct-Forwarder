package ru.aloyenz.directforwarder;

import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;

import java.nio.file.Path;

import org.slf4j.Logger;

public class Singleton {

    private static final Singleton INSTANCE = new Singleton();

    public static Singleton getInstance() {
        return INSTANCE;
    }

    private Singleton() {
    }

    @Getter
    private Logger logger;

    @Getter
    private ProxyServer server;

    @Getter
    private Path dataDirectory;

    @Getter
    private String prefix;

    @Getter
    private String teleport;

    @Getter
    private String errorTeleport;

    @Getter
    private String directTeleport;

    @Getter
    private String errorDirectTeleport;

    @Getter
    private String domain;

    @Getter
    private int port;

    public void init(ProxyServer server, Logger logger, Path dataDirectory, String domain, int port) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.domain = domain;
        this.port = port;
    }

    public void initTexts(String prefix, String teleport, String errorTeleport, String directTeleport, String errorDirectTeleport) {
        this.prefix = prefix;
        this.teleport = teleport;
        this.errorTeleport = errorTeleport;
        this.directTeleport = directTeleport;
        this.errorDirectTeleport = errorDirectTeleport;
    }
}
