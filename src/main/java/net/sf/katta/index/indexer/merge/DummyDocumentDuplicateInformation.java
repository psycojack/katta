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

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

class DummyDocumentDuplicateInformation implements IDocumentDuplicateInformation {
  public String[] getSupportedFieldNames() {
    return new String[]{"foo"};
  }

  public String getKey(Document document) {
    List list = document.getFields();
    return list.isEmpty() ? "foo" : ((Field) list.get(0)).stringValue();
  }

  public String getSortValue(Document document) {
    return getKey(document);
  }
}