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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;

import com.hotels.hcommon.ssh.SshSettings;

public class SshSettingsTest {

  private static final String KNOWN_HOSTS = "knownHosts";
  private static final String IDENTITY_KEY_1 = "K1";
  private static final String IDENTITY_KEY_2 = "K2";

  public @Rule ExpectedException expectedEx = ExpectedException.none();
  public @Rule TemporaryFolder tmpFolder = new TemporaryFolder();

  private File knownHosts;
  private File identityKey1;
  private File identityKey2;

  @Before
  public void init() throws Exception {
    knownHosts = tmpFolder.newFile(KNOWN_HOSTS);

    JSch jSch = new JSch();
    KeyPair keyPair = null;

    identityKey1 = tmpFolder.newFile(IDENTITY_KEY_1);
    keyPair = KeyPair.genKeyPair(jSch, KeyPair.RSA);
    keyPair.writePrivateKey(identityKey1.getAbsolutePath());

    identityKey2 = tmpFolder.newFile(IDENTITY_KEY_2);
    keyPair = KeyPair.genKeyPair(jSch, KeyPair.RSA);
    keyPair.writePrivateKey(identityKey2.getAbsolutePath());
  }

  @Test
  public void typical() {
    SshSettings sshSettings = SshSettings
        .builder()
        .withRoute("a -> b")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .build();
    assertThat(sshSettings.getSshPort(), is(SshSettings.DEFAULT_SSH_PORT));
    assertThat(sshSettings.getSessionTimeout(), is(SshSettings.DEFAULT_SESSION_TIMEOUT));
    assertThat(sshSettings.isStrictHostKeyChecking(), is(SshSettings.DEFAULT_STRICT_HOST_KEY_CHECKING));
    assertThat(sshSettings.getRoute(), is("a -> b"));
    assertThat(sshSettings.getKnownHosts(), is(knownHosts.getAbsolutePath()));
    assertThat(sshSettings.getPrivateKeys(),
        is(Arrays.asList(identityKey1.getAbsolutePath(), identityKey2.getAbsolutePath())));
  }

  @Test
  public void nonDefaultValues() {
    SshSettings sshSettings = SshSettings
        .builder()
        .withSshPort(23)
        .withSessionTimeout(1050)
        .withRoute("h1 -> h2")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .withStrictHostKeyChecking(false)
        .build();
    assertThat(sshSettings.getSshPort(), is(23));
    assertThat(sshSettings.getSessionTimeout(), is(1050));
    assertThat(sshSettings.isStrictHostKeyChecking(), is(false));
    assertThat(sshSettings.getRoute(), is("h1 -> h2"));
    assertThat(sshSettings.getKnownHosts(), is(knownHosts.getAbsolutePath()));
    assertThat(sshSettings.getPrivateKeys(),
        is(Arrays.asList(identityKey1.getAbsolutePath(), identityKey2.getAbsolutePath())));
  }

  public void invalidSshPort() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Invalid SSH port number: 0");
    SshSettings
        .builder()
        .withSshPort(0)
        .withRoute("h1 -> h2")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .build();
  }

  @Test
  public void invalidSessionTimeout() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Invalid SSH session timeout: -1");
    SshSettings
        .builder()
        .withSessionTimeout(-1)
        .withRoute("h1 -> h2")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .build();
  }

  @Test
  public void invalidRoute() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Invalid SSH tunnel route: '@ -> 1'");
    SshSettings
        .builder()
        .withRoute("@ -> 1")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .build();
  }

  @Test
  public void invalidKnownHosts() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Invalid SSH known hosts: ' '");
    SshSettings
        .builder()
        .withRoute("h1 -> h2")
        .withKnownHosts(" ")
        .withPrivateKeys(identityKey1.getAbsolutePath() + "," + identityKey2.getAbsolutePath())
        .build();
  }

  @Test
  public void invalidPrivateKeys() {
    expectedEx.expect(IllegalArgumentException.class);
    expectedEx.expectMessage("Invalid SSH private keys: ' '");
    SshSettings
        .builder()
        .withRoute("h1 -> h2")
        .withKnownHosts(knownHosts.getAbsolutePath())
        .withPrivateKeys(" ")
        .build();
  }

}
