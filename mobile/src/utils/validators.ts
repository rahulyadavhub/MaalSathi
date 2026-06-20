const TRUCK_REG_REGEX = /^[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{1,4}$/;
const PHONE_REGEX = /^[6-9]\d{9}$/;

export function validateTruckNumber(raw: string): { valid: boolean; normalized: string } {
  const normalized = raw.toUpperCase().replace(/[\s\-]+/g, "");
  return { valid: TRUCK_REG_REGEX.test(normalized), normalized };
}

export function validatePhone(raw: string): { valid: boolean; normalized: string } {
  const normalized = raw.replace(/[\s\-+()]+/g, "");
  const digits = normalized.startsWith("91") && normalized.length === 12
    ? normalized.slice(2)
    : normalized;
  return { valid: PHONE_REGEX.test(digits), normalized: digits };
}

export function validateName(name: string): boolean {
  const trimmed = name.trim();
  return trimmed.length >= 2 && trimmed.length <= 50 && /[a-zA-Zऀ-ॿ]/.test(trimmed);
}
