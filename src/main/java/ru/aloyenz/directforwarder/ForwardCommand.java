package ru.aloyenz.directforwarder;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import ru.aloyenz.directforwarder.direct.Forwarder;

public class ForwardCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        final CommandSource source = invocation.source();
        final String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command only for players!"));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(Component.text(Singleton.getInstance().getPrefix() + "§сДля этой команды нужен аргумент в виде сервера для подключения!"));
            return;
        }

        source.sendMessage(Component.text("§6Подключаем Вас к серверу " + args[0]));
        source.sendMessage(new Forwarder((Player) source, args[0]).forward());
    }
}
