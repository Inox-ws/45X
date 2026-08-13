/** Client-side mirror of the backend's file checks (Section 9, Section 11) - a fast first check, not a substitute for it. */
export function validateFile(file: File, allowedExtensions: string[], maxSizeMb: number): string | null {
  const lowerName = file.name.toLowerCase();
  const hasAllowedExtension = allowedExtensions.some(ext => lowerName.endsWith(ext));
  if (!hasAllowedExtension) {
    return `Unsupported file type. Allowed: ${allowedExtensions.join(', ')}`;
  }
  if (file.size > maxSizeMb * 1024 * 1024) {
    return `File exceeds the maximum allowed size of ${maxSizeMb} MB.`;
  }
  if (file.size === 0) {
    return 'The selected file is empty.';
  }
  return null;
}
