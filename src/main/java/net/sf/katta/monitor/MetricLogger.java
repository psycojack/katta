/**
 * Copyright 2008 the original author or authors.
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
package net.sf.katta.monitor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.katta.protocol.ConnectedComponent;
import net.sf.katta.protocol.IAddRemoveListener;
import net.sf.katta.protocol.InteractionProtocol;
import net.sf.katta.util.ZkConfiguration;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.log4j.Logger;

public class MetricLogger implements IZkDataListener, ConnectedComponent {

  public enum OutputType {
    Log4J, SystemOut;
  }

  private final static Logger LOG = Logger.getLogger(MetricLogger.class);

  private OutputType _outputType;
  private ReentrantLock _lock;

  protected final InteractionProtocol _protocol;
  private long _loggedRecords = 0;

  /**
   * @deprecated use
   *             {@link MetricLogger#MetricLogger(OutputType, InteractionProtocol)}
   *             instead
   */
  public MetricLogger(OutputType outputType, ZkClient zkClient, ZkConfiguration zkConf) {
    this(outputType, new InteractionProtocol(zkClient, zkConf));
  }

  public MetricLogger(OutputType outputType, InteractionProtocol protocol) {
    _protocol = protocol;
    _outputType = outputType;
    _protocol.registerComponent(this);
    List<String> children = _protocol.registerMetricsNodeListener(this, new IAddRemoveListener() {
      @Override
      public void removed(String name) {
        unsubscribeDataUpdates(name);
      }

      @Override
      public void added(String name) {
        MetricsRecord metric = _protocol.getMetric(name);
        logMetric(metric);
        subscribeDataUpdates(name);
      }
    });
    for (String node : children) {
      subscribeDataUpdates(node);
    }
    _lock = new ReentrantLock();
    _lock.lock();
  }

  protected void subscribeDataUpdates(String nodeName) {
    _protocol.registerMetricsDataListener(this, nodeName, this);
  }

  protected void unsubscribeDataUpdates(String nodeName) {
    _protocol.unregisterMetricsDataListener(this, nodeName, this);
  }

  @Override
  public void handleDataChange(String dataPath, Serializable data) throws Exception {
    MetricsRecord metrics = (MetricsRecord) data;
    logMetric(metrics);
  }

  protected void logMetric(MetricsRecord metrics) {
    switch (_outputType) {
    case Log4J:
      LOG.info(metrics);
      break;
    case SystemOut:
      System.out.println(metrics.toString());
      break;
    default:
      throw new IllegalStateException("output type " + _outputType + " not supported");
    }
    _loggedRecords++;
  }

  @Override
  public void handleDataDeleted(String dataPath) throws Exception {
    // nothing todo
  }

  public void join() throws InterruptedException {
    synchronized (_lock) {
      _lock.wait();
    }
  }

  public long getLoggedRecords() {
    return _loggedRecords;
  }

  @Override
  public void disconnect() {
    _lock.unlock();
  }

  @Override
  public void reconnect() {
    // nothing to do
  }

}
