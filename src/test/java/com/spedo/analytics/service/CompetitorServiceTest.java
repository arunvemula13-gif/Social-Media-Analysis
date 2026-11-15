package com.spedo.analytics.service;

import com.spedo.analytics.dto.CompetitorBenchmarkDto;
import com.spedo.analytics.entity.*;
import com.spedo.analytics.mapper.EntityDtoMapper;
import com.spedo.analytics.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitorServiceTest {

    @Mock
    private CompetitorRepository competitorRepository;

    @Mock
    private CompetitorMetricRepository competitorMetricRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SocialAccountRepository socialAccountRepository;

    @Mock
    private SocialMetricRepository socialMetricRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private com.spedo.analytics.integration.YouTubeAdapter youTubeAdapter;

    @Mock
    private EntityDtoMapper mapper;

    @InjectMocks
    private CompetitorService competitorService;

    @Test
    @DisplayName("Compare Competitors: calculates benchmarks and sorts by engagement rank")
    void testCompareCompetitors_RanksCorrectly() {
        Long userId = 1L;
        User user = new User("demo@spedo.io", "hash", "Demo", null, Role.ROLE_USER);
        user.setId(userId);

        Competitor c1 = new Competitor(user, "Competitor Alpha", "https://alpha.com", "Tech");
        c1.setId(101L);

        CompetitorMetric m1 = new CompetitorMetric();
        m1.setCompetitor(c1);
        m1.setPlatform(SocialPlatform.INSTAGRAM);
        m1.setFollowers(100000);
        m1.setFollowerGrowth(200);
        m1.setEngagementRate(new BigDecimal("4.5000"));
        m1.setPostingFrequencyPerWeek(new BigDecimal("5.0"));
        m1.setRecordedDate(LocalDate.now());

        when(socialAccountRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(competitorRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(c1));
        when(competitorMetricRepository.findLatestByCompetitorAndPlatform(eq(101L), any())).thenReturn(List.of(m1));

        List<CompetitorBenchmarkDto> benchmarks = competitorService.compareCompetitors(userId, null, "engagement");

        assertFalse(benchmarks.isEmpty());
        assertEquals(1, benchmarks.size());
        assertEquals("Competitor Alpha", benchmarks.get(0).getName());
        assertEquals(1, benchmarks.get(0).getRank());
        assertEquals(new BigDecimal("4.5000"), benchmarks.get(0).getEngagementRate());
    }
}
