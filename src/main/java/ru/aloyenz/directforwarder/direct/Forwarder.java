package ru.aloyenz.directforwarder.direct;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import ru.aloyenz.directforwarder.Singleton;
import ru.aloyenz.directforwarder.Pair;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private Component replacer(String normaled, String name, @Nullable String error, @Nullable String domain) {
        if (error == null) {
            error = "null";
        }
        if (domain == null) {
            domain = "null";
        }
        return Component.text(normaled.replace("{server}", name)
                .replace("{error}", error)
                .replace("{domain}", domain));
    }

    private Pair<Boolean, String> checkSuccesfull(CompletableFuture<ConnectionRequestBuilder.Result> result) throws ExecutionException, InterruptedException {
        String message;

        if (result.get().getReasonComponent().isPresent()) {
            message = result.get().getReasonComponent().get().toString();
        } else {
            message = "unknown";
        }

        return new Pair<>(result.get().isSuccessful(), message);
    }

    public Component forward() {
        final Optional<RegisteredServer> server = singleton.getServer().getServer(name);
        if (player.getCurrentServer().isEmpty()) {
            return Component.text("§cВы должны подключиться к любому серверу перед использованием данной команды!");
        }
        if (player.getCurrentServer().get().getServer().getServerInfo().getName().equals(name)) {
            return Component.text("§cВы уже подключены к данному серверу!");
        }

        Pair<Boolean, String> resulter;

        if (server.isEmpty()) {
            // Код для прямого телепорта
            logger.info("Player "
                    + player.getUsername()
                    + " (/" + player.getRemoteAddress().toString()
                    + ") requesting to directly connect to a server "
                    + name);

            DirectServer directServer;
            try {
                directServer = new DirectServer(name, host);
            } catch (Exception ex) {
                logger.warn("Unable to fetch IPv4 address for host " + host);
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        ex.getMessage(),
                        host);
            }

            final CompletableFuture<ConnectionRequestBuilder.Result> result = player
                    .createConnectionRequest(
                            directServer
                                    .getServer()
                    ).connect();

            try {
                resulter = checkSuccesfull(result);
            } catch (ExecutionException | InterruptedException ex) {
                logger.error("Exception has been occured while teleport player "
                        + player.getUsername()
                        + " to a direct server "
                        + name, ex);
                return replacer(singleton.getErrorTeleport(),
                        name,
                        ex.getMessage(),
                        host);
            }

            if (!resulter.getKey()) {
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        resulter.getValue(),
                        host);
            } else {
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        null,
                        host);
            }
        } else { // Телепорт по зарегистрированному серверу
            logger.info("Player "
                    + player.getUsername()
                    + " (/" + player.getRemoteAddress().toString()
                    + ") requesting to connect to a server "
                    + name);
            final CompletableFuture<ConnectionRequestBuilder.Result> result = player
                    .createConnectionRequest(
                            server.get()
                    ).connect();
            try {
                resulter = checkSuccesfull(result);
            } catch (ExecutionException | InterruptedException ex) {
                logger.error("Exception has been occured while teleport player "
                        + player.getUsername()
                        + " to a registered server "
                        + name,
                        ex);
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        ex.getMessage(),
                        null);
            }

            if (!resulter.getKey()) {
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        resulter.getValue(),
                        null);
            } else {
                return replacer(singleton.getErrorDirectTeleport(),
                        name,
                        null,
                        null);
            }
        }
    }
}
