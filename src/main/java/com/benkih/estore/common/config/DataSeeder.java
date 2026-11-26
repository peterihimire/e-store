package com.benkih.estore.common.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PermissionRoleSeeder permissionRoleSeeder;
    private final MarketplaceCatalogSeeder marketplaceCatalogSeeder;
    private final TaxShippingSeeder taxShippingSeeder;

    @Override
    public void run(String... args) {
        try {
            log.info("Starting data seeding...");

            // Seeding steps remain disabled, exactly as in the original DataSeeder.
            // The original orchestration order is kept below; uncomment to enable:
            // permissionRoleSeeder.seedPermissions();
            // permissionRoleSeeder.seedSystemRoles();
            // permissionRoleSeeder.seedSuperAdmin();
            //
            // marketplaceCatalogSeeder.seedMarketplaceAttributes();
            // marketplaceCatalogSeeder.seedBrands();
            //
            // taxShippingSeeder.seedTaxRules();
            // taxShippingSeeder.seedShippingRates();

        } catch (Exception e) {
            log.error(" Error during data seeding: {}", e.getMessage(), e);
        }
    }
}
