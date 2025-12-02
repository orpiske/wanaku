import { useCallback } from "react";
import {
  getApiV1Capabilities,
  postApiV1Resources,
  getApiV1Resources,
  deleteApiV1Resources,
  postApiV1ResourcesResponse,
  getApiV1ResourcesResponse,
  deleteApiV1ResourcesResponse,
  getApiV1CapabilitiesResponse,
} from "../../api/wanaku-router-api";
import { DeleteApiV1ResourcesParams, ResourceReference } from "../../models";

export const useResources = () => {
  /**
   * List management resources.
   */
  const listManagementResources = useCallback(
    (options?: RequestInit): Promise<getApiV1CapabilitiesResponse> => {
      return getApiV1Capabilities(options);
    },
    []
  );

  /**
   * Expose a resource.
   */
  const exposeResource = useCallback(
    (
      resourceReference: ResourceReference,
      options?: RequestInit
    ): Promise<postApiV1ResourcesResponse> => {
      return postApiV1Resources(resourceReference, options);
    },
    []
  );

  /**
   * List resources.
   */
  const listResources = useCallback(
    (options?: RequestInit): Promise<getApiV1ResourcesResponse> => {
      return getApiV1Resources(undefined, options);
    },
    []
  );

  /**
   * Remove a resource.
   */
  const removeResource = useCallback(
    (
      params?: DeleteApiV1ResourcesParams,
      options?: RequestInit
    ): Promise<deleteApiV1ResourcesResponse> => {
      return deleteApiV1Resources(params, options);
    },
    []
  );

  return {
    listManagementResources,
    exposeResource,
    listResources,
    removeResource,
  };
};
