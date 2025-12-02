import { useCallback } from "react";
import {
  getApiV1DataStore,
  postApiV1DataStore,
  deleteApiV1DataStoreId,
} from "../../api/wanaku-router-api";
import type { DataStore, GetApiV1DataStoreParams } from "../../models";

/**
 * Custom hook for DataStore API operations
 */
export const useDataStores = () => {
  const listDataStores = useCallback(
    (params?: GetApiV1DataStoreParams, options?: RequestInit) => {
      return getApiV1DataStore(params, options);
    },
    []
  );

  const addDataStore = useCallback(
    (dataStore: DataStore, options?: RequestInit) => {
      return postApiV1DataStore(dataStore, options);
    },
    []
  );

  const deleteDataStore = useCallback(
    (id: string, options?: RequestInit) => {
      return deleteApiV1DataStoreId(id, options);
    },
    []
  );

  return {
    listDataStores,
    addDataStore,
    deleteDataStore,
  };
};
