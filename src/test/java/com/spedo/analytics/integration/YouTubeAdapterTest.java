package com.spedo.analytics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.entity.SocialMetric;
import com.spedo.analytics.exception.BadRequestException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class YouTubeAdapterTest {

    private HttpServer server;
    private YouTubeAdapter adapter;
    private final AtomicReference<String> requestQuery = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/channels", exchange -> {
            requestQuery.set(exchange.getRequestURI().getRawQuery());
            byte[] response = """
                    {"items":[{"snippet":{"title":"Live Channel","customUrl":"@livechannel","thumbnails":{"high":{"url":"https://example.test/channel.jpg"}}},"statistics":{"subscriberCount":"1234","viewCount":"987654","videoCount":"22"}}]}
                    """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var body = exchange.getResponseBody()) {
                body.write(response);
            }
        });
        server.start();

        adapter = new YouTubeAdapter(HttpClient.newHttpClient(), new ObjectMapper());
        ReflectionTestUtils.setField(adapter, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(adapter, "apiUrl",
                "http://127.0.0.1:" + server.getAddress().getPort() + "/channels");
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void syncMetricsUsesPublicChannelStatisticsWithoutGeneratingEngagement() {
        SocialAccount account = new SocialAccount();
        account.setAccountId("youtube_livechannel");
        account.setAccountName("Old Name");
        account.setHandle("@livechannel");
        account.setFollowerCount(1000);
        account.setDemo(true);

        SyncResult result = adapter.syncMetrics(account);

        assertTrue(result.isSuccess());
        assertEquals(1234, result.getCurrentFollowers());
        assertEquals(234, result.getFollowerGrowth());
        assertEquals("Live Channel", account.getAccountName());
        assertEquals("@livechannel", account.getHandle());
        assertEquals("https://example.test/channel.jpg", account.getProfileImageUrl());
        assertEquals(987654, account.getChannelViews());
        assertEquals(22, account.getVideoCount());
        assertFalse(account.isDemo());

        SocialMetric metric = result.getLatestMetric();
        assertNotNull(metric);
        assertEquals(0, metric.getImpressions());
        assertEquals(0, metric.getReach());
        assertEquals(0, metric.getTotalLikes());
        assertEquals(0, metric.getTotalComments());
        assertEquals(0, metric.getTotalShares());
        assertEquals("part=snippet%2Cstatistics&forHandle=%40livechannel&key=test-api-key", requestQuery.get());
    }

    @Test
    void syncMetricsRejectsMissingApiKeyInsteadOfUsingDemoValues() {
        ReflectionTestUtils.setField(adapter, "apiKey", "");

        assertThrows(BadRequestException.class, () -> adapter.syncMetrics(new SocialAccount()));
        assertNull(requestQuery.get());
    }
}
