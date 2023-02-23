/*
 * Copyright (C) 2019 the original author or authors.
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
package com.mfw.atlas.provider.event;

import com.mfw.atlas.provider.constant.InstanceChangeEnum;
import org.springframework.context.ApplicationEvent;

/**
 * @author KL
 * @Time 2020/10/22 1:57 下午
 */
public class ServiceChangeEvent extends ApplicationEvent {

    private InstanceChangeEnum type;
    public long freshTime;
    public Object data;

    public ServiceChangeEvent(Object source, InstanceChangeEnum type, Object data) {
        super(source);
        this.type = type;
        this.freshTime = System.currentTimeMillis();
        this.data = data;
    }

    public ServiceChangeEvent(Object source, InstanceChangeEnum type, long freshTime, Object data) {
        super(source);
        this.type = type;
        this.freshTime = freshTime;
        this.data = data;
    }

    public InstanceChangeEnum getType() {
        return type;
    }

    public void setType(InstanceChangeEnum type) {
        this.type = type;
    }

    public long getFreshTime() {
        return freshTime;
    }

    public void setFreshTime(long freshTime) {
        this.freshTime = freshTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}



