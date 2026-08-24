import {GATEWAY_BASE_URL} from '../../config/env';
import type {ApiError, ApiErrorKind} from '../../models/errors';

const REQUEST_TIMEOUT_MS = 15000;

interface ApiEnvelope<T> {
  ok: boolean;
  message: string;
  data: T | null;
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  body?: unknown;
  /** Bearer token to attach, if this call requires authentication. Callers
   * (identityApi/partnerApi) are responsible for reading it from the correct
   * session - the client itself has no notion of "diner" vs "partner". */
  token?: string;
}

/**
 * Maps an HTTP status to a safe, generic, French user-facing message. The
 * backend's own `message` is only trusted for 400/409, where identity-service
 * deliberately returns curated, non-sensitive text (e.g. "An account with
 * this email already exists"). 401 always uses a fixed generic message -
 * the backend itself never distinguishes "wrong password" from "wrong token
 * type" from "no token", by design, and the client must not try to guess.
 */
function toApiError(status: number, backendMessage?: string): ApiError {
  switch (status) {
    case 400:
      return {
        kind: 'validation',
        status,
        message: backendMessage || 'Données invalides. Vérifiez les informations saisies.',
      };
    case 401:
      return {
        kind: 'unauthorized',
        status,
        message: 'Votre session a expiré ou les identifiants sont incorrects.',
      };
    case 403:
      return {
        kind: 'forbidden',
        status,
        message: "Vous n'avez pas l'autorisation d'effectuer cette action.",
      };
    case 404:
      return {kind: 'notFound', status, message: 'Cette ressource est introuvable.'};
    case 409:
      return {
        kind: 'conflict',
        status,
        message: backendMessage || 'Cette information existe déjà.',
      };
    case 429:
      return {
        kind: 'rateLimited',
        status,
        message: 'Trop de tentatives. Veuillez patienter avant de réessayer.',
      };
    default:
      return {
        kind: 'server',
        status,
        message: 'Une erreur inattendue s’est produite. Veuillez réessayer.',
      };
  }
}

/**
 * Minimal centralized HTTP client. Every request goes through the Gateway -
 * callers never see or construct a raw URL. Always resolves to the response
 * envelope's `data` on success, or throws a structured ApiError on failure.
 */
export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const {method = 'GET', body, token} = options;

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  let response: Response;
  try {
    response = await fetch(`${GATEWAY_BASE_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
  } catch {
    const networkError: ApiError = {
      kind: 'network' as ApiErrorKind,
      status: null,
      message: 'Impossible de contacter le serveur. Vérifiez votre connexion.',
    };
    throw networkError;
  } finally {
    clearTimeout(timeoutId);
  }

  let envelope: ApiEnvelope<T> | null = null;
  try {
    envelope = (await response.json()) as ApiEnvelope<T>;
  } catch {
    envelope = null;
  }

  if (!response.ok || !envelope || envelope.ok === false) {
    throw toApiError(response.status, envelope?.message);
  }

  return envelope.data as T;
}
