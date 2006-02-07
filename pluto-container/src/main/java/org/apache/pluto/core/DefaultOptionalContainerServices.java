/*
 * Copyright 2004 The Apache Software Foundation.
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
package org.apache.pluto.core;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.services.optional.PortletPreferencesService;
import org.apache.pluto.services.optional.PortletEnvironmentService;
import org.apache.pluto.services.optional.PortletInvokerService;

/**
 * Default Optional Container Services implementation.
 *
 * @author <a href="mailto:ddewolf@apache.org">David H. DeWolf</a>
 * @author <a href="mailto:zheng@apache.org">ZHENG Zhong</a>
 * @version 1.0
 * @since Sep 18, 2004
 */
public class DefaultOptionalContainerServices
implements OptionalContainerServices {
	
	// Private Member Variables ------------------------------------------------
	
    /** The portlet preferences service implementation. */
    private PortletPreferencesService preferenceService = null;
    
    
    // Constructors ------------------------------------------------------------
    
    /**
     * Constructs an instance using the default portlet preferences service
     * implementation.
     */
    public DefaultOptionalContainerServices() {
        preferenceService = new DefaultPortletPreferencesService();
    }
    
    /**
     * Constructs an instance using specified optional container services
     * implementation. If the portlet preferences service is provided, it will
     * be used. Otherwise, the default portlet preferences service will be used.
     * @param root  the root optional container services implementation.
     */
    public DefaultOptionalContainerServices(OptionalContainerServices root) {
        if (root.getPortletPreferencesService() != null) {
            preferenceService = root.getPortletPreferencesService();
        } else {
        	preferenceService = new DefaultPortletPreferencesService();
        }
    }
    
    
    // OptionalContainerServices Impl ------------------------------------------
    
    public PortletPreferencesService getPortletPreferencesService() {
        return preferenceService;
    }
    
    /**
     * TODO:
     */
    public PortletEnvironmentService getPortletEnvironmentService() {
        return null;
    }
    
    /**
     * TODO:
     */
    public PortletInvokerService getPortletInvokerService(InternalPortletWindow window) {
        return null;
    }
    
}

