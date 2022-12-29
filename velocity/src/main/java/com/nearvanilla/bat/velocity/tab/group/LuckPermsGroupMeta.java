package com.nearvanilla.bat.velocity.tab.group;

import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;

public class LuckPermsGroupMeta {

    private final CachedMetaData metaData;

    public LuckPermsGroupMeta(Group group) {
        this.metaData = group.getCachedData().getMetaData();
    }

    public CachedMetaData getMetaData() {
        return metaData;
    }
}
