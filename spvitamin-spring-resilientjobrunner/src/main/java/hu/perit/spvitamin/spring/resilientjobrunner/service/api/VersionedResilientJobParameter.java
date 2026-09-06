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

package hu.perit.spvitamin.spring.resilientjobrunner.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import hu.perit.spvitamin.json.JSonSerializer;
import hu.perit.spvitamin.json.SpvitaminObjectMapper;
import lombok.SneakyThrows;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Improved base class for resilient job parameters that stores the schema version in the JSON
 * and supports version-based migration.
 *
 * <p>Unlike {@link ResilientJobParameter}, this class embeds the version number in the serialized
 * JSON, so deserialization does not require the caller to supply the version separately.</p>
 *
 * <p>Migration strategy (versions start from 1):
 * <ul>
 *   <li>If the JSON has no "version" field it is treated as a legacy {@link ResilientJobParameter}
 *       payload; the target class must override {@link #getLegacyClass()} and
 *       {@link #migrateFromLegacy(ResilientJobParameter)}.</li>
 *   <li>If the stored version is older than the current version, the class returned by
 *       {@link #getPreviousVersionClass()} is used to recursively migrate upwards.</li>
 * </ul>
 * </p>
 *
 * <p>Subclasses must provide a public no-arg constructor so that both Jackson and
 * {@link #fromJson} can instantiate them for version inspection.</p>
 *
 * @author Peter Nagy
 */
public abstract class VersionedResilientJobParameter
{
    public static final String VERSION = "schema_version";


    public String toJson()
    {
        return JSonSerializer.toJson(this);
    }


    /**
     * Returns the schema version of this parameter class. Implementations must return a
     * compile-time constant (e.g. {@code return 1;}). The value is serialized into the JSON
     * under the key {@code "schema_version"} and is used during deserialization to drive migration.
     * Versions start from 1.
     */
    @JsonProperty(value = VERSION, access = JsonProperty.Access.READ_ONLY)
    public abstract int getVersion();


    /**
     * Deserializes a {@code VersionedResilientJobParameter} from JSON, migrating through
     * intermediate versions as necessary.
     *
     * <ul>
     *   <li>If the JSON contains no {@code "schema_version"} field, legacy migration via
     *       {@link #getLegacyClass()} / {@link #migrateFromLegacy} is attempted.</li>
     *   <li>If the stored version equals {@code targetClass.getVersion()}, the JSON is
     *       deserialized directly.</li>
     *   <li>If the stored version is older, the parameter is recursively deserialized as
     *       {@link #getPreviousVersionClass()} and then migrated up via
     *       {@link #migrateFrom(VersionedResilientJobParameter)}.</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T extends VersionedResilientJobParameter> T fromJson(String json, Class<T> targetClass)
    {
        JsonMapper mapper = SpvitaminObjectMapper.getJsonMapper();
        JsonNode node = mapper.readTree(json);

        T instance = targetClass.getDeclaredConstructor().newInstance();

        if (!node.has(VERSION))
        {
            // No version field: treat as a legacy ResilientJobParameter payload
            Class<? extends ResilientJobParameter> legacyClass = instance.getLegacyClass();
            if (legacyClass == null)
            {
                throw new IllegalStateException(String.format(
                        "JSON has no 'version' field and %s does not define a legacy class for migration",
                        targetClass.getSimpleName()));
            }
            ResilientJobParameter legacy = JSonSerializer.fromJson(json, legacyClass);
            return (T) instance.migrateFromLegacy(legacy);
        }

        int jsonVersion = node.get(VERSION).asInt();
        int currentVersion = instance.getVersion();

        if (jsonVersion == currentVersion)
        {
            return JSonSerializer.fromJson(json, targetClass);
        }

        if (jsonVersion > currentVersion)
        {
            throw new IllegalArgumentException(String.format(
                    "Cannot downgrade %s from version %d to %d",
                    targetClass.getSimpleName(), jsonVersion, currentVersion));
        }

        // Stored version is older than current: migrate upward recursively
        Class<? extends VersionedResilientJobParameter> previousClass = instance.getPreviousVersionClass();
        if (previousClass == null)
        {
            throw new IllegalStateException(String.format(
                    "Missing migration path in %s for stored version %d (current: %d)",
                    targetClass.getSimpleName(), jsonVersion, currentVersion));
        }

        VersionedResilientJobParameter previousVersionObject = fromJson(json, previousClass);
        return (T) instance.migrateFrom(previousVersionObject);
    }


    /**
     * Returns the class representing the immediately preceding schema version, used to
     * drive recursive upward migration. Return {@code null} for version 1 (no prior versioned
     * class exists; legacy migration is handled separately via {@link #getLegacyClass()}).
     */
    protected abstract Class<? extends VersionedResilientJobParameter> getPreviousVersionClass();


    /**
     * Migrates a {@code previous} version instance to the current version. The argument is
     * guaranteed to be an instance of the class returned by {@link #getPreviousVersionClass()}.
     */
    protected abstract VersionedResilientJobParameter migrateFrom(VersionedResilientJobParameter previous);


    /**
     * Returns the legacy {@link ResilientJobParameter} subclass that this class can be migrated
     * from when the JSON has no {@code "schema_version"} field. Override and return the appropriate class
     * for version-1 classes that replace an existing {@link ResilientJobParameter} subclass.
     * The default implementation returns {@code null} (no legacy migration).
     */
    protected Class<? extends ResilientJobParameter> getLegacyClass()
    {
        return null;
    }


    /**
     * Migrates a {@code legacy} {@link ResilientJobParameter} instance to this version.
     * Override together with {@link #getLegacyClass()}. The default implementation throws
     * {@link UnsupportedOperationException}.
     */
    protected VersionedResilientJobParameter migrateFromLegacy(ResilientJobParameter legacy)
    {
        throw new UnsupportedOperationException(
                "Legacy migration from ResilientJobParameter not supported in " + getClass().getSimpleName());
    }
}
