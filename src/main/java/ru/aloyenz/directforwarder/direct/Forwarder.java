package ru.aloyenz.directforwarder.direct;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import ru.aloyenz.directforwarder.Singleton;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Forwarder {

    private final Player player;
    private final String host;
    private final String name;
    private final Singleton singleton;
    private final Logger logger;

    public Forwarder(Player player, String name) {
        this.player = player;
        this.host = name + "." + Singleton.getInstance().getDomain();
        this.name = name;
        this.singleton = Singleton.getInstance();
        this.logger = singleton.getLogger();
    }

    public Component forward() {
        Optional<RegisteredServer> server = singleton.getServer().getServer(name);
        if (!player.getCurrentServer().isPresent()) {
            return Component.text("§cВы должны подключиться к любому серверу перед использованием данной команды!");
        }
        if (player.getCurrentServer().get().getServer().getServerInfo().getName().equals(name)) {
            return Component.text("§cВы уже подключены к данному серверу!");
        }

        if (server.isEmpty()) {
            // Код для прямого телепорта
            singleton.getLogger().info("Player " + player.getUsername() + " (/" + player.getRemoteAddress().toString() + ") requesting to directly connect to a server " + name);

            DirectServer directServer;
            try {
                directServer = new DirectServer(name, host);
            } catch (Exception ex) {
                logger.warn("Unable to fetch IPv4 address for host " + host);
                return Component.text(singleton.getErrorDirectTeleport()
                        .replace("{server}", name)
                        .replace("{error}", ex.getMessage())
                        .replace("{domain}", host));
            }

            ConnectionRequestBuilder builder = player.createConnectionRequest(directServer.getServer());
            CompletableFuture<ConnectionRequestBuilder.Result> result = builder.connect();
            boolean rs;
            String message;
            try {
                rs = result.get().isSuccessful();
                if (result.get().getReasonComponent().isPresent()) {
                    message = result.get().getReasonComponent().get().toString();
                } else {
                    message = "unknown";
                }
            } catch (Exception ex) {
                logger.error("Exception has been occured while teleport player " + player.getUsername() + " to a direct server " + name, ex);
                return Component.text(singleton.getErrorTeleport()
                        .replace("{server}", name)
                        .replace("{error}", ex.getMessage())
                        .replace("{domain}", host));
            }

            if (!rs) {
                return Component.text(singleton.getErrorDirectTeleport()
                        .replace("{server}", name)
                        .replace("{error}", message)
                        .replace("{domain", host));
            } else {
                return Component.text(singleton.getDirectTeleport()
                        .replace("{server}", name)
                        .replace("{domain}", host));
            }
        } else { // Телепорт по зарегистрированному серверу
            singleton.getLogger().info("Player " + player.getUsername() + " (/" + player.getRemoteAddress().toString() + ") requesting to connect to a server " + name);
            ConnectionRequestBuilder builder = player.createConnectionRequest(server.get());
            CompletableFuture<ConnectionRequestBuilder.Result> result = builder.connect();
            boolean rs;
            String message;
            try {
                rs = result.get().isSuccessful();
                if (result.get().getReasonComponent().isPresent()) {
                    message = result.get().getReasonComponent().get().toString();
                } else {
                    message = "unknown";
                }
            } catch (Exception ex) {
                logger.error("Exception has been occured while teleport player " + player.getUsername() + " to a registered server " + name, ex);
                return Component.text(singleton.getErrorTeleport()
                        .replace("{server}", name)
                        .replace("{error}", ex.getMessage()));
            }

            if (!rs) {
                return Component.text(singleton.getErrorTeleport()
                        .replace("{server}", name)
                        .replace("{error}", message));
            } else {
                return Component.text(singleton.getTeleport()
                        .replace("{server}", name));
            }
        }
    }
}
