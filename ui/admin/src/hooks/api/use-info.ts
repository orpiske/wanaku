import { useCallback } from "react";
import { customFetch } from "../../custom-fetch";

/** Payload returned by the management info endpoint. */
export interface ServerInfo {
  name: string;
  version: string;
  /** Release channel of the running server: "early-access" or "stable". */
  channel: string;
}

interface InfoResponse {
  status: number;
  data: ServerInfo;
  headers: Headers;
}

export const useInfo = () => {
  const getInfo = useCallback((options?: RequestInit): Promise<InfoResponse> => {
    return customFetch<InfoResponse>("/api/v1/management/info", {
      ...options,
      method: "GET",
    });
  }, []);

  return { getInfo };
};
