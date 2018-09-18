/**
 * Copyright (C) 2018 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.hcommon.ssh;

import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

import com.hotels.hcommon.ssh.SshSettings.Builder;
import com.hotels.hcommon.ssh.validation.constraint.TunnelRoute;

public class SshSettingsMapper {

  private final Builder sshSettingsBuilder = SshSettings.builder();
  private String wrongStrictHostKeyChecking;
  private boolean strictHostKeyCheckingIsWrong = false;

  @SuppressWarnings("deprecation")
  public @NotBlank @TunnelRoute String getRoute() {
    return buildSettings().getRoute();
  }

  public void setRoute(String route) {
    sshSettingsBuilder.withRoute(route);
  }

  public @Min(1) @Max(65535) int getPort() {
    return buildSettings().getSshPort();
  }

  public void setPort(int port) {
    sshSettingsBuilder.withSshPort(port);
  }

  public String getLocalHost() {
    return buildSettings().getLocalHost();
  }

  public void setLocalHost(String localHost) {
    sshSettingsBuilder.withLocalHost(localHost);
  }

  public @NotBlank String getPrivateKeys() {
    return join(buildSettings().getPrivateKeys());
  }

  public void setPrivateKeys(String privateKeys) {
    sshSettingsBuilder.withPrivateKeys(privateKeys);
  }

  public @NotBlank String getKnownHosts() {
    return buildSettings().getKnownHosts();
  }

  public void setKnownHosts(String knownHosts) {
    sshSettingsBuilder.withKnownHosts(knownHosts);
  }

  public @Min(0) @Max(Integer.MAX_VALUE) int getTimeout() {
    return buildSettings().getSessionTimeout();
  }

  public void setTimeout(int timeout) {
    sshSettingsBuilder.withSessionTimeout(timeout);
  }

  public @Pattern(regexp = "^(?i:yes\\b|no\\b)") String getStrictHostKeyChecking() {
    if (wrongStrictHostKeyChecking != null && strictHostKeyCheckingIsWrong) {
      strictHostKeyCheckingIsWrong = false;
      return wrongStrictHostKeyChecking;
    }

    if (buildSettings().isStrictHostKeyChecking()) {
      return "yes";
    } else {
      return "no";
    }
  }

  public void setStrictHostKeyChecking(String strictHostKeyChecking) {
    if (strictHostKeyChecking.toLowerCase() == "yes") {
      sshSettingsBuilder.withStrictHostKeyChecking(true);
    } else if (strictHostKeyChecking.toLowerCase() == "no") {
      sshSettingsBuilder.withStrictHostKeyChecking(false);
    } else {
      wrongStrictHostKeyChecking = strictHostKeyChecking;
      strictHostKeyCheckingIsWrong = true;
    }
  }

  private SshSettings buildSettings() {
    return sshSettingsBuilder.buildWithoutCheck();
  }

  // TODO: replace with String.join when project is moved to Java 8
  // Taken from Guava Joiner method appendTo with a few changes
  // we know that the list will have at least one element
  private String join(List<String> list) {
    StringBuilder joinedList = new StringBuilder();

    Iterator<String> iterator = list.iterator();
    joinedList.append(iterator.next().trim());
    while (iterator.hasNext()) {
      joinedList.append(',');
      joinedList.append(iterator.next().trim());
    }

    return joinedList.toString();
  }

}
