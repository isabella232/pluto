/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.internal;

import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.PortletWindow;

/**
 * The internal portlet response interface extends PortletResponse and adds
 * some methods used by Pluto.
 *
 */
public interface InternalPortletResponse extends PortletResponse {

    public PortletWindow getPortletWindow();
    

    /**
     * Is set true when a jsp, servlet is included.
     * @param included true when included
     */
    public void setIncluded(boolean included);
    
    /**
     * Returns true if a jsp or servlet is included.
     * @return true if a jsp or servlet is included.
     */
    public boolean isIncluded();
    
    public void setForwarded(boolean forwared);
    
    public boolean isForwarded();
    
    public boolean isForwardedAllowed();
    
    public boolean isRequestForwarded();
    
    public void setRequestForwarded();

    public HttpServletResponse getHttpServletResponse();
    
}