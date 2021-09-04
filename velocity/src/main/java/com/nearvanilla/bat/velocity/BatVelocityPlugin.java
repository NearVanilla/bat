package com.nearvanilla.bat.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

@Plugin(
        id = "bat",
        name = "bat",
        version = "@version@",
        description = "Basic, awesome TAB plugin",
        url = "nearvanilla.com"
)
public class BatVelocityPlugin {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
