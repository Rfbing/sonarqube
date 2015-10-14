/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package upgradetest;

import com.sonar.orchestrator.Orchestrator;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.sonar.wsclient.jsonsimple.JSONObject;
import org.sonar.wsclient.jsonsimple.parser.JSONParser;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Throwables.propagate;

public abstract class WsCallAndWait<RESPONSE> {
  private static final long TIMEOUT_5_MINUTES = 5L * 60 * 1000;
  private static final long DELAY_3_SECONDS = 3L * 1000;

  private final Orchestrator orchestrator;
  private final String targetRelativeUrl;
  private final long timeout;
  private final long delay;

  protected WsCallAndWait(Orchestrator orchestrator, String targetRelativeUrl, long timeout, long delay) {
    this.orchestrator = orchestrator;
    this.targetRelativeUrl = checkNotNull(targetRelativeUrl);
    this.timeout = timeout;
    this.delay = delay;
  }

  protected WsCallAndWait(Orchestrator orchestrator, String targetRelativeUrl) {
    this(orchestrator, targetRelativeUrl, TIMEOUT_5_MINUTES, DELAY_3_SECONDS);
  }

  @Nonnull
  public RESPONSE call() {
    String response = orchestrator.getServer().wsClient().post(targetRelativeUrl);
    JSONObject jsonObject = toJsonObject(response);
    try {
      return parse(jsonObject);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse JSON response", e);
    }
  }

  @CheckForNull
  public RESPONSE callAndWait() {
    long endAt = System.currentTimeMillis() + timeout;

    while (System.currentTimeMillis() < endAt) {
      RESPONSE response = call();
      if (shouldWait(response)) {
        sleepQuietly(delay);
      } else {
        return response;
      }
    }
    return null;
  }

  private void sleepQuietly(long rateInMs) {
    try {
      Thread.sleep(rateInMs);
    } catch (InterruptedException e) {
      propagate(e);
    }
  }

  private JSONObject toJsonObject(String s) {
    try {
      JSONParser parser = new JSONParser();
      Object o = parser.parse(s);
      if (o instanceof JSONObject) {
        return (JSONObject) o;
      }
      throw new RuntimeException("Can not parse response from server migration WS (not a JSON object)");
    } catch (Exception e) {
      throw new IllegalStateException("Invalid JSON: " + s, e);
    }
  }

  @Nonnull
  protected abstract RESPONSE parse(JSONObject jsonObject);

  protected abstract boolean shouldWait(RESPONSE response);
}
