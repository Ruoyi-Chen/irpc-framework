package org.idea.irpc.framework.core.common.cache;

import org.idea.irpc.framework.core.registry.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {
    /**
     * 服务提供的类
     */
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<String, Object>();

    /**
     * 服务提供者URL列表
     */
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
}
