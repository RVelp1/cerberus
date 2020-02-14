/*
 * Copyright (c) 2019 Nike, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.qos.logback.core.rolling;

import com.nike.cerberus.audit.logger.service.S3LogUploaderService;
import com.nike.internal.util.StringUtils;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Rolling policy that will copy audit logs to S3 if enabled when the logs roll. */
@Slf4j
@Component
public class AuditLogsS3TimeBasedRollingPolicy<E> extends TimeBasedRollingPolicy<E> {

  private final String bucket;
  private final String bucketRegion;
  private LinkedBlockingQueue<String> logChunkFileS3Queue = new LinkedBlockingQueue<>();
  private S3LogUploaderService s3LogUploaderService = null;

  @Autowired
  public AuditLogsS3TimeBasedRollingPolicy(
      @Value("${cerberus.audit.athena.bucket}") String bucket,
      @Value("${cerberus.audit.athena.bucketRegion}") String bucketRegion,
      S3LogUploaderService s3LogUploaderService) {
    this.bucket = bucket;
    this.bucketRegion = bucketRegion;

    setS3LogUploaderService(s3LogUploaderService);
    log.info("AuditLogsS3TimeBasedRollingPolicy initialized");
  }

  @Autowired
  public void setS3LogUploaderService(S3LogUploaderService s3LogUploaderService) {
    this.s3LogUploaderService = s3LogUploaderService;
    log.info("Setting up S3 Uploader Service in policy");
    if (logChunkFileS3Queue.size() > 0) {
      Stream.generate(() -> logChunkFileS3Queue.poll())
          .forEach(
              filename -> {
                s3LogUploaderService.ingestLog(filename);
                log.info("Log ingested in policy for: " + filename);
              });
    }
  }

  private boolean isS3AuditLogCopyingEnabled() {
    log.info("isS3AuditLogCopyingEnabled hit");
    return StringUtils.isNotBlank(bucket) && StringUtils.isNotBlank(bucketRegion);
  }

  @Override
  public void rollover() throws RolloverFailure {
    super.rollover();

    log.info("Policy rollover hit");
    if (isS3AuditLogCopyingEnabled()) {
      String filename = timeBasedFileNamingAndTriggeringPolicy.getElapsedPeriodsFileName() + ".gz";
      if (s3LogUploaderService != null) {
        s3LogUploaderService.ingestLog(filename);
        log.info("Log ingested in policy rollover for: " + filename);
      } else {
        logChunkFileS3Queue.offer(filename);
      }
    }
  }
}
