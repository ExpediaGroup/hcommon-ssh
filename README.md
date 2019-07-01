# Hcommon-ssh

This library provides common functionality which allows network-connected components to route traffic through SSH tunnels.

## Start using

You can obtain this library from Maven Central:

[![Maven Central TGZ](https://maven-badges.herokuapp.com/maven-central/com.hotels/hcommon-ssh/badge.svg?subject=com.hotels:hcommon-ssh.jar)](https://maven-badges.herokuapp.com/maven-central/com.hotels/hcommon-ssh) [![Build Status](https://travis-ci.org/HotelsDotCom/hcommon-ssh.svg?branch=master)](https://travis-ci.org/HotelsDotCom/hcommon-ssh) [![Coverage Status](https://coveralls.io/repos/github/HotelsDotCom/hcommon-ssh/badge.svg?branch=master)](https://coveralls.io/github/HotelsDotCom/hcommon-ssh?branch=master) ![GitHub license](https://img.shields.io/github/license/HotelsDotCom/hcommon-ssh.svg)

## Overview

Components can be proxied to use a SSH tunnel. A `SshSettings` object provides the basic configuration required to establish the SSH connection and the SSH tunnel.

This library uses Java Proxies to handle the SSH tunneling logic so components must implement the marker interface `Tunnelable`.

An out-of-the-box factory is provided to handle the component wrapping: `TunnelableFactory`. The factory uses a `MethodChecker` to verify whether the method being invoked must ensure the SSH tunnel is open or whether the method must close the SSH tunnel. In order to proxy a component the `wrap` method of this factory class must be invoked passing the component to wrap, an instance of `MethodChecker` and the remote host and port to which the component will be connecting.

Example:

    // Mark the client as tunnelable
    public class MyClient implements Tunnelable, ... {
      ...
    }
    ...
    public class MyClientSupplier implements TunnelableSupplier<MyClient> {
      ...
    }
    ...
    MyClientSupplier myClientSupplier = new MyClientSupplier();
    ...
    SshSettings sshSettings = SshSettings
      .builder()
      .withRoute("hop1 -> hop2")
      .withKnownHosts("/home/user/.ssh/known_hosts")
      .withPrivateKeys("/home/user/.ssh/id_rsa_common")
      .build();
    TunnelableFactory factory = new TunnelableFactory(sshSettings);
    Tunnelable wrapped = factory.wrap(myClientSupplier, MethodCheck.DEFAULT, "my-remote-service", 8080);

### SSH tunnel syntax

The tunnel `route` expression is described with the following <a href="https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form">EBNF</a>:

    path = path part, {"->", path part}
    path part = {user, "@"}, hostname
    user = ? user name ?
    hostname = ? hostname ?

For example, if the remote service runs on the host `remote-server-box:1024` which can only be reached first via `bastion-host` and then `jump-box` then the SSH tunnel route expression in `SshSettings` will be `bastion-host -> jump-box` and host `remote-server-box` and port `1024` will be provided as method parameters in the factory. If `bastion-host` is only accessible by user `ec2-user` and `jump-box` by user `user-a` then the expression above becomes `ec2-user@bastion-host -> user-a@jump-box`.

Once the tunnel is established the library will set up port forwarding from the local machine specified to the remote machine. The last node in the tunnel expression doesn't need to be the target server, the only requirement is that this last node must be able to communicate with the _remote-server-box_. Sometimes this is not possible due to firewall restrictions so in these cases they must be the same.

All the machines in the tunnel expression can be included in the _known___hosts_ file and in this case the keys required to access each box should be set in the `SshSettings` `privateKkeys` property. For example, if `bastion-host` is authenticated with `bastion.pem` and both `jump-box` and `remote-server-box` are authenticated with `emr.pem` then the property must be set as`"<path-to-ssh-keys>/bastion.pem, <path-to-ssh-keys>/emr.pem"`.

If all machines in the tunnel expression are not included in the _known___hosts_ file then the `SshSettings` property `strictHostKeyChecking` should be set to no.

To add the fingerprint of `remote-box` in to the _known___hosts_ file the following command can be used:

    ssh-keyscan -t rsa remote-box >> .ssh/known_hosts

## Legal
This project is available under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).

Copyright 2019 Expedia, Inc.
