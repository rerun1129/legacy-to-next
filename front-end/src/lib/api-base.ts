export const FMS_API_URL =
  process.env.NEXT_PUBLIC_FMS_API_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080";

export const ADMIN_API_URL =
  process.env.NEXT_PUBLIC_ADMIN_API_URL ??
  "http://localhost:8081";
