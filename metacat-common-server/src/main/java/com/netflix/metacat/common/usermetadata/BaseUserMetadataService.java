/*
 * Copyright 2016 Netflix, Inc.
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.netflix.metacat.common.usermetadata;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.metacat.common.dto.HasDataMetadata;
import com.netflix.metacat.common.dto.HasDefinitionMetadata;
import com.netflix.metacat.common.dto.HasMetadata;

import java.util.Optional;

/**
 * Created by amajumdar on 4/13/15.
 */
public abstract class BaseUserMetadataService implements UserMetadataService{
    public void saveMetadata(String userId, HasMetadata holder, boolean merge) {
        if (holder instanceof HasDefinitionMetadata) {
            HasDefinitionMetadata defDto = (HasDefinitionMetadata) holder;

            // If the user is updating the definition metadata do a merge on the existing metadata
            ObjectNode newMetadata = defDto.getDefinitionMetadata();
            if (newMetadata != null) {
                HasDefinitionMetadata definitionDto = (HasDefinitionMetadata) holder;
                saveDefinitionMetadata(definitionDto.getDefinitionName(), userId, Optional.of(newMetadata), merge);
            }
        }

        if (holder instanceof HasDataMetadata) {
            HasDataMetadata dataDto = (HasDataMetadata) holder;

            // If the user is updating the data metadata and a separate data location exists,
            // do a merge on the existing metadata
            ObjectNode newMetadata = dataDto.getDataMetadata();
            if (newMetadata != null && dataDto.isDataExternal()) {
                saveDataMetadata(dataDto.getDataUri(), userId, Optional.of(newMetadata), merge);
            }
        }
    }

    public void populateMetadata(HasMetadata holder) {
        Optional<ObjectNode> metadata = Optional.empty();
        if (holder instanceof HasDataMetadata) {
            HasDataMetadata dataDto = (HasDataMetadata) holder;
            if (dataDto.isDataExternal()) {
                metadata = getDataMetadata(dataDto.getDataUri());
            }
        }
        Optional<ObjectNode> definitionMetadata = Optional.empty();
        if (holder instanceof HasDefinitionMetadata) {
            HasDefinitionMetadata definitionDto = (HasDefinitionMetadata) holder;
            definitionMetadata = getDefinitionMetadata(definitionDto.getDefinitionName());
        }
        populateMetadata( holder, definitionMetadata.orElse(null), metadata.orElse(null));
    }

    public void populateMetadata(HasMetadata holder, ObjectNode definitionMetadata, ObjectNode dataMetadata) {
        if (holder instanceof HasDefinitionMetadata) {
            HasDefinitionMetadata defDto = (HasDefinitionMetadata) holder;
            defDto.setDefinitionMetadata(definitionMetadata);
        }

        if (holder instanceof HasDataMetadata) {
            HasDataMetadata dataDto = (HasDataMetadata) holder;
            if (dataDto.isDataExternal()) {
                dataDto.setDataMetadata(dataMetadata);
            }
        }
    }
}