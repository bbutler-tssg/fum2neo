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
package org.tssg.solas.bb.fum2neo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.tssg.solas.bb.fum2neo.domain.AppEventSummary;
import org.tssg.solas.bb.fum2neo.domain.IAppEventNode;
import org.tssg.solas.bb.fum2neo.domain.MultiDescriptor;
import org.tssg.solas.bb.fum2neo.domain.NodeShared;
import org.tssg.solas.bb.fum2neo.domain.shared.CormelRelationshipShared;
import org.tssg.solas.bb.fum2neo.domain.shared.CormelTNode;
import org.tssg.solas.bb.fum2neo.domain.shared.EntityOperation;
import org.tssg.solas.bb.fum2neo.domain.shared.Field;
import org.tssg.solas.bb.fum2neo.domain.shared.NodeOpSummary;
import org.tssg.solas.bb.fum2neo.domain.shared.NodeType;
import org.tssg.solas.bb.fum2neo.domain.shared.RelType;

import com.amadeus.fum.types.Doc;
import com.amadeus.fum.types.Hits;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author bbutler
 *
 */
public class UploadExtract {

//  private static Logger log = LoggerFactory.getLogger(UploadExtract.class);
  private static final String defaultConnectionString = "bolt://localhost:7687";

  private static final String defaultBase = "goodBadForSample20";
  
  private static final File defaultInputFile = new File("input/"+defaultBase+".json");
  private static final File defaultOutCsvFile = new File("output/"+defaultBase+".csv");
  private static final File defaultAvroOutFile = new File("output/"+defaultBase+".gz.avro");
  private static final int default_batchSize = 2500;
  private static final boolean default_addErrors = false;
  private static final boolean default_addWarnings = false;

//  private static final String projectName = "fum2neo";

  @Parameter(names = {"-i", "--input"}, description = "name of FUM extract JSON file to process", required=true)
  private File jsonInputFile = defaultInputFile;

  @Parameter(names = {"-o", "--output"}, description = "name of FUM extract important fields csv")
  private File csvOutputFile = defaultOutCsvFile;
  
  @Parameter(names = {"-b", "--batchSize"}, description = "insert transaction size")
  private int batchSize = default_batchSize;
  
  @Parameter(names = {"-e", "--errors"}, description = "add error nodes to graph")
  private boolean addErrors = default_addErrors;
  
  @Parameter(names = {"-w", "--warnings"}, description = "add warning nodes to graph")
  private boolean addWarnings = default_addWarnings;

  @Parameter(names = {"-a", "--avroFile"}, description = "Avro file used to store AppEvent summaries", required=true)
  private File avroOutFile = defaultAvroOutFile;
  
  @Parameter(names = {"-f", "--function"}, description = "Function to be applied: UpdateNeo, WriteAvro or both", validateWith = ValidFunctionArgument.class, required=true)
  private List<String> functions  = new ArrayList<>(); 

  @Parameter(names = {"-h", "--help"}, help = true)
  private boolean help;
  
  private Map<NodeType, RelType> lookupRelTypeFrom = new HashMap<>();

  public UploadExtract() {
    lookupRelTypeFrom.put(NodeType.Error, RelType.HAS_ERROR);
    lookupRelTypeFrom.put(NodeType.Warning, RelType.HAS_WARNING);
  }

  /**
   * @return the jsonInputFile
   */
  public File getJsonInputFile() {
    return jsonInputFile;
  }

  /**
   * @return the csvOutputFile
   */
  public File getCsvOutputFile() {
    return csvOutputFile;
  }

  /**
   * @return the batchSize
   */
  public int getBatchSize() {
    return batchSize;
  }

  /**
   * @return the addErrors
   */
  public boolean isAddErrors() {
    return addErrors;
  }

  /**
   * @return the addWarnings
   */
  public boolean isAddWarnings() {
    return addWarnings;
  }

  /**
   * @return the avroOutFile
   */
  public File getAvroOutFile() {
    return avroOutFile;
  }

  /**
   * @return the functions
   */
  public List<String> getFunctions() {
    return functions;
  }

  private void progressReport(PrintStream outputStream, String s) {
    // See https://stackoverflow.com/a/20677345
    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HH:mm:ss"));
    outputStream.println(now+" "+s);
  }

  private SortedMap<TransactionSourceField, String> parseSourceFields(LinkedHashMap<String, Object> sourceMap) {
    SortedMap<TransactionSourceField, String> sourceFields = new TreeMap<TransactionSourceField, String>();
    for (TransactionSourceField field : TransactionSourceField.values()) {
      String fieldStr = field.toString();
      sourceFields.put(field,Optional.ofNullable((String) sourceMap.get(fieldStr)).orElse(""));
    }
    return sourceFields;
  }

