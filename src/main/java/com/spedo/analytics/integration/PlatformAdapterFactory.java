package com.spedo.analytics.integration;

import com.spedo.analytics.entity.SocialPlatform;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PlatformAdapterFactory {

    private final Map<SocialPlatform, SocialPlatformAdapter> adapterMap = new EnumMap<>(SocialPlatform.class);

    public PlatformAdapterFactory(List<SocialPlatformAdapter> adapters) {
        for (SocialPlatformAdapter adapter : adapters) {
            adapterMap.put(adapter.getPlatform(), adapter);
        }
    }

    public SocialPlatformAdapter getAdapter(SocialPlatform platform) {
        SocialPlatformAdapter adapter = adapterMap.get(platform);
        if (adapter == null) {
            throw new IllegalArgumentException("No adapter configured for platform: " + platform);
        }
        return adapter;
    }
}
