import { useCallback } from "react";
import {
  getApiV1Capabilities,
  postApiV1Tools,
  getApiV1Tools,
  deleteApiV1Tools,
  getApiV1CapabilitiesResponse,
  postApiV1ToolsResponse,
  getApiV1ToolsResponse,
  deleteApiV1ToolsResponse,
} from "../../api/wanaku-router-api";
import {
  DeleteApiV1ToolsParams,
  ToolReference,
  GetApiV1ToolsParams,
} from "../../models";

export const useTools = () => {
  /**
   * List management tools.
   */
  const listManagementTools = useCallback(
    (options?: RequestInit): Promise<getApiV1CapabilitiesResponse> => {
      return getApiV1Capabilities(options);
    },
    []
  );

  /**
   * Add a tool.
   */
  const addTool = useCallback(
    (
      toolReference: ToolReference,
      options?: RequestInit
    ): Promise<postApiV1ToolsResponse> => {
      return postApiV1Tools(toolReference, options);
    },
    []
  );

  /**
   * List tools.
   */
  const listTools = useCallback(
    (
      params?: GetApiV1ToolsParams,
      options?: RequestInit
    ): Promise<getApiV1ToolsResponse> => {
      return getApiV1Tools(params, options);
    },
    []
  );

  /**
   * Remove a tool.
   */
  const removeTool = useCallback(
    (
      params?: DeleteApiV1ToolsParams,
      options?: RequestInit
    ): Promise<deleteApiV1ToolsResponse> => {
      return deleteApiV1Tools(params, options);
    },
    []
  );

  return {
    listManagementTools,
    addTool,
    listTools,
    removeTool,
  };
};
