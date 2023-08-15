package ru.aloyenz.directforwarder;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@Plugin(id = "directforwarder", name = "Direct Forwarder", version = BuildConstants.VERSION,
    url = BuildConstants.PRODUCTS, authors = {BuildConstants.AUTHOR})
public class DirectForwarder {

    private final Logger logger;
    private final ProxyServer server;

    @Inject
    public DirectForwarder(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws IOException {

        this.logger = logger;
        this.server = server;

        final File directory = dataDirectory.toFile();
        if (!directory.exists()) {
            directory.mkdir();
        }

        final File config = new File(directory + File.separator + "config.toml");
        if (!config.exists()) {
            if (!config.createNewFile()) {
                logger.error("Undable to create a new config file!");
                return;
            }

            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(config, StandardCharsets.UTF_8);
                final InputStream in = DirectForwarder.class.getResourceAsStream("/configuration/config.toml");
                assert in != null;
                final Reader is = new InputStreamReader(in, StandardCharsets.UTF_8);

                final StringBuilder contents = new StringBuilder();

                int content;
                while ((content = is.read()) != -1) {
                    contents.append((char) content);
                }

                fileWriter.write(contents.toString());
                fileWriter.flush();
            } catch (IOException exception) {
                logger.error("Unable to write a new config file!", exception);
                return;
            }
        }

        final Toml toml = new Toml().read(config);

        Singleton.getInstance().init(server, logger, dataDirectory,
                toml.getString("global.domain"),
                Integer.parseInt(toml.getString("global.port")));

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
        logger.info("I will be happy if you visit my future server at the link "
                + BuildConstants.SITE
                + " or order something else on "
                + BuildConstants.PRODUCTS);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Re-registering an original /server command");
        this.server.getCommandManager().unregister("server");
        this.server.getCommandManager().register("server", new ForwardCommand());
    }
}
