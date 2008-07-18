/**
 * Copyright 2008 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.katta.index.indexer.merge;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobConf;
import net.sf.katta.util.Logger;

public class IndexMergeJob implements Configurable {

  private Configuration _configuration;

  public void merge(Path kattaIndices) throws Exception {

    Logger.info("collect all shards in this folder: " + kattaIndices);

    Path dedupPath = new Path("/tmp/katta.index.dedup", "" + System.currentTimeMillis());

    _configuration.setBoolean("mapred.map.tasks.speculative.execution", false);
    IndexToSequenceFileJob indexToSequenceFileJob = new IndexToSequenceFileJob();
    indexToSequenceFileJob.setConf(_configuration);
    indexToSequenceFileJob.indexToSequenceFile(kattaIndices, dedupPath);

    SequenceFileToIndexJob sequenceFileToIndexJob = new SequenceFileToIndexJob();
    sequenceFileToIndexJob.setConf(_configuration);
    sequenceFileToIndexJob.sequenceFileToIndex(dedupPath, kattaIndices);

    Logger.info("delete sequence file and extracted indices: " + dedupPath);
    FileSystem fileSystem = FileSystem.get(_configuration);
    fileSystem.delete(dedupPath);

    Logger.info("all shards in this folder are merged: " + kattaIndices);
    Logger.info("move these shards into a archive folder");
  }

  public void setConf(Configuration configuration) {
    _configuration = configuration;
  }

  public Configuration getConf() {
    return _configuration;
  }

  public static void main(String[] args) throws Exception {
    //TODO delete all merged katta indices
    Path kattaIndices = new Path(args[0]);
    IndexMergeJob job = new IndexMergeJob();
    JobConf jobConf = new JobConf();
    jobConf.setJarByClass(IndexMergeJob.class);
    job.setConf(jobConf);
    job.merge(kattaIndices);
  }
}