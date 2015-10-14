/*
 * Copyright (C) 2009-2014 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package upgradetest;

public class ServerStatusResponse {
  private final String id;
  private final String version;
  private final Status status;

  public ServerStatusResponse(String id, String version, Status status) {
    this.id = id;
    this.version = version;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public Status getStatus() {
    return status;
  }

  public enum Status {
    UP, DOWN, DB_MIGRATION_NEEDED, DB_MIGRATION_RUNNING
  }
}
