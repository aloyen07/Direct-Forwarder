package ru.aloyenz.directforwarder;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Plugin(id = "directforwarder", name = "Direct Forwarder", version = BuildConstants.VERSION,
    url = BuildConstants.PRODUCTS, authors = {BuildConstants.AUTHOR})
public class DirectForwarder {

    private final Logger logger;
    private final ProxyServer server;

    @Inject
    public DirectForwarder(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws IOException {

        File directory = dataDirectory.toFile();
        if (!directory.exists()) {
            directory.mkdir();
        }

        File config = new File(directory.toString() + "/config.toml");
        if (!config.exists()) {
            config.createNewFile();
            FileWriter fileWriter = new FileWriter(config, StandardCharsets.UTF_8);

            InputStream in = DirectForwarder.class.getResourceAsStream("/configuration/config.toml");
            assert in != null;
            Reader is = new InputStreamReader(in, StandardCharsets.UTF_8);

            StringBuilder contents = new StringBuilder();

            int content;
            while ((content = is.read()) != -1) {
                contents.append((char) content);
            }

            fileWriter.write(contents.toString());
            fileWriter.flush();
        }

        Toml toml = new Toml().read(config);

        Singleton.getInstance().init(server, logger, dataDirectory,
                toml.getString("global.domain"),
                Integer.parseInt(toml.getString("global.port")));
        this.logger = logger;
        this.server = server;

        Singleton.getInstance().initTexts(
                toml.getString("messages.prefix"),
                toml.getString("messages.teleport"),
                toml.getString("messages.errorteleport"),
                toml.getString("messages.directteleport"),
                toml.getString("messages.errordirectteleport")
        );

        logger.info("---------------------------------------");
        logger.info("           Direct Forwarder            ");
        logger.info("Version: " + BuildConstants.VERSION);
        logger.info("Author: " + BuildConstants.AUTHOR);
        logger.info("---------------------------------------");
        logger.info("I will be happy if you visit my future server at the link " + BuildConstants.SITE + " or order something else on " + BuildConstants.PRODUCTS);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Re-registering an original /server command");
        this.server.getCommandManager().unregister("server");
        this.server.getCommandManager().register("server", new ForwardCommand());
    }
}
