/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.spring.keystore;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Peter Nagy
 */

@Getter
@Setter
public class KeystoreEntry implements Comparable<KeystoreEntry>
{
    public enum EntryType
    {
        PRIVATE_KEY_ENTRY,
        SECRET_KEY_ENTRY,
        TRUSTED_CERTIFICATE_ENTRY
    }

    private String alias;
    private String password;
    private boolean inUse;
    private EntryType type;
    private List<CertInfo> chain = new ArrayList<>();

    public boolean isEntryForCoputer(String computerName)
    {
        if (this.chain == null) return false;
        if (this.type != EntryType.PRIVATE_KEY_ENTRY) return false;

        for (CertInfo info : chain)
        {
            if (info.isValid())
            {
                String subjectCN = info.getSubjectCN();
                if (subjectCN.equalsIgnoreCase(computerName)) return true;
                String[] names = subjectCN.split("\\.");
                if (names.length > 0)
                {
                    for (String name : names)
                    {
                        if (name.equalsIgnoreCase(computerName)) return true;
                    }
                }
            }
        }
        return false;
    }


    public boolean isValid()
    {
        for (CertInfo certInfo : this.chain)
        {
            if (!certInfo.isValid()) return false;
        }
        return true;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof KeystoreEntry))
        {
            return false;
        }
        KeystoreEntry that = (KeystoreEntry) o;
        return Objects.equals(alias, that.alias);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(alias);
    }


    @Override
    public int compareTo(KeystoreEntry o)
    {
        if (this.alias == null) return 1;
        if (o == null || o.alias == null) return -1;

        return this.alias.compareTo(o.alias);
    }

    @Override
    public String toString()
    {
        return "KeystoreEntry{" +
                "alias='" + alias + '\'' +
                ", password='" + password + '\'' +
                ", inUse=" + inUse +
                ", type=" + type +
                ", chain=" + chain +
                '}';
    }
}
