/**
 * Copyright (C) 2018-2019 Expedia, Inc.
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
package com.hotels.hcommon.ssh.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.SessionFactory;

import com.hotels.hcommon.ssh.SshException;
import com.hotels.hcommon.ssh.SshSettings;

public class DefaultSessionFactorySupplier implements SessionFactorySupplier {
  private static final Logger log = LoggerFactory.getLogger(DefaultSessionFactorySupplier.class);

  private static final String PROPERTY_JSCH_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

  private final SshSettings sshSettings;
  private SessionFactory sessionFactory = null;

  public DefaultSessionFactorySupplier(SshSettings sshSettings) {
    this.sshSettings = sshSettings;
  }

  @Override
  public SessionFactory get() {
    if (sessionFactory == null) {
      try {
        synchronized (this) {
          System.setProperty(DefaultSessionFactory.PROPERTY_JSCH_KNOWN_HOSTS_FILE, sshSettings.getKnownHosts());
          DefaultSessionFactory defaultSessionFactory = new DefaultSessionFactory();
          defaultSessionFactory.setIdentitiesFromPrivateKeys(sshSettings.getPrivateKeys());
          defaultSessionFactory.setPort(sshSettings.getSshPort());
          defaultSessionFactory.setConfig(PROPERTY_JSCH_STRICT_HOST_KEY_CHECKING, strictHostKeyChecking());
          sessionFactory = new DelegatingSessionFactory(defaultSessionFactory, sshSettings.getSessionTimeout());
          log.debug("Session factory created for {}@{}:{}", sessionFactory.getUsername(), sessionFactory.getHostname(),
              sessionFactory.getPort());
        }
      } catch (JSchException | RuntimeException e) {
        throw new SshException("Unable to create factory with knownHosts="
            + sshSettings.getKnownHosts()
            + " and identityKeys="
            + sshSettings.getPrivateKeys(), e);
      }
    }
    return sessionFactory;
  }

  private String strictHostKeyChecking() {
    return sshSettings.isStrictHostKeyChecking() ? "yes" : "no";
  }

}
