/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.impl.helper;

import org.jboss.aerogear.android.RecordId;

import java.util.Objects;

public class Data implements Comparable<Data> {

    @RecordId
    private Integer id;
    private String name;
    private String description;
    private boolean enable;

    public Data(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Data(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Data(Integer id, String name, String description, boolean enable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enable = enable;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Data other = (Data) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (this.enable != other.enable) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(Data data) {
        if (data == null) {
            return 1;
        }
        if (id == null) {
            if (data.id == null) {
                return 0;
            } else {
                return -1;
            }
        }
        return id.compareTo(data.id);
    }

    @Override
    public String toString() {
        return "Data{" + "id=" + id + ", name=" + name + ", description=" + description + ", enable=" + enable + '}';
    }

}
