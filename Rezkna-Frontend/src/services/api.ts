import { GATEWAY_BASE_URL } from '../config/env';

/** Matches the backend's common response envelope (common-lib ApiResponse<T>). */
export interface ApiResponse<T> {
  ok: boolean;
  message: string;
  data: T | null;
}

interface ApiRequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  /**
   * Sprint 0 has no login/token storage yet - this parameter exists so a
   * future authenticated request does not require rewriting this layer.
   */
  authToken?: string;
  body?: unknown;
}

/**
 * Minimal HTTP client foundation. Every request goes through the Gateway -
 * this is the only base URL the app is configured with, so there is no way
 * for calling code to accidentally reach a microservice port directly.
 */
export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<ApiResponse<T>> {
  const { method = 'GET', authToken, body } = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (authToken) {
    headers.Authorization = `Bearer ${authToken}`;
  }

  const response = await fetch(`${GATEWAY_BASE_URL}${path}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  return (await response.json()) as ApiResponse<T>;
}
