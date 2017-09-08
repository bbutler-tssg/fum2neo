/*
Copyright (c) 2017, Bernard Butler (Waterford Institute of Technology, Ireland), Project: SOLAS placement in Amadeus SA, where SOLAS (Project ID: 612480) is funded by the European Commision FP7 MC-IAPP-Industry-Academia Partnerships and Pathways scheme.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 -  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 -  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 -  Neither the name of WATERFORD INSTITUTE OF TECHNOLOGY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
/**
 * 
 */
package org.tssg.solas.bb.fum2neo.domain;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tssg.solas.bb.fum2neo.domain.shared.Field;
import org.tssg.solas.bb.fum2neo.domain.shared.NodeOpSummary;
import org.tssg.solas.bb.fum2neo.domain.shared.NodeType;

/**
 * @author bbutler
 *
 */
public class MultiDescriptor implements IAppEventNode {

  Logger log = LoggerFactory.getLogger(MultiDescriptor.class);
  private static final String defaultConnectionString = "bolt://localhost:7687";
  private NodeType nodeType;
  private Map<String, Object> params = new HashMap<>();
  private Transaction tx;
  private NodeShared cormelNode;
  
  public MultiDescriptor(NodeType nodeType, Transaction tx) {
    this.tx = tx;
    this.nodeType = nodeType;
    this.cormelNode = new NodeShared(nodeType, tx);
  }

  /* (non-Javadoc)
   * @see org.tssg.solas.bb.fum2neo.domain.IAppEventNode#getCormelNode()
   */
  @Override
  public NodeShared getCormelNode() {
    return cormelNode;
  }

  /* (non-Javadoc)
   * @see org.tssg.solas.bb.fum2neo.domain.IAppEventNode#getParams()
   */
  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  public static String deriveKey(NodeType nodeType, Map<String, Object> params) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.valueOf(nodeType));
    sb.append("_");
    sb.append(String.valueOf(params.get(Field.AppEvent_M_Code)));
    sb.append("_");
    sb.append(String.valueOf(params.get(Field.AppEvent_M_Description)));
    return sb.toString();
  }

  /* (non-Javadoc)
   * @see org.tssg.solas.bb.fum2neo.domain.IAppEventNode#addNode(java.util.Map)
   */
  @Override
  public NodeOpSummary addNode(Map<String, Object> params) {
    this.params = params;
    String key = deriveKey(nodeType, params);
    StatementResult result = tx.run("MERGE (n:" + nodeType + " {" +
      Field.Node_AppEventFile + ": {" + Field.Node_AppEventFile + "}," +
      Field.AppEvent_M_Code + ": {" + Field.AppEvent_M_Code + "}," +
      Field.AppEvent_M_Description + ": {" + Field.AppEvent_M_Description + "}," +
      "nodeType: {nodeType}," +
      "key: {key}" +
      "}) RETURN ID(n) AS nodeId",
      parameters(
          Field.Node_AppEventFile, params.get(Field.Node_AppEventFile),
          Field.AppEvent_M_Code, params.get(Field.AppEvent_M_Code),
          Field.AppEvent_M_Description, params.get(Field.AppEvent_M_Description),
          "nodeType", String.valueOf(nodeType),
          "key", key
          )
      );
    NodeOpSummary ns = cormelNode.addNodeExtra(params, key, result);
    return ns;
  }

  /* (non-Javadoc)
   * @see org.tssg.solas.bb.fum2neo.domain.IAppEventNode#lookupNode(java.util.Map)
   */
  @Override
  public NodeOpSummary lookupNode(Map<String, Object> params) {
    String key = deriveKey(nodeType, params);
    NodeOpSummary ns = cormelNode.lookupNode(params, key);
    return ns;
  }

  /* (non-Javadoc)
   * @see org.tssg.solas.bb.fum2neo.domain.IAppEventNode#deleteNode(java.util.Map)
   */
  @Override
  public NodeOpSummary deleteNode(Map<String, Object> params) {
    String key = deriveKey(nodeType, params);
    NodeOpSummary ns = cormelNode.deleteNode(params, key);
    return ns;
  }

  public static void main(String[] args) {
    Map<String,Object> params = new HashMap<>();
    params.put(Field.Node_AppEventFile, "dataSource");
    params.put(Field.AppEvent_M_Code, "00");
    params.put(Field.AppEvent_M_Description, "00description");

    try (Driver driver = GraphDatabase.driver(defaultConnectionString)) {
    // try (Driver driver = GraphDatabase.driver(connectionString,
    // AuthTokens.basic("neo4j", "neo4j"))) {
      try (Session session = driver.session()) {
        Transaction tx = session.beginTransaction();
        IAppEventNode ex = new MultiDescriptor(NodeType.Error, tx);
        NodeShared cns = ex.getCormelNode();
        
        cns.testCRUD(params, ex);

        tx.close();
      }
    }
    
  }

}
