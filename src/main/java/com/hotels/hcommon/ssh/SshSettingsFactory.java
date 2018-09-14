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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.google.common.base.Joiner;

import com.hotels.hcommon.ssh.SshSettings.Builder;

public class SshSettingsFactory {

  private final Joiner joiner = Joiner.on(',');
  private final Builder sshSettingsBuilder = SshSettings.builder();

  public SshSettings newInstance() {
    return sshSettingsBuilder.build();
  }

  public String getRoute() {
    return sshSettingsBuilder.build().getRoute();
  }

  public void setRoute(String route) {
    sshSettingsBuilder.withRoute(route);
  }

  public int getPort() {
    return sshSettingsBuilder.build().getSshPort();
  }

  public void setPort(@Min(1) @Max(65535) int port) {
    sshSettingsBuilder.withSshPort(port);
  }

  public String getLocalHost() {
    return sshSettingsBuilder.build().getLocalHost();
  }

  public void setLocalHost(String localHost) {
    sshSettingsBuilder.withLocalHost(localHost);
  }

  public String getPrivateKeys() {
    return joiner.join(sshSettingsBuilder.build().getPrivateKeys());
  }

  public void setPrivateKeys(String privateKeys) {
    sshSettingsBuilder.withPrivateKeys(privateKeys);
  }

  public String getKnownHosts() {
    return sshSettingsBuilder.build().getKnownHosts();
  }

  public void setKnownHosts(String knownHosts) {
    sshSettingsBuilder.withKnownHosts(knownHosts);
  }

  public int getTimeout() {
    return sshSettingsBuilder.build().getSessionTimeout();
  }

  public void setTimeout(int timeout) {
    sshSettingsBuilder.withSessionTimeout(timeout);
  }

  public String getStrictHostKeyChecking() {
    if (sshSettingsBuilder.build().isStrictHostKeyChecking()) {
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
    }
  }

}
