package com.tuktak.inventory.service;

import com.tuktak.inventory.entity.SiteSettings;
import com.tuktak.inventory.repository.SiteSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteSettingsService {

    private final SiteSettingsRepository siteSettingsRepository;

    // Default values
    private static final String FREE_DELIVERY_THRESHOLD = "free_delivery_threshold";
    private static final String ANNOUNCEMENT_TEXT = "announcement_text";
    private static final String ANNOUNCEMENT_ACTIVE = "announcement_active";
    private static final String SHOP_NAME = "shop_name";
    private static final String SHOP_PHONE = "shop_phone";
    private static final String SHOP_EMAIL = "shop_email";
    private static final String SHOP_ADDRESS = "shop_address";

    @Transactional(readOnly = true)
    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        // Set defaults
        settings.put(FREE_DELIVERY_THRESHOLD, "0");
        settings.put(ANNOUNCEMENT_TEXT, "");
        settings.put(ANNOUNCEMENT_ACTIVE, "false");
        settings.put(SHOP_NAME, "TukTak Deal");
        settings.put(SHOP_PHONE, "");
        settings.put(SHOP_EMAIL, "");
        settings.put(SHOP_ADDRESS, "");

        // Override with DB values
        siteSettingsRepository.findAll().forEach(s ->
                settings.put(s.getSettingKey(), s.getSettingValue()));
        return settings;
    }

    @Transactional
    public Map<String, String> updateSettings(Map<String, String> newSettings) {
        for (Map.Entry<String, String> entry : newSettings.entrySet()) {
            SiteSettings setting = siteSettingsRepository
                    .findBySettingKey(entry.getKey())
                    .orElse(SiteSettings.builder()
                            .settingKey(entry.getKey())
                            .build());
            setting.setSettingValue(entry.getValue());
            siteSettingsRepository.save(setting);
        }
        log.info("Site settings updated: {}", newSettings.keySet());
        return getAllSettings();
    }

    @Transactional(readOnly = true)
    public String getSetting(String key) {
        return siteSettingsRepository.findBySettingKey(key)
                .map(SiteSettings::getSettingValue)
                .orElse("");
    }

    @Transactional(readOnly = true)
    public double getFreeDeliveryThreshold() {
        String value = getSetting(FREE_DELIVERY_THRESHOLD);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}