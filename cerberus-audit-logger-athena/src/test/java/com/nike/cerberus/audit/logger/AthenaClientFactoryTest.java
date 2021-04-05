package com.nike.cerberus.audit.logger;

import com.amazonaws.services.athena.AmazonAthena;
import org.junit.Assert;
import org.junit.Test;

public class AthenaClientFactoryTest {

  @Test
  public void testGetClientAlwaysReturnsSameAthenaInstance() {
    AthenaClientFactory athenaClientFactory = new AthenaClientFactory();
    AmazonAthena clientInstance1 = athenaClientFactory.getClient("region-2");
    AmazonAthena clientInstance2 = athenaClientFactory.getClient("region-2");
    Assert.assertSame(clientInstance1, clientInstance2);
  }
}
