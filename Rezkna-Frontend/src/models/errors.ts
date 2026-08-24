/**
 * Structured, screen-safe error shape. `message` is always a generic,
 * user-facing string (French) - never a raw backend/exception message,
 * per the Sprint 1 error-handling contract.
 */
export type ApiErrorKind =
  | 'validation'
  | 'unauthorized'
  | 'forbidden'
  | 'notFound'
  | 'conflict'
  | 'rateLimited'
  | 'server'
  | 'network';

export interface ApiError {
  kind: ApiErrorKind;
  status: number | null;
  message: string;
}

export function isApiError(value: unknown): value is ApiError {
  return (
    typeof value === 'object' &&
    value !== null &&
    'kind' in value &&
    'message' in value
  );
}

/**
 * Safely extracts a user-facing message from anything a screen might catch:
 * a structured ApiError, a plain Error (e.g. from a social sign-in stub), or
 * something unexpected. Never surfaces a raw exception/stack trace.
 */
export function getErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Une erreur inattendue s’est produite. Veuillez réessayer.';
}
