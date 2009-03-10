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
package org.apache.pluto.core;

import org.apache.pluto.container.NamespaceMapper;
import org.apache.pluto.container.OptionalContainerServices;
import org.apache.pluto.container.PortletAppDescriptorService;
import org.apache.pluto.container.PortletEnvironmentService;
import org.apache.pluto.container.PortletInfoService;
import org.apache.pluto.container.PortletInvokerService;
import org.apache.pluto.container.PortletPreferencesService;
import org.apache.pluto.container.PortletRequestContextService;
import org.apache.pluto.container.UserInfoService;
import org.apache.pluto.container.driver.PortalAdministrationService;
import org.apache.pluto.container.driver.PortalDriverServices;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.descriptors.services.jaxb.PortletAppDescriptorServiceImpl;

/**
 * Default Optional Container Services and Portal Driver Services implementation.
 *
 * @version 1.0
 * @since Sep 18, 2004
 */
public class DefaultOptionalContainerServices implements OptionalContainerServices, PortalDriverServices {

    private PortletPreferencesService portletPreferencesService;
    private PortletRegistryService portletRegistryService;
    private PortletContextService portletContextService;
    private PortletInvokerService portletInvokerService;
    private PortletRequestContextService portletRequestContextService;
    private PortletEnvironmentService portletEnvironmentService;
    private PortletInfoService portletInfoService;
    private PortalAdministrationService portalAdministrationService;
    private UserInfoService userInfoService;
    private NamespaceMapper namespaceMapper;
    private PortletAppDescriptorService descriptorService;

    /**
     * Constructs an instance using the default portlet preferences service
     * implementation.
     */
    public DefaultOptionalContainerServices() {
        portletPreferencesService = new DefaultPortletPreferencesService();
        portletRegistryService = new PortletContextManager();
        portletContextService = (PortletContextManager)portletRegistryService;
        portletInvokerService = new DefaultPortletInvokerService(portletContextService);
        portletRequestContextService = new DefaultPortletRequestContextService();
        portletEnvironmentService = new DefaultPortletEnvironmentService();
        portletInfoService = new DefaultPortletInfoService();
        portalAdministrationService = new DefaultPortalAdministrationService();
        userInfoService = new DefaultUserInfoService();
        namespaceMapper = new DefaultNamespaceMapper();
        descriptorService = new PortletAppDescriptorServiceImpl();                        
    }

    /**
     * Constructs an instance using specified optional container services
     * implementation. If the portlet preferences service is provided, it will
     * be used. Otherwise, the default portlet preferences service will be used.
     * @param root  the root optional container services implementation.
     */
    public DefaultOptionalContainerServices(OptionalContainerServices root, PortalDriverServices driverServices) {
        this();
        if (root.getPortletPreferencesService() != null) {
            portletPreferencesService = root.getPortletPreferencesService();
        }

        if (driverServices.getPortletRegistryService() != null) {
            portletRegistryService = driverServices.getPortletRegistryService();
        }

        if (driverServices.getPortletContextService() != null) {
            portletContextService = driverServices.getPortletContextService();
        }

        if(root.getPortletEnvironmentService() != null) {
            portletEnvironmentService = root.getPortletEnvironmentService();
        }

        if(root.getPortletInvokerService() != null) {
            portletInvokerService = root.getPortletInvokerService();
        }

        if(root.getPortletRequestContextService() != null) {
            portletRequestContextService = root.getPortletRequestContextService();
        }

        if(root.getPortletEnvironmentService() != null) {
            portletEnvironmentService = root.getPortletEnvironmentService();
        }

        if(root.getPortletInfoService() != null) {
            portletInfoService = root.getPortletInfoService();
        }

        if(driverServices.getPortalAdministrationService() != null) {
            portalAdministrationService = driverServices.getPortalAdministrationService();
        }

		if(root.getUserInfoService() != null) {
            userInfoService = root.getUserInfoService();
		}
		
        if(root.getNamespaceMapper() != null) {
            namespaceMapper = root.getNamespaceMapper();
        }
        
        if (descriptorService == null) 
        {
            descriptorService = new PortletAppDescriptorServiceImpl();
        }
    }


    // OptionalContainerServices Impl ------------------------------------------

    public PortletPreferencesService getPortletPreferencesService() {
        return portletPreferencesService;
    }


    public PortletRegistryService getPortletRegistryService() {
        return portletRegistryService;
    }

    public PortletContextService getPortletContextService() {
        return portletContextService;
    }

    public PortletRequestContextService getPortletRequestContextService() {
        return portletRequestContextService;
    }

    public PortletEnvironmentService getPortletEnvironmentService() {
        return portletEnvironmentService;
    }

    public PortletInvokerService getPortletInvokerService() {
        return portletInvokerService;
    }

    public PortletInfoService getPortletInfoService() {
        return portletInfoService;
    }

    public PortalAdministrationService getPortalAdministrationService() {
        return portalAdministrationService;
    }

    public UserInfoService getUserInfoService() {
        return userInfoService;
    }
    
    public NamespaceMapper getNamespaceMapper() {
        return namespaceMapper;
    }

    public PortletAppDescriptorService getDescriptorService()
    {
        return this.descriptorService;
    }
}