  private ErrorsWarningsBean deriveErrorsWarnings(LinkedHashMap<String, Object> sourceMap, SortedSet<String> emptySet) {
    SortedSet<String> errors = null;
    SortedSet<String> warnings = null;
    if (null != sourceMap.get("TransactionErrors")) {
      @SuppressWarnings("unchecked")
      Map<String, Object> teMap = (Map<String, Object>) sourceMap.get("TransactionErrors");
      @SuppressWarnings("unchecked")
      List<LinkedHashMap<String, String>> errorList = (List<LinkedHashMap<String, String>>) teMap.get("Errors");
      @SuppressWarnings("unchecked")
      List<LinkedHashMap<String, String>> warningList = (List<LinkedHashMap<String, String>>) teMap.get("Warnings");
      // System.out.println(teMap);
      if (null != errorList) {
        errors = new TreeSet<>();
        for (LinkedHashMap<String, String> errorEntry : errorList) {
          errors.add(errorEntry.get("Error").replace(",","_"));
        }
      } else {
        errors = emptySet;
      }
      if (null != warningList) {
        warnings = new TreeSet<>();
        for (LinkedHashMap<String, String> warningEntry : warningList) {
          warnings.add(warningEntry.get("Warning").replace(",","_"));
        }
      } else {
        warnings = emptySet;
      }
    } else {
      errors = emptySet;
      warnings = emptySet;
    }
    ErrorsWarningsBean errorsWarnings = new ErrorsWarningsBean(errors, warnings);
    return errorsWarnings;
  }
  
