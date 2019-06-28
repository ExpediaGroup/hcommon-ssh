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
package com.hotels.hcommon.ssh;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pastdev.jsch.tunnel.Tunnel;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;

import com.hotels.hcommon.ssh.tunnel.TunnelConnectionManagerFactory;

@RunWith(MockitoJUnitRunner.class)
public class TunnelableFactoryTest {

  private static final int SSH_PORT = 22;
  private static final int LOCAL_PORT = 10240;
  private static final String LOCAL_HOST = "local";
  private static final int REMOTE_PORT = 1024;
  private static final String REMOTE_HOST = "remote";

  private static interface Connectable extends Tunnelable {
    public void method();

    public void methodThrowingException() throws TunneledException;
  }

  private static class Tunnelled implements Connectable {
    @Override
    public void method() {}

    @Override
    public void methodThrowingException() throws TunneledException {
      throw new TunneledException();
    }
  }

  private static class TunneledException extends Exception {
    private static final long serialVersionUID = 1L;
  }

  private @Mock SshSettings sshSettings;
  private @Mock TunnelConnectionManager tunnelConnectionManager;
  private @Mock Tunnel tunnel;
  private @Mock TunnelConnectionManagerFactory tunnelConnectionManagerFactory;
  private @Mock MethodChecker methodChecker;
  private @Mock TunnelableSupplier<Tunnelled> tunnelableSupplier;

  private TunnelableFactory<Tunnelled> tunnelableFactory;
  private final Tunnelled tunnelled = new Tunnelled();

  @Before
  public void init() {
    when(tunnel.getAssignedLocalPort()).thenReturn(SSH_PORT);
    when(tunnelConnectionManager.getTunnel(anyString(), anyInt())).thenReturn(tunnel);
    when(tunnelConnectionManagerFactory.create(LOCAL_HOST, LOCAL_PORT, REMOTE_HOST, REMOTE_PORT))
        .thenReturn(tunnelConnectionManager);
    when(tunnelConnectionManagerFactory.getSshSettings()).thenReturn(sshSettings);
    when(tunnelableSupplier.get()).thenReturn(tunnelled);
    tunnelableFactory = new TunnelableFactory<>(tunnelConnectionManagerFactory);
  }

  @Test
  public void openTunnelUponCreation() throws Exception {
    tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
    verify(tunnelConnectionManager).open();
  }

  @Test
  public void ensureTunnelIsOpen() throws Exception {
    when(methodChecker.isTunnelled(any(Method.class))).thenReturn(true);
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    proxy.method();
    verify(tunnelConnectionManager).ensureOpen();
    verify(tunnelConnectionManager, never()).close();
  }

  @Test
  public void ensureTunnelIsOpenProxyThrowsException() throws Exception {
    when(methodChecker.isTunnelled(any(Method.class))).thenReturn(true);
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    try {
      proxy.methodThrowingException();
      fail("Should have thrown TunneledException");
    } catch (TunneledException e) {
      verify(tunnelConnectionManager).ensureOpen();
      verify(tunnelConnectionManager, never()).close();
    }
  }

  @Test
  public void closeTunnel() throws Exception {
    when(methodChecker.isShutdown(any(Method.class))).thenReturn(true);
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    proxy.method();
    verify(tunnelConnectionManager, never()).ensureOpen();
    verify(tunnelConnectionManager).close();
  }

  @Test
  public void closeTunnelThrowsException() throws Exception {
    when(methodChecker.isShutdown(any(Method.class))).thenReturn(true);
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    try {
      proxy.methodThrowingException();
      fail("Should have thrown TunneledException");
    } catch (TunneledException e) {
      verify(tunnelConnectionManager, never()).ensureOpen();
      verify(tunnelConnectionManager).close();
    }
  }

  @Test
  public void noTunnelConnectivityInteractions() throws Exception {
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    proxy.method();
    verify(tunnelConnectionManager, never()).ensureOpen();
    verify(tunnelConnectionManager, never()).close();
  }

  @Test(expected = TunneledException.class)
  public void invokeThrowsUnderlyingTargetException() throws Throwable {
    Connectable proxy = (Connectable) tunnelableFactory.wrap(tunnelableSupplier, methodChecker, LOCAL_HOST, LOCAL_PORT,
        REMOTE_HOST, REMOTE_PORT);
    proxy.methodThrowingException();
  }

}
