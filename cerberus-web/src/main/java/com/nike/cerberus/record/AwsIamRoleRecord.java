/*
 * Copyright (c) 2020 Nike, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nike.cerberus.record;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

/** POJO for representing a AWS_IAM_ROLE record. */
@Data
@Builder
public class AwsIamRoleRecord {

  private String id;

  private String createdBy;

  private String lastUpdatedBy;

  private OffsetDateTime createdTs;

  private OffsetDateTime lastUpdatedTs;

  private String awsIamRoleArn;
}
