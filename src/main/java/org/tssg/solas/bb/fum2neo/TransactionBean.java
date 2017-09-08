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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.tssg.solas.bb.fum2neo.domain.shared.Field;

/**
 * @author bbutler
 *
 */
public class TransactionBean implements Serializable, Comparable<TransactionBean> {

  /**
   * serialVersionUID is maintained manually
   */
  private static final long serialVersionUID = 1L;
  
  private String type;
  private SortedMap<TransactionSourceField, String> sourceFields;
  private ErrorsWarningsBean errorsWarnings;
  
  public TransactionBean(final String type, final SortedMap<TransactionSourceField, String> sourceFields,
      final ErrorsWarningsBean errorsWarnings) {
    this.type = type;
    this.sourceFields = sourceFields;
    this.errorsWarnings = errorsWarnings;
  }

  public Map<String,Object> parsePrefix() {
    Map<String, Object> params = new HashMap<>();
    String prefix = sourceFields.get(TransactionSourceField.Prefix);
    String[] parts = prefix.split(":");
    if (parts.length == 3) {
      // normal case
      params.put(Field.T_DcxId, parts[1]);
      params.put(Field.T_TrxNb, parts[2]);
    } else if (null != sourceFields.get(TransactionSourceField.DcxID)) {
      // Exceptional case: DcxID supplied but TrxNb cannot be derived from the Prefix.
      // Give TrxNb a dummy value of 0, for consistency with cormel-tree-master
      params.put(Field.T_DcxId, sourceFields.get(TransactionSourceField.DcxID));
      params.put(Field.T_TrxNb, String.valueOf(0));
    }
    return params; 
  }
  
  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return the sourceFields
   */
  public SortedMap<TransactionSourceField, String> getSourceFields() {
    return sourceFields;
  }

  /**
   * @return the errorsWarnings
   */
  public ErrorsWarningsBean getErrorsWarnings() {
    return errorsWarnings;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("type=");
    sb.append(type);
    for (TransactionSourceField key : sourceFields.keySet()) {
      sb.append(",");
      sb.append(key.toString());
      sb.append("=");
      sb.append(sourceFields.get(key));
    }
    sb.append(",");
    sb.append(errorsWarnings.toString());
    return sb.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((errorsWarnings == null) ? 0 : errorsWarnings.hashCode());
    result = prime * result + ((sourceFields == null) ? 0 : sourceFields.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TransactionBean other = (TransactionBean) obj;
    if (errorsWarnings == null) {
      if (other.errorsWarnings != null)
        return false;
    } else if (!errorsWarnings.equals(other.errorsWarnings))
      return false;
    if (sourceFields == null) {
      if (other.sourceFields != null)
        return false;
    } else if (!sourceFields.equals(other.sourceFields))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public int compareTo(TransactionBean arg0) {
    return this.toString().compareTo(arg0.toString());
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
  }

}
