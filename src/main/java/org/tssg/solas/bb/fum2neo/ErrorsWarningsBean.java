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
import java.util.SortedSet;

/**
 * @author bbutler
 *
 */
public class ErrorsWarningsBean implements Serializable, Comparable<ErrorsWarningsBean> {

  /**
   * serialVersionUID is maintained manually
   */
  private static final long serialVersionUID = 1L;

  private SortedSet<String> errors;
  private SortedSet<String> warnings;
  
  public ErrorsWarningsBean(final SortedSet<String> errors, final SortedSet<String> warnings) {
    this.errors = errors;
    this.warnings = warnings;
  }
  
  
  /**
   * @return the errors
   */
  public SortedSet<String> getErrors() {
    return errors;
  }


  /**
   * @return the warnings
   */
  public SortedSet<String> getWarnings() {
    return warnings;
  }


  @Override
  public int compareTo(ErrorsWarningsBean arg0) {
    return this.toString().compareTo(arg0.toString());
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    boolean first;
    sb.append("Errors=[");
    first = true;
    for (String error : errors) {
      if (!first) {
        sb.append(";");
      } else {
        first = false;
      }
      sb.append("\"");
      sb.append(error);
      sb.append("\"");
    }
    sb.append("],Warnings=[");
    first = true;
    for (String warning : warnings) {
      if (!first) {
        sb.append(";");
      } else {
        first = false;
      }
      sb.append("\"");
      sb.append(warning);
      sb.append("\"");
    }
    sb.append("]");
    return sb.toString();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((errors == null) ? 0 : errors.hashCode());
    result = prime * result + ((warnings == null) ? 0 : warnings.hashCode());
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
    ErrorsWarningsBean other = (ErrorsWarningsBean) obj;
    if (errors == null) {
      if (other.errors != null)
        return false;
    } else if (!errors.equals(other.errors))
      return false;
    if (warnings == null) {
      if (other.warnings != null)
        return false;
    } else if (!warnings.equals(other.warnings))
      return false;
    return true;
  }


  /**
   * @param args
   */
  public static void main(String[] args) {
  }

}
