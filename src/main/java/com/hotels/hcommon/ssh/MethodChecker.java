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

import java.lang.reflect.Method;

/**
 * Implementations of this interface will check which method invocations must ensure the SSH tunnels is open and which
 * method invocations must close the SSH tunnel.
 */
public interface MethodChecker {

  public static final MethodChecker DEFAULT = new MethodChecker() {

    @Override
    public boolean isTunnelled(Method method) {
      return "connect".equals(method.getName())
          || "reconnect".equals(method.getName())
          || "open".equals(method.getName());
    }

    @Override
    public boolean isShutdown(Method method) {
      return "disconnect".equals(method.getName())
          || "close".equals(method.getName())
          || "shutdown".equals(method.getName());
    }

  };

  /**
   * Checks if the method requires the SSH tunnel to be open.
   * <p>
   * If the method returns <code>true</code> it will ensure the SSH tunnel is open before the actual method is invoked.
   * This may result being an expensive operation so its advised only us this during the connect and reconnect phases.
   * </p>
   * 
   * @param method Object method to check.
   * @return <code>true</code> if the SSH tunnel is required by the invocation, <code>false</code> otherwise.
   */
  boolean isTunnelled(Method method);

  /**
   * Checks if the method is shutting down the tunnel connection in some way and thus requires the SSH tunnel to be
   * closed.
   *
   * @param method Object method to check.
   * @return <code>true</code> if the SSH tunnel must be closed after the invocation, <code>false</code> otherwise.
   */
  boolean isShutdown(Method method);

}
