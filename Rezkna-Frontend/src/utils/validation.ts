/** Matches the backend's @Email validation closely enough for client-side UX feedback. */
export function isValidEmail(value: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.trim());
}

/** Mirrors identity-service's @Size(min = 8) on RegisterRequest.password. */
export function isValidPassword(value: string): boolean {
  return value.length >= 8;
}

/** Mirrors identity-service's 6-digit OTP format. */
export function isValidOtpCode(value: string): boolean {
  return /^[0-9]{6}$/.test(value.trim());
}
