package com.spedo.analytics.scheduler;

import com.spedo.analytics.entity.SocialAccount;
import com.spedo.analytics.repository.SocialAccountRepository;
import com.spedo.analytics.service.SocialAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetricsSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(MetricsSyncScheduler.class);

    private final SocialAccountRepository socialAccountRepository;
    private final SocialAccountService socialAccountService;

    public MetricsSyncScheduler(SocialAccountRepository socialAccountRepository,
                                SocialAccountService socialAccountService) {
        this.socialAccountRepository = socialAccountRepository;
        this.socialAccountService = socialAccountService;
    }

    /**
     * Periodic synchronization of social metrics (runs every hour or as configured)
     */
    @Scheduled(cron = "${app.scheduling.sync-cron:0 0 * * * *}")
    public void syncAllActiveAccounts() {
        log.info("Starting automated hourly social account metric synchronization...");
        try {
            List<SocialAccount> accounts = socialAccountRepository.findAllWithUser();
            for (SocialAccount account : accounts) {
                try {
                    socialAccountService.syncAccount(account.getUser().getId(), account.getId());
                } catch (Throwable e) {
                    log.error("Failed to sync account id: {} (handle: {}): {}", account.getId(), account.getHandle(), e.getMessage());
                }
            }
            log.info("Automated social account metric synchronization completed for {} accounts.", accounts.size());
        } catch (Throwable t) {
            log.error("Error during scheduled metrics synchronization: {}", t.getMessage(), t);
        }
    }
}
