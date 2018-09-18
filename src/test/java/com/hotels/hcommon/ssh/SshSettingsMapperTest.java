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

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;

public class SshSettingsMapperTest {

  private final SshSettingsMapper settings = new SshSettingsMapper();
  private final String knownHosts = "knownHosts";
  private final String privateKey = "privateKey";
  private final String route = "hostA -> hostB";
  private final int timeout = 123;

  private static Validator validator;

  @Before
  public void before() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();

    settings.setKnownHosts(knownHosts);
    settings.setPrivateKeys(privateKey);
    settings.setRoute(route);
    settings.setTimeout(timeout);
  }

  @Test
  public void typical() {
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
  }

  @Test
  public void parametersAreAssignedCorrectly() {
    assertThat(settings.getKnownHosts(), is(knownHosts));
    assertThat(settings.getPrivateKeys(), is(privateKey));
    assertThat(settings.getRoute(), is(route));
    assertThat(settings.getTimeout(), is(timeout));
  }

  @Test
  public void infiniteTimeout() {
    settings.setTimeout(0);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
  }

  @Test
  public void portTooHigh() {
    settings.setPort(65536);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);

    assertThat(violations.size(), is(1));
  }

  @Test
  public void portTooLow() {
    settings.setPort(0);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void nullRoute() {
    settings.setRoute(null);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void emptyRoute() {
    settings.setRoute("");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void blankRoute() {
    settings.setRoute(" ");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void nullKnownHosts() {
    settings.setKnownHosts(null);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void emptyKnownHosts() {
    settings.setKnownHosts(" ");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void blankKnownHosts() {
    settings.setKnownHosts(" ");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void nullPrivateKey() {
    settings.setPrivateKeys(null);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void emptyPrivateKey() {
    settings.setPrivateKeys("");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void blankPrivateKey() {
    settings.setPrivateKeys(" ");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void multiplePrivateKeys() {
    settings.setPrivateKeys("privatekey1,privatekey2");
    assertThat(settings.getPrivateKeys(), is("privatekey1,privatekey2"));
  }

  @Test
  public void multiplePrivateKeysWithSpace() {
    settings.setPrivateKeys("privatekey1, privatekey2");
    assertThat(settings.getPrivateKeys(), is("privatekey1,privatekey2"));
  }

  @Test
  public void negativeTimeout() {
    settings.setTimeout(-1);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void timeoutTooHigh() {
    settings.setTimeout(Integer.MAX_VALUE + 1);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void strictHostKeyCheckingSetToYes() {
    settings.setStrictHostKeyChecking("yes");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
  }

  @Test
  public void strictHostKeyCheckingSetToNo() {
    settings.setStrictHostKeyChecking("no");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
  }

  @Test
  public void strictHostKeyCheckingSetToIncorrectValue() {
    settings.setStrictHostKeyChecking("foo");
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(1));
  }

  @Test
  public void strictHostKeyCheckingDefaultsToYes() {
    assertThat(settings.getStrictHostKeyChecking(), is("yes"));
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
  }

  @Test
  public void getLocalHost() {
    String localHost = "localHost";
    settings.setLocalHost(localHost);
    Set<ConstraintViolation<SshSettingsMapper>> violations = validator.validate(settings);
    assertThat(violations.size(), is(0));
    assertThat(settings.getLocalHost(), is(localHost));
  }
}
