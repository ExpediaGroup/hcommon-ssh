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
package com.hotels.hcommon.ssh.tunnel;

import static com.hotels.hcommon.ssh.validation.Preconditions.checkArgument;

import java.io.IOException;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pastdev.jsch.tunnel.TunnelConnectionManager;

import com.hotels.hcommon.ssh.SshException;
import com.hotels.hcommon.ssh.SshSettings;
import com.hotels.hcommon.ssh.session.SessionFactorySupplier;

public class DefaultTunnelConnectionManagerFactory implements TunnelConnectionManagerFactory {
  private static final Logger log = LoggerFactory.getLogger(DefaultTunnelConnectionManagerFactory.class);

  public static final String LOCALHOST = "localhost";
  public static final int FIRST_AVAILABLE_PORT = 0;

  private static int getLocalPort() {
    try (ServerSocket socket = new ServerSocket(FIRST_AVAILABLE_PORT)) {
      return socket.getLocalPort();
    } catch (IOException | RuntimeException e) {
      throw new SshException("Unable to bind to a free localhost port", e);
    }
  }

  private final SshSettings sshSettings;
  private final SessionFactorySupplier sessionFactorySupplier;

  public DefaultTunnelConnectionManagerFactory(SshSettings sshSettings, SessionFactorySupplier sessionFactorySupplier) {
    this.sshSettings = sshSettings;
    this.sessionFactorySupplier = sessionFactorySupplier;
  }

  @Override
  public SshSettings getSshSettings() {
    return sshSettings;
  }

  @Override
  public TunnelConnectionManager create(String remoteHost, int remotePort) {
    return create(LOCALHOST, FIRST_AVAILABLE_PORT, remoteHost, remotePort);
  }

  @Override
  public TunnelConnectionManager create(String localHost, int localPort, String remoteHost, int remotePort) {
    checkArgument(localHost != null && !localHost.trim().isEmpty(), "localHost is required");
    checkArgument(0 <= localPort && localPort <= 65535,
        "localPort must a valid port number, a value between 0 and 65535");
    checkArgument(remoteHost != null && !remoteHost.trim().isEmpty(), "remoteHost is required");
    checkArgument(0 < remotePort && remotePort <= 65535,
        "remotePort must a valid port number, a value between 1 and 65535");

    if (localPort == FIRST_AVAILABLE_PORT) {
      localPort = getLocalPort();
    }

    StringBuilder tunnelExpressionBuilder = new StringBuilder(100);
    String route = getSshSettings().getRoute();
    if (route == null || route.trim().isEmpty()) {
      tunnelExpressionBuilder.append(localHost).append("->").append(remoteHost);
    } else {
      tunnelExpressionBuilder.append(route.trim().replaceAll("\\s", ""));
    }
    String tunnelExpression = tunnelExpressionBuilder
        .append("|")
        .append(localHost)
        .append(":")
        .append(localPort)
        .append(":")
        .append(remoteHost)
        .append(":")
        .append(remotePort)
        .toString();

    try {
      log.debug("Creating SSH tunnel connection manager for expression {}", tunnelExpression);
      return new TunnelConnectionManager(sessionFactorySupplier.get(), tunnelExpression);
    } catch (Exception e) {
      throw new SshException("Unable to create a TunnelConnectionManager: " + tunnelExpression, e);
    } finally {
      log.debug("SSH tunnel connection manager for expression {} has been created", tunnelExpression);
    }
  }

}
