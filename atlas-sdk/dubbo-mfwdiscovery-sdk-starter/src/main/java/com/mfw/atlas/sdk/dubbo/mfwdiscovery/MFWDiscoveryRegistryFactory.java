/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mfw.atlas.sdk.dubbo.mfwdiscovery;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.support.AbstractRegistryFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.mfw.atlas.client.udp.PushEventDispatcher;
import com.mfw.atlas.client.udp.PushReceiver;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry.MFWDiscoveryNamingServiceUtils;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry.MFWDiscoveryRegistry;
import com.mfw.atlas.sdk.dubbo.mfwdiscovery.registry.MFWDiscoveryNamingService;


/**
 * MFW discovery registry factory {@link com.alibaba.dubbo.registry.RegistryFactory}
 *
 * @author fenghua
 */
public class MFWDiscoveryRegistryFactory extends AbstractRegistryFactory {
    private static volatile NamingService namingService;

    @Override
    protected Registry createRegistry(URL url) {
        if(namingService == null) {
            synchronized (getClass()) {
                if(namingService == null) {
                    namingService = MFWDiscoveryNamingServiceUtils.createNamingService(url);

                    // udp event listener
                    PushEventDispatcher dispatcher = new PushEventDispatcher();
                    dispatcher.addListener((MFWDiscoveryNamingService) namingService);
                    PushReceiver receiver = new PushReceiver();
                    receiver.setPushEventDispatcher(dispatcher);
                }
            }
        }

        return new MFWDiscoveryRegistry(url, namingService);
    }
}
