/*
 *  (C) Copyright 2017 TheOtherP (theotherp@gmx.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nzbhydra.config.downloading;

import com.google.common.base.Strings;
import lombok.Data;
import org.nzbhydra.config.BaseConfig;
import org.nzbhydra.config.ValidatingConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties(prefix = "downloading")
public class DownloadingConfig extends ValidatingConfig<DownloadingConfig> {

    private List<DownloaderConfig> downloaders = new ArrayList<>();
    private String saveTorrentsTo;
    private String saveNzbsTo;
    private boolean sendMagnetLinks;
    private boolean updateStatuses;
    private boolean showDownloaderStatus = true;

    @Override
    public ConfigValidationResult validateConfig(BaseConfig oldConfig, DownloadingConfig newConfig, BaseConfig newBaseConfig) {
        List<String> errors = new ArrayList<>();
        if (getSaveTorrentsTo().isPresent()) {
            File file = new File(getSaveTorrentsTo().get());
            if (!file.isAbsolute()) {
                errors.add("Torrent black hole folder " + getSaveTorrentsTo().get() + " is not absolute");
            }
            if (file.exists() && !file.isDirectory()) {
                errors.add("Torrent black hole folder " + file.getAbsolutePath() + " is a file");
            }
            if (!file.exists()) {
                boolean created = file.mkdir();
                if (!created) {
                    errors.add("Torrent black hole folder " + file.getAbsolutePath() + " could not be created");
                }
            }
        }
        List<ConfigValidationResult> validationResults = downloaders.stream().map(downloaderConfig -> downloaderConfig.validateConfig(oldConfig, downloaderConfig, newBaseConfig)).collect(Collectors.toList());
        List<String> downloaderErrors = validationResults.stream().map(ConfigValidationResult::getErrorMessages).flatMap(Collection::stream).collect(Collectors.toList());
        errors.addAll(downloaderErrors);

        List<String> warnings = validationResults.stream().map(ConfigValidationResult::getWarningMessages).flatMap(Collection::stream).collect(Collectors.toList());

        return new ConfigValidationResult(errors.isEmpty(), false, errors, warnings);
    }

    public Optional<String> getSaveTorrentsTo() {
        return Optional.ofNullable(Strings.emptyToNull(saveTorrentsTo));
    }

    public Optional<String> getSaveNzbsTo() {
        return Optional.ofNullable(saveNzbsTo);
    }

    @Override
    public DownloadingConfig prepareForSaving(BaseConfig oldBaseConfig) {
        return this;
    }

    @Override
    public DownloadingConfig updateAfterLoading() {
        return this;
    }

    @Override
    public DownloadingConfig initializeNewConfig() {
        return this;
    }

}
