import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export function proxy(request: NextRequest) {
  const hasAuth = request.cookies.has("fms.auth");
  const isLoginPage = request.nextUrl.pathname === "/login";

  if (!hasAuth && !isLoginPage) {
    return NextResponse.redirect(new URL("/login", request.url));
  }
  if (hasAuth && isLoginPage) {
    return NextResponse.redirect(new URL("/", request.url));
  }
  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon\\.ico|api/).*)"],
};
