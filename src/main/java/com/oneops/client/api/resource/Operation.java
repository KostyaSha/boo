/*
 * Copyright 2017 Walmart, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.oneops.client.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.client.api.APIClient;
import com.oneops.client.api.OOInstance;
import com.oneops.client.api.ResourceObject;
import com.oneops.client.api.exception.OneOpsClientAPIException;
import com.oneops.client.api.util.JsonUtil;

public class Operation extends APIClient {

  private static final String RESOURCE_URI = "/operations/environments/";

  private String OPERATION_ENV_URI;
  private OOInstance instance;
  private String assemblyName;
  private String environmentName;

  public Operation(OOInstance instance, String assemblyName, String environmentName)
      throws OneOpsClientAPIException {
    super(instance);
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = String.format("Missing assembly name");
      throw new OneOpsClientAPIException(msg);
    }

    if (environmentName == null || environmentName.length() == 0) {
      String msg = String.format("Missing environment name");
      throw new OneOpsClientAPIException(msg);
    }

    this.assemblyName = assemblyName;
    this.environmentName = environmentName;
    this.instance = instance;
    OPERATION_ENV_URI = Assembly.ASSEMBLY_URI + assemblyName + RESOURCE_URI + environmentName;
  }

  /**
   * Lists all instances for a given assembly, environment, platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath listInstances(String platformName, String componentName)
      throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = String.format("Missing component name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.queryParam("instances_state", "all")
        .get(OPERATION_ENV_URI + "/platforms/" + platformName + "/components/" + componentName
    // + "/instances.json?instances_state=all");
            + "/instances");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to get instances due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get instances due to null response");
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Mark all instances for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath markInstancesForReplacement(String platformName, String componentName)
      throws OneOpsClientAPIException {
    List<Integer> instanceIds = listInstances(platformName, componentName).getList("ciId");
    return markInstancesForReplacement(platformName, componentName, instanceIds);
  }

  /**
   * Mark an instance for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath markInstanceForReplacement(String platformName, String componentName,
      Integer instanceId) throws OneOpsClientAPIException {
    List<Integer> instanceIds = Lists.newArrayList();
    instanceIds.add(instanceId);
    return markInstancesForReplacement(platformName, componentName, instanceIds);
  }

  /**
   * Mark an instance for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  JsonPath markInstancesForReplacement(String platformName, String componentName,
      List<Integer> instanceIds) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();
    jo.put("ids", instanceIds);
    jo.put("state", "replace");
    String uri = "/assemblies/" + assemblyName + "/operations/instances/state";

    Response response = request.body(jo.toString()).put(uri);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to set replace marker on instance(s) due to %s",
            response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to set replace marker on instance(s) due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  public JsonPath getLogData(String procedureId, List<String> actionIds)
      throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    String uri = "/operations/procedures/log_data";
    request.queryParam("procedure_id", procedureId);

    if (actionIds != null) {
      for (String actionId : actionIds) {
        request.queryParam("action_ids", actionId);
      }
    }

    Response response = request.get(uri);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to set replace marker on instance(s) due to %s",
            response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to set replace marker on instance(s) due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all procedures for a given platform
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath listProcedures(String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response =
        request.get(OPERATION_ENV_URI + "/platforms/" + platformName + "/procedures");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to get procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get procedures due to null response");
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Get procedure Id for a given platform, procedure name
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Integer getProcedureId(String platformName, String procedureName)
      throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (procedureName == null || procedureName.length() == 0) {
      String msg = String.format("Missing procedure name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    Integer procId = null;
    JsonPath procs = listProcedures(platformName);
    if (procs != null) {
      List<Integer> ids = procs.getList("ciId");
      List<String> names = procs.getList("ciName");
      for (int i = 0; i < names.size(); i++) {
        if (names.get(i).equals(procedureName)) {
          procId = ids.get(i);
          return procId;
        }
      }
    }

    String msg = String.format("Failed to get procedure with the given name " + procedureName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all actions for a given platform, component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath listActions(String platformName, String componentName)
      throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = String.format("Missing component name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(OPERATION_ENV_URI + "/platforms/" + platformName
        + "/components/" + componentName + "/actions/");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to get actions due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get actions due to null response");
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Execute procedure for a given platform
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath executeProcedure(String platformName, String procedureName, String arglist)
      throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (procedureName == null || procedureName.length() == 0) {
      String msg = String.format("Missing procedure name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    Transition transition = new Transition(instance, this.assemblyName);
    JsonPath platform = transition.getPlatform(this.environmentName, platformName);
    Integer platformId = platform.getInt("ciId");
    properties.put("procedureState", "active");
    properties.put("arglist", arglist);
    properties.put("definition", null);
    properties.put("ciId", "" + platformId);
    properties.put("procedureCiId", "" + getProcedureId(platformName, procedureName));
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    Response response = request.body(jsonObject.toString()).post("/operations/procedures/");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg =
            String.format("Failed to execute procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to execute procedures due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get procedure status for a given Id
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath getProcedureStatus(String procedureId) throws OneOpsClientAPIException {
    if (procedureId == null || procedureId.length() == 0) {
      String msg = String.format("Missing procedure Id to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get("/operations/procedures/" + procedureId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg =
            String.format("Failed to get procedure status due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to get procedure status with the given Id " + procedureId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get procedure status for a given Id
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath cancelProcedure(String procedureId) throws OneOpsClientAPIException {
    if (procedureId == null || procedureId.length() == 0) {
      String msg = String.format("Missing procedure Id to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("procedureState", "canceled");
    properties.put("definition", null);
    properties.put("actions", null);
    properties.put("procedureCiId", null);
    properties.put("procedureId", null);
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    Response response =
        request.body(jsonObject.toString()).put("/operations/procedures/" + procedureId);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg =
            String.format("Failed to cancel procedure due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format(
        "Failed to cancel procedure with the given Id " + procedureId + " due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Execute procedure for a given platform
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath executeAction(String platformName, String componentName, String actionName,
      List<String> instanceList, String arglist, int rollAt) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = String.format("Missing component name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (actionName == null || actionName.length() == 0) {
      String msg = String.format("Missing action name to fetch details");
      throw new OneOpsClientAPIException(msg);
    }
    if (instanceList == null || instanceList.size() == 0) {
      String msg = String.format("Missing instances list to fetch details");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    Transition transition = new Transition(instance, this.assemblyName);
    JsonPath component =
        transition.getPlatformComponent(this.environmentName, platformName, componentName);
    Integer componentId = component.getInt("ciId");
    properties.put("procedureState", "active");
    properties.put("arglist", arglist);

    properties.put("ciId", "" + componentId);
    properties.put("force", "true");
    properties.put("procedureCiId", "0");
    Map<String, Object> flow = Maps.newHashMap();
    flow.put("targetIds", instanceList);
    flow.put("relationName", "base.RealizedAs");
    flow.put("direction", "from");

    Map<String, Object> action = Maps.newHashMap();
    action.put("isInheritable", null);
    action.put("actionName", "base.RealizedAs");
    action.put("inherited", null);
    action.put("isCritical", "true");
    action.put("stepNumber", "1");
    action.put("extraInfo", null);
    action.put("actionName", actionName);

    List<Map<String, Object>> actions = Lists.newArrayList();
    actions.add(action);
    flow.put("actions", actions);

    Map<String, Object> definition = new HashMap<String, Object>();
    List<Map<String, Object>> flows = Lists.newArrayList();
    flows.add(flow);
    definition.put("flow", flows);
    definition.put("name", actionName);

    properties.put("definition", definition.toString());
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    jsonObject.put("roll_at", String.valueOf(rollAt));
    jsonObject.put("critical", "true");
    Response response = request.body(jsonObject.toString()).post("/operations/procedures/");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg =
            String.format("Failed to execute procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to execute procedures due to null response");
    throw new OneOpsClientAPIException(msg);
  }
}