  private void addErrorWarningNodes(String fromKey, SortedSet<String> extraSet, NodeType nodeType, Transaction tx, Map<String,Object> treeKeyParams) {
    String pivot = " - ";
    if (!extraSet.isEmpty()) {
      Map<String, Object> params = new HashMap<>();
      for (String extra: extraSet) {
        IAppEventNode errWarn = new MultiDescriptor(nodeType, tx);
        if (extra.contains(pivot)) {
          String[] parts = extra.split(pivot);
          int np = parts.length;
          params.put(Field.AppEvent_M_Code, parts[0].trim());
          if (np == 2) {
            params.put(Field.AppEvent_M_Description, parts[1].trim());
          } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < np; i++) {
              sb.append(parts[i].trim());
              if (i < (np-2)) {
                sb.append(pivot);
              }
            }
            params.put(Field.AppEvent_M_Description, sb.toString());
          }
        }
        NodeOpSummary ews = errWarn.addNode(params);
        NodeType fromNodeType = NodeType.Tsegment;
        NodeType toNodeType = nodeType;
        String toKey = ews.getKey();
        CormelRelationshipShared.createRel(tx, fromNodeType, fromKey, toNodeType, toKey, lookupRelTypeFrom.get(toNodeType), treeKeyParams);
      }
    }
  }
  
  private NodeOpSummary updateTransactionNode(Session session, TransactionBean trans, Transaction tx) {
    String prefix = trans.getSourceFields().get(TransactionSourceField.Prefix);
    NodeOpSummary nos;
    Map<String,Object> params = trans.parsePrefix();
    if (params.isEmpty()) {
      // Exceptional case: DcxId not supplied - just return a dummy NodeOpSummary
      Map<String,Object> properties = new HashMap<>();
      String key = "";
      Long id = 0L;
      int opCount = 0;
      progressReport(System.err, "ERROR: AppEvent (" + prefix + ") updated " + opCount + " Transaction nodes");
      nos = new NodeOpSummary(NodeType.Tsegment, properties, key, id, EntityOperation.UPDATE, opCount);
      return nos;
    }
    String key = CormelTNode.deriveKey(params);
    Map<String, Object> treeKeyParams = CormelTNode.deriveTreeKeyParams(tx, params);
    params.clear();
    params.put(Field.AppEvent_T_Status, trans.getSourceFields().get(TransactionSourceField.TransactionStatus));
    params.put(Field.Node_AppEventFile, trans.getSourceFields().get(TransactionSourceField.TransactionStatus));

    CormelTNode tNode = new CormelTNode(tx);
    nos = tNode.updateNode(params, key);
    int opCount = nos.getOpCount();

    if (opCount == 1) {
      Long tNodeId = nos.getId();
      progressReport(System.out, "Success: AppEvent (" + prefix + ") updated Transaction node with id "+tNodeId);
      ErrorsWarningsBean errorsWarnings = trans.getErrorsWarnings();
      SortedSet<String> errors = errorsWarnings.getErrors();
      addErrorWarningNodes(key, errors, NodeType.Error, tx, treeKeyParams);
      SortedSet<String> warnings = errorsWarnings.getWarnings();
      addErrorWarningNodes(key, warnings, NodeType.Warning, tx, treeKeyParams);
    } else {
      progressReport(System.out, "WARNING: AppEvent (" + prefix + ") updated " + opCount + " (1 expected!) Transaction nodes");
    }

    return nos;
  }

  private List<LinkedHashMap<String,Object>> drillToInnerHitsdMap(Doc document) {
    Map<String, Object> docAdditionalProperties = document.getAdditionalProperties();
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Hits> outerHitsMap = (LinkedHashMap<String, Hits>) docAdditionalProperties.get("hits");
    @SuppressWarnings("unchecked")
    List<LinkedHashMap<String,Object>> listInnerHitsMap = (List<LinkedHashMap<String,Object>>) outerHitsMap.get("hits");
    return listInnerHitsMap;
  }
  
  private TransactionBean deriveTransactionBean(LinkedHashMap<String,Object> innerHitsMap, SortedSet<String> emptySet) {
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> sourceMap = (LinkedHashMap<String, Object>) innerHitsMap.get("_source");
    String type = (String) innerHitsMap.get("_type");
    SortedMap<TransactionSourceField, String> sourceFields = parseSourceFields(sourceMap);
    ErrorsWarningsBean errorsWarnings = deriveErrorsWarnings(sourceMap, emptySet);
    TransactionBean trans = new TransactionBean(type, sourceFields, errorsWarnings);
    return trans;
  }
  
  private int deriveTreeId(String trxNb) {
    String sep = "-";
    int treeId;
    if (trxNb.contains(sep)) {
      String[] parts = trxNb.split("-");
      treeId = Integer.parseInt(parts[0]);
    } else {
      treeId = Integer.parseInt(trxNb);
    }
    return treeId;
  }

  private AppEventSummary deriveAppEventSummaryFrom(TransactionBean trans) {
    AppEventSummary aeSummary = new AppEventSummary();
    String trxnsactionStatus = trans.getSourceFields().get(TransactionSourceField.TransactionStatus);

    String dcxId;
    String trxNb;
    Map<String,Object> params = trans.parsePrefix();
    if (!params.isEmpty()) {
      dcxId = String.valueOf(params.get(Field.T_DcxId));
      trxNb = String.valueOf(params.get(Field.T_TrxNb));
    } else {
      dcxId = trans.getSourceFields().get(TransactionSourceField.DcxID);
      trxNb = "0";
    }
    int treeId = deriveTreeId(trxNb);
    aeSummary.setDcxId(dcxId);
    aeSummary.setTrxNb(trxNb);
    aeSummary.setTreeId(treeId);
    aeSummary.setStatus(trxnsactionStatus);
    return aeSummary;
  }
  
  private void writeSummaryToAvro(File avroOutFile, PrintStream csvLog, Doc document) {
    List<LinkedHashMap<String,Object>> listInnerHitsMap = drillToInnerHitsdMap(document);
    SortedSet<String> emptySet = new TreeSet<String>();
    
    DatumWriter<AppEventSummary> aesDatumWriter = new SpecificDatumWriter<AppEventSummary>(AppEventSummary.class);
    try (DataFileWriter<AppEventSummary> dataFileWriter = new DataFileWriter<AppEventSummary>(aesDatumWriter)) {
      // See https://stackoverflow.com/a/27214227/1988855
      // Use deflate with compression level set to 6 (max is 9); default is no
      // compression. Other compression options exist.
      dataFileWriter.setCodec(CodecFactory.deflateCodec(6));

      int recordCnt = 0;
      for (LinkedHashMap<String, Object> innerHitsMap : listInnerHitsMap) {
        TransactionBean trans = deriveTransactionBean(innerHitsMap, emptySet);
        AppEventSummary aeSummary = deriveAppEventSummaryFrom(trans);
        recordCnt++;
        try {
          if (recordCnt == 1) {
            dataFileWriter.create(aeSummary.getSchema(), avroOutFile);
          }
          dataFileWriter.append(aeSummary);
        } catch (IOException e) {
          progressReport(System.err, "IOException either when opening or appending data to "+avroOutFile);
          e.printStackTrace();
        }
        progressReport(csvLog, trans.toString());
      }

      progressReport(System.out, "Written final transaction.");
    } catch (IOException e1) {
      progressReport(System.err, "IOException when closing "+avroOutFile);
      e1.printStackTrace();
    }
  }

  private void updateNeo(Session session, PrintStream csvLog, Doc document) {
    int txCount = 0;
    long start;
    long elapsed;
    int cnt = 0;
    SortedSet<String> emptySet = new TreeSet<String>();
    List<LinkedHashMap<String,Object>> listInnerHitsMap = drillToInnerHitsdMap(document);

    progressReport(System.out, "Create a new transaction for this session");
    Transaction tx = session.beginTransaction();

    for (LinkedHashMap<String,Object> innerHitsMap : listInnerHitsMap) {

      TransactionBean trans = deriveTransactionBean(innerHitsMap, emptySet);
      
      updateTransactionNode(session, trans, tx);

      if (cnt % batchSize == (batchSize - 1)) {
        progressReport(System.out, batchSize
            + " FUM records processed, about to commit and close the current transaction and start a new one");
        start = System.nanoTime();
        tx.success(); // Commit before closing!!
        tx.close();
        tx = session.beginTransaction();
        elapsed = System.nanoTime() - start;
        txCount++;
        progressReport(System.out, "Committed and closed transaction "+(txCount-1)+" and started transaction "+txCount+" in " + TimeUnit.NANOSECONDS.toMillis(elapsed)
            + " milliseconds.");
      }

      cnt++;

      progressReport(csvLog, trans.toString());
    }

    tx.success(); // Commit before closing!!
    tx.close();
    progressReport(System.out, "Committed and closed final transaction "+txCount+" in this session.");

  }
  
  public void readParseAndSave() {
//    File jsonInputFile = new File(System.getProperty("user.home")+"/"+projectName+"/"+jsonInputFileStr);
//    File csvOutputFile = new File(System.getProperty("user.home")+"/"+projectName+"/"+csvOutputFileStr);
    final PrintStream csvOutStream;

//    String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
//    Path path = Paths.get(System.getProperty("user.home")+"/"+projectName+"/output/timings/"+now+".txt"); // Java 8 - see http://winterbe.com/posts/2015/03/25/java8-examples-string-number-math-files/

    ObjectMapper mapper = new ObjectMapper();
    
    Doc document = null;
    try {
      document = mapper.readValue(jsonInputFile, Doc.class);
    } catch (JsonParseException e) {
      progressReport(System.err, "JsonParseException when reading "+jsonInputFile);
      e.printStackTrace();
    } catch (JsonMappingException e) {
      progressReport(System.err, "JsonMappingException when reading "+jsonInputFile);
      e.printStackTrace();
    } catch (IOException e) {
      progressReport(System.err, "IOException when reading "+jsonInputFile);
      e.printStackTrace();
    };

    StringBuilder fieldList = new StringBuilder();
    fieldList.append("Type");
    for (TransactionSourceField field : TransactionSourceField.values()) {
      String fieldStr = field.toString();
      fieldList.append(",");
      fieldList.append(fieldStr);
    }
    String[] extras = {"Errors", "Warnings"};
    for  (String fieldStr : extras) {
      fieldList.append(",");
      fieldList.append(fieldStr);
    }

    try {
      csvOutStream = new PrintStream(csvOutputFile);
      progressReport(csvOutStream, fieldList.toString());

      if (functions.contains(String.valueOf(FunctionOperation.UpdateNeo))) {
        try (Driver driver = GraphDatabase.driver(defaultConnectionString)) {
          try (Session session = driver.session()) {
            for (NodeType nodeType : NodeType.values()) {
              NodeShared.applyConstraint(session, nodeType);
            }
            updateNeo(session, csvOutStream, document);
          }
        }
      }
      
      if (functions.contains(String.valueOf(FunctionOperation.WriteAvro))) {
        writeSummaryToAvro(avroOutFile, csvOutStream, document);
      }      

    } catch (FileNotFoundException e1) {
      progressReport(System.err, "FileNotFoundException when switching sysout to " + csvOutputFile);
      e1.printStackTrace();
    }
    
    progressReport(System.out, "Completed...");
    
  }
  
  /**
   * @param args
   */
  public static void main(String ... argv) {
    UploadExtract ue = new UploadExtract();
    JCommander.newBuilder()
    .addObject(ue)
    .build()
    .parse(argv);

    ue.readParseAndSave();
  }

}
