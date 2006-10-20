/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 */
/*
 * Copyright 2006 IBM Corporation.
 */
package javax.portlet;

/**
 * The <CODE>EventRequest</CODE> represents the request sent to the portlet
 * to handle an event.
 * It extends the PortletRequest interface to provide event request
 * information to portlets.<br>
 * The portlet container creates an <CODE>EventRequest</CODE> object and
 * passes it as argument to the portlet's <CODE>processEvent</CODE> method.
 * 
 * @see ActionRequest
 * @see PortletRequest
 * @since 2.0
 */
public interface EventRequest extends PortletRequest {

    /**
     * Returns the event that trigged the call to the processEvent method.
     * 
     * @return      the event that triggered the current processEvent call. 
     */
    
    public Event getEvent();
}
