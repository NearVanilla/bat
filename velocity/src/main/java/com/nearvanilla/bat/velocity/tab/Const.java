package com.nearvanilla.bat.velocity.tab;

import net.luckperms.api.query.Flag;
import net.luckperms.api.query.QueryOptions;

public class Const {
    public static final QueryOptions LP_QUERY_RECURSIVE = QueryOptions.defaultContextualOptions().toBuilder().flag(Flag.RESOLVE_INHERITANCE, true).build();
}
