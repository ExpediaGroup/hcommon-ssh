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
package com.hotels.hcommon.ssh.tunnel;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pastdev.jsch.SessionFactory;
import com.pastdev.jsch.SessionFactory.SessionFactoryBuilder;
import com.pastdev.jsch.tunnel.Tunnel;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;

import com.hotels.hcommon.ssh.SshSettings;
import com.hotels.hcommon.ssh.session.SessionFactorySupplier;
import com.hotels.hcommon.ssh.tunnel.DefaultTunnelConnectionManagerFactory;

@RunWith(MockitoJUnitRunner.class)
public class DefaultTunnelConnectionManagerFactoryTest {

  public @Mock SshSettings sshSettings;
  public @Mock SessionFactorySupplier sessionFactorySupplier;
  public @Mock SessionFactory sessionFactory;
  public @Mock SessionFactoryBuilder sessionFactoryBuilder;

  private DefaultTunnelConnectionManagerFactory tunnelConnectionManagerFactory;

  @Before
  public void init() {
    when(sessionFactorySupplier.get()).thenReturn(sessionFactory);
    when(sessionFactory.newSessionFactoryBuilder()).thenReturn(sessionFactoryBuilder);
    tunnelConnectionManagerFactory = new DefaultTunnelConnectionManagerFactory(sshSettings, sessionFactorySupplier);
  }

  @Test
  public void remoteDetailsOnly() {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create("hotels.com", 5678);
    Tunnel tunnel = tunnelConnectionManager.getTunnel("hotels.com", 5678);
    assertThat(tunnel.getAssignedLocalPort() > 0, is(true));
    assertThat(tunnel.getLocalAlias(), is("localhost"));
    assertThat(tunnel.getDestinationHostname(), is("hotels.com"));
    assertThat(tunnel.getDestinationPort(), is(5678));
  }

  @Test
  public void remoteDetailsWithHops() {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create("hotels.com", 5678);
    Tunnel tunnel = tunnelConnectionManager.getTunnel("hotels.com", 5678);
    assertThat(tunnel.getAssignedLocalPort() > 0, is(true));
    assertThat(tunnel.getLocalAlias(), is("localhost"));
    assertThat(tunnel.getDestinationHostname(), is("hotels.com"));
    assertThat(tunnel.getDestinationPort(), is(5678));
  }

  @Test
  public void localAndRemoteDetails() {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create("my-host", 0, "hotels.com",
        5678);
    Tunnel tunnel = tunnelConnectionManager.getTunnel("hotels.com", 5678);
    assertThat(tunnel.getAssignedLocalPort() > 0, is(true));
    assertThat(tunnel.getLocalAlias(), is("my-host"));
    assertThat(tunnel.getDestinationHostname(), is("hotels.com"));
    assertThat(tunnel.getDestinationPort(), is(5678));
  }

  @Test
  public void fullSpec() {
    TunnelConnectionManager tunnelConnectionManager = tunnelConnectionManagerFactory.create("my-host", 1234,
        "hotels.com", 5678);
    Tunnel tunnel = tunnelConnectionManager.getTunnel("hotels.com", 5678);
    assertThat(tunnel.getSpec(), is("my-host:1234:hotels.com:5678"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullLocalHost() {
    tunnelConnectionManagerFactory.create(null, 22, "target", 5678);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidLocalHost() {
    tunnelConnectionManagerFactory.create(" ", 22, "target", 5678);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidLocalPort() {
    tunnelConnectionManagerFactory.create("host", -1, "target", 5678);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRemoteHost() {
    tunnelConnectionManagerFactory.create("host", 22, null, 22);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidRemoteHost() {
    tunnelConnectionManagerFactory.create("host", 22, " ", 22);
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidRemotePort() {
    tunnelConnectionManagerFactory.create("host", 1234, "target", -1);
  }

}
