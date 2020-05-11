/**
 * Copyright © 2018 Jeremy Custenborder (jcustenborder@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jcustenborder.netty.syslog;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.graylog2.syslog4j.SyslogIF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class SyslogIT {
  protected abstract int port();

  protected abstract SyslogIF syslogIF();

  protected abstract ChannelFuture setupServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, TestSyslogMessageHandler handler) throws InterruptedException;

  private EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
  private EventLoopGroup workerGroup = new NioEventLoopGroup(4);
  protected ChannelFuture channelFuture;
  protected TestSyslogMessageHandler handler;

  @BeforeEach
  public void setup() throws InterruptedException {
    this.bossGroup = new NioEventLoopGroup();
    this.workerGroup = new NioEventLoopGroup();
    this.handler = new TestSyslogMessageHandler();
    this.channelFuture = setupServer(this.bossGroup, this.workerGroup, this.handler);
    Thread.sleep(500);
  }

  @Test
  public void roundtrip() throws InterruptedException {
    final int count = 100;
    SyslogIF syslogIF = syslogIF();
    for (int i = 0; i < count; i++) {
      syslogIF.info("{\"node\":\"172.168.1.2\",\"node_id\":\"EXEBWSA>HJ+LLJFS\",\"unit_id\":\"\",\"unitname\":\"\",\"computername\":\"WIN-182\",\"username\":\"张三\", \"departmentid\":\"95141\",\"department\":\"IT support\",\"program\":\"d:\\projects\\zushen\\svn\\windows\\trunk\\bin\\windows\\Debug\\client.hook\\hooktest.exe\",\"facility\":\"c2s\",\"object\":\"10.10.1.47\", \"details\":\"账户名称;zhangsan;密码强度;没有密码;密码长度;0;自动登录;否;”,\"result\":\"1\",\"entrystamp\":\"1525221667262\", \"level\":\"6\",\"type\":\"process_connect\", \"producttype\":\"h\",\"behaviourtype\":\"4\",\"reservation\":\"IPv4，TCP\" ,\"gid\"：\"全局统一id\",\"op\":{\"src\":\"\",\"dest\":\"80\"}，\"raw\":\"{\\\"foo\\\":\\\"bar\\\"}\" }");
    }
    syslogIF.flush();

    final long start = System.currentTimeMillis();
    while ((System.currentTimeMillis() - start) < 5000 && this.handler.messages.size() < count) {
      Thread.sleep(100);
    }
    assertEquals(count, this.handler.messages.size());
  }


  @AfterEach
  public void close() throws InterruptedException {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    this.channelFuture.channel().closeFuture().sync();
  }

}
