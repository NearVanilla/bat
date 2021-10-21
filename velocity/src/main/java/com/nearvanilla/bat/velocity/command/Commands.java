package com.nearvanilla.bat.velocity.command;

import cloud.commandframework.Command;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.CommandDescription;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import cloud.commandframework.arguments.parser.ParserParameter;
import cloud.commandframework.arguments.parser.StandardParameters;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.google.inject.Inject;
import com.nearvanilla.bat.velocity.BatVelocityPlugin;
import com.nearvanilla.bat.velocity.config.ConfigLoader;
import com.nearvanilla.bat.velocity.tab.TablistService;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import cloud.commandframework.velocity.VelocityCommandManager;
import cloud.commandframework.CommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import com.velocitypowered.api.command.CommandSource;

import java.util.function.Function;

/**
 * Manages commands
 */
public class Commands {
    private final static @NonNull Component CONFIG_RELOAD_SUCCESS = Component.text("Config reloaded successfully", NamedTextColor.GREEN);
    private final static @NonNull Component CONFIG_RELOAD_FAILURE = Component.text("Error reloading config - see console for details", NamedTextColor.RED);
    private final @NonNull BatVelocityPlugin plugin;
    private final @NonNull CommandManager<CommandSource> manager;
    private final @NonNull ConfigLoader configLoader;
    private final @NonNull TablistService tablistService;

    @Inject
    public Commands(final @NonNull BatVelocityPlugin plugin,
                    final @NonNull ConfigLoader configLoader,
                    final @NonNull TablistService tablistService,
                    final @NonNull ProxyServer server
                    ) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.tablistService = tablistService;
        this.manager = new VelocityCommandManager<>(
                server.getPluginManager().ensurePluginContainer(plugin),
                server,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
                );
    }

    /**
     * Registers the commands
     */
    public void register() {
        AnnotationParser<CommandSource> annotationParser;
        annotationParser = new AnnotationParser<>(
            manager, CommandSource.class, parserParams -> SimpleCommandMeta.empty()
        );
        // Parse commands
        annotationParser.parse(this);
    }


    @CommandMethod("bat reload")
    @CommandPermission("bat.command.reload")
    @CommandDescription("Reload BATs config")
    private void reloadConfig(final @NonNull CommandSource source) {
        try {
            this.configLoader.reloadConfig();
            this.tablistService.disable();
            this.tablistService.enable();
        } catch (RuntimeException e) {
            source.sendMessage(CONFIG_RELOAD_FAILURE);
            throw e;
        }
        source.sendMessage(CONFIG_RELOAD_SUCCESS);
    }
}
