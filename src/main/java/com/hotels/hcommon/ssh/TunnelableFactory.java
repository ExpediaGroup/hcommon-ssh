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

import static com.hotels.hcommon.ssh.tunnel.DefaultTunnelConnectionManagerFactory.LOCALHOST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;

import com.hotels.hcommon.ssh.session.DefaultSessionFactorySupplier;
import com.hotels.hcommon.ssh.tunnel.DefaultTunnelConnectionManagerFactory;
import com.hotels.hcommon.ssh.tunnel.TunnelConnectionManagerFactory;

public class TunnelableFactory<T extends Tunnelable> {
  private static final Logger log = LoggerFactory.getLogger(TunnelableFactory.class);

  private static class TunnelingConnectableInvocationHandler<T> implements InvocationHandler {
    private final TunnelConnectionManager tunnelConnectionManager;
    private final T delegate;
    private final MethodChecker methodChecker;

    private TunnelingConnectableInvocationHandler(
        TunnelConnectionManager tunnelConnectionManager,
        T delegate,
        MethodChecker methodChecker) {
      this.tunnelConnectionManager = tunnelConnectionManager;
      this.delegate = delegate;
      this.methodChecker = methodChecker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (methodChecker.isTunnelled(method)) {
        tunnelConnectionManager.ensureOpen();
        return method.invoke(delegate, args);
      }
      if (methodChecker.isShutdown(method)) {
        Object result = method.invoke(delegate, args);
        tunnelConnectionManager.close();
        return result;
      }
      return method.invoke(delegate, args);
    }
  }

  private final TunnelConnectionManagerFactory tunnelConnectionManagerFactory;

  public TunnelableFactory(SshSettings sshSettings) {
    this(new DefaultTunnelConnectionManagerFactory(sshSettings, new DefaultSessionFactorySupplier(sshSettings)));
  }

  /* VisibleForTesting */
  TunnelableFactory(TunnelConnectionManagerFactory tunnelConnectionManagerFactory) {
    this.tunnelConnectionManagerFactory = tunnelConnectionManagerFactory;
  }

  public Tunnelable wrap(
      TunnelableSupplier<T> delegateSupplier,
      MethodChecker methodChecker,
      String remoteHost,
      int remotePort) {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create(remoteHost, remotePort);
    return wrap(delegateSupplier, methodChecker, tunnelConnectionManager, remoteHost, remotePort);
  }

  public Tunnelable wrap(
      TunnelableSupplier<T> delegateSupplier,
      MethodChecker methodChecker,
      String localHost,
      int localPort,
      String remoteHost,
      int remotePort) {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create(localHost, localPort,
        remoteHost, remotePort);
    return wrap(delegateSupplier, methodChecker, tunnelConnectionManager, remoteHost, remotePort);
  }

  private Tunnelable wrap(
      TunnelableSupplier<T> delegateSupplier,
      MethodChecker methodChecker,
      TunnelConnectionManager tunnelConnectionManager,
      String remoteHost,
      int remotePort) {
    openTunnel(remoteHost, remotePort, tunnelConnectionManager);
    T delegate = delegateSupplier.get();
    TunnelingConnectableInvocationHandler<T> tunneledHandler = new TunnelingConnectableInvocationHandler<>(
        tunnelConnectionManager, delegate, methodChecker);

    return (Tunnelable) Proxy.newProxyInstance(getClass().getClassLoader(), delegate.getClass().getInterfaces(),
        tunneledHandler);
  }

  private void openTunnel(String remoteHost, int remotePort, TunnelConnectionManager tunnelConnectionManager) {
    SshSettings sshSettings = tunnelConnectionManagerFactory.getSshSettings();
    try {
      log.debug("Creating tunnel: {}:? -> {} -> {}:{}", LOCALHOST, sshSettings.getRoute(), remoteHost, remotePort);
      int localPort = tunnelConnectionManager.getTunnel(remoteHost, remotePort).getAssignedLocalPort();
      tunnelConnectionManager.open();
      log.debug("Tunnel created: {}:{} -> {} -> {}:{}", LOCALHOST, localPort, sshSettings.getRoute(), remoteHost,
          remotePort);
    } catch (JSchException | RuntimeException e) {
      String message = String.format("Unable to establish SSH tunnel: '%s:?' -> '%s' -> '%s:%s'", LOCALHOST,
          sshSettings.getRoute(), remoteHost, remotePort);
      throw new SshException(message, e);
    }
  }

}
