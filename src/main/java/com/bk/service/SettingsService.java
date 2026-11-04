package com.bk.service;

import com.bk.model.AppSetting;
import com.bk.repository.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    public static final String REPORTS_PUBLISHED = "reports_published";
    public static final String VOTING_OPEN = "voting_open";

    private final AppSettingRepository settingRepository;

    public SettingsService(AppSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    @Transactional(readOnly = true)
    public boolean areReportsPublished() {
        return getBooleanSetting(REPORTS_PUBLISHED, false);
    }

    @Transactional
    public void publishReports(boolean published) {
        setBooleanSetting(REPORTS_PUBLISHED, published);
    }

    @Transactional(readOnly = true)
    public boolean isVotingOpen() {
        return getBooleanSetting(VOTING_OPEN, false);
    }

    @Transactional
    public void setVotingOpen(boolean open) {
        setBooleanSetting(VOTING_OPEN, open);
    }

    private boolean getBooleanSetting(String key, boolean defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(AppSetting::getSettingValue)
                .map(value -> {
                    if ("true".equalsIgnoreCase(value)) {
                        return true;
                    }
                    if ("false".equalsIgnoreCase(value)) {
                        return false;
                    }
                    throw new IllegalStateException("Invalid boolean value for setting '" + key + "'.");
                })
                .orElse(defaultValue);
    }

    private void setBooleanSetting(String key, boolean value) {
        AppSetting setting = settingRepository.findBySettingKey(key)
                .orElseGet(() -> new AppSetting(key, Boolean.toString(value)));
        setting.setSettingValue(Boolean.toString(value));
        settingRepository.save(setting);
    }
}
