import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
  ]),
  {
    rules: {
      "react-hooks/exhaustive-deps": "error",
      "no-restricted-syntax": [
        "error",
        {
          selector: "Comment[value=/eslint-disable.*react-hooks\\/exhaustive-deps/]",
          message: "exhaustive-deps 비활성화 금지. 의존성을 채우거나 ref 패턴으로 재설계하세요."
        }
      ]
    }
  }
]);

export default eslintConfig;
