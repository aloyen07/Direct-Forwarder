package ru.aloyenz.directforwarder.direct;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import ru.aloyenz.directforwarder.Singleton;

import java.net.*;
import java.util.Arrays;

public class DirectServer {

    private InetSocketAddress address;
    private String name;

    public DirectServer(String name, String host) throws UnknownHostException {

        InetAddress address1;
        this.name = name;

        address1 = Inet6Address.getByName(host);
        Singleton.getInstance().getLogger().info("Succesfully fetched IP for " + name + " (" + Arrays.toString(address1.getAddress()) + ").");

        this.address = new InetSocketAddress(address1, Singleton.getInstance().getPort());
    }

    public RegisteredServer getServer() {
        return Singleton.getInstance().getServer().createRawRegisteredServer(new ServerInfo(this.name, this.address));
    }
}
