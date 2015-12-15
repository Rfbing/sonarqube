/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.permission.ws;

import java.util.Set;
import org.sonar.api.i18n.I18n;
import org.sonar.api.resources.ResourceTypes;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewParam;
import org.sonar.core.permission.GlobalPermissions;
import org.sonar.core.permission.ProjectPermissions;
import org.sonar.core.util.Uuids;
import org.sonar.server.user.UserSession;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Ordering.natural;
import static java.lang.String.format;
import static org.sonar.server.component.ResourceTypeFunctions.RESOURCE_TYPE_TO_QUALIFIER;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_DESCRIPTION;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_GROUP_ID;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_GROUP_NAME;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_ID;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_PERMISSION;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_PROJECT_ID;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_PROJECT_KEY;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_PROJECT_KEY_PATTERN;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_QUALIFIER;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_TEMPLATE_ID;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_TEMPLATE_NAME;
import static org.sonarqube.ws.client.permission.PermissionsWsParameters.PARAM_USER_LOGIN;

public class PermissionsWsParametersBuilder {

  private static final String PERMISSION_PARAM_DESCRIPTION = format("Permission" +
    "<ul>" +
    "<li>Possible values for global permissions: %s</li>" +
    "<li>Possible values for project permissions %s</li>" +
    "</ul>",
    GlobalPermissions.ALL_ON_ONE_LINE,
    ProjectPermissions.ALL_ON_ONE_LINE);
  private static final String PROJECT_PERMISSION_PARAM_DESCRIPTION = format("Permission" +
    "<ul>" +
    "<li>Possible values for project permissions %s</li>" +
    "</ul>",
    ProjectPermissions.ALL_ON_ONE_LINE);

  private PermissionsWsParametersBuilder() {
    // static methods only
  }

  public static void createPermissionParameter(NewAction action) {
    action.createParam(PARAM_PERMISSION)
      .setDescription(PERMISSION_PARAM_DESCRIPTION)
      .setRequired(true);
  }

  public static void createProjectPermissionParameter(NewAction action) {
    action.createParam(PARAM_PERMISSION)
      .setDescription(PROJECT_PERMISSION_PARAM_DESCRIPTION)
      .setRequired(true);
  }

  public static void createGroupNameParameter(NewAction action) {
    action.createParam(PARAM_GROUP_NAME)
      .setDescription("Group name or 'anyone' (case insensitive)")
      .setExampleValue("sonar-administrators");
  }

  public static void createGroupIdParameter(NewAction action) {
    action.createParam(PARAM_GROUP_ID)
      .setDescription("Group id")
      .setExampleValue("42");
  }

  public static void createProjectParameter(NewAction action) {
    createProjectIdParameter(action);
    createProjectKeyParameter(action);
  }

  private static void createProjectIdParameter(NewAction action) {
    action.createParam(PARAM_PROJECT_ID)
      .setDescription("Project id")
      .setExampleValue("ce4c03d6-430f-40a9-b777-ad877c00aa4d");
  }

  private static void createProjectKeyParameter(NewAction action) {
    action.createParam(PARAM_PROJECT_KEY)
      .setDescription("Project key")
      .setExampleValue("org.apache.hbas:hbase");
  }

  public static void createUserLoginParameter(NewAction action) {
    action.createParam(PARAM_USER_LOGIN)
      .setRequired(true)
      .setDescription("User login")
      .setExampleValue("g.hopper");
  }

  public static void createTemplateParameters(NewAction action) {
    createTemplateIdParameter(action);
    createTemplateNameParameter(action);
  }

  private static void createTemplateIdParameter(NewAction action) {
    action.createParam(PARAM_TEMPLATE_ID)
      .setDescription("Template id")
      .setExampleValue(Uuids.UUID_EXAMPLE_01);
  }

  private static void createTemplateNameParameter(NewAction action) {
    action.createParam(PARAM_TEMPLATE_NAME)
      .setDescription("Template name")
      .setExampleValue("Default Permission Template for Projects");
  }

  public static void createTemplateProjectKeyPatternParameter(NewAction action) {
    action.createParam(PARAM_PROJECT_KEY_PATTERN)
      .setDescription("Project key pattern. Must be a valid Java regular expression")
      .setExampleValue(".*\\.finance\\..*");
  }

  public static void createTemplateDescriptionParameter(NewAction action) {
    action.createParam(PARAM_DESCRIPTION)
      .setDescription("Description")
      .setExampleValue("Permissions for all projects related to the financial service");
  }

  public static void createIdParameter(NewAction action) {
    action.createParam(PARAM_ID)
      .setRequired(true)
      .setDescription("Id")
      .setExampleValue("af8cb8cc-1e78-4c4e-8c00-ee8e814009a5");
  }

  public static NewParam createQualifierParameter(NewAction action, QualifierParameterContext context) {
    return action.createParam(PARAM_QUALIFIER)
      .setDescription("Project qualifier. Filter the results with the specified qualifier. Possible values are:" + buildRootQualifiersDescription(context))
      .setPossibleValues(getRootQualifiers(context.getResourceTypes()));
  }

  private static Set<String> getRootQualifiers(ResourceTypes resourceTypes) {
    return from(resourceTypes.getRoots())
      .transform(RESOURCE_TYPE_TO_QUALIFIER)
      .toSortedSet(natural());
  }

  private static String buildRootQualifiersDescription(QualifierParameterContext context) {
    StringBuilder description = new StringBuilder();
    description.append("<ul>");
    String qualifierPattern = "<li>%s - %s</li>";
    for (String qualifier : getRootQualifiers(context.getResourceTypes())) {
      description.append(format(qualifierPattern, qualifier, qualifierLabel(context, qualifier)));
    }
    description.append("</ul>");

    return description.toString();
  }

  private static String qualifierLabel(QualifierParameterContext context, String qualifier) {
    String qualifiersPropertyPrefix = "qualifiers.";
    return context.getI18n().message(context.getUserSession().locale(), qualifiersPropertyPrefix + qualifier, "");
  }

  public static class QualifierParameterContext {
    private final I18n i18n;
    private final ResourceTypes resourceTypes;
    private final UserSession userSession;

    private QualifierParameterContext(UserSession userSession, I18n i18n, ResourceTypes resourceTypes) {
      this.i18n = i18n;
      this.resourceTypes = resourceTypes;
      this.userSession = userSession;
    }

    public static QualifierParameterContext newQualifierParameterContext(UserSession userSession, I18n i18n, ResourceTypes resourceTypes) {
      return new QualifierParameterContext(userSession, i18n, resourceTypes);
    }

    public I18n getI18n() {
      return i18n;
    }

    public ResourceTypes getResourceTypes() {
      return resourceTypes;
    }

    public UserSession getUserSession() {
      return userSession;
    }
  }
}
