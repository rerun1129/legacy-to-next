/**
 * 테스트 전용 Provider 래퍼.
 *
 * 패널 컴포넌트가 요구하는 세 가지 context 를 한 번에 제공한다:
 * - NextIntlClientProvider: useTranslations / useLocale (next-intl)
 * - FormProvider          : useFormContext (react-hook-form)
 * - QueryClientProvider   : useQuery (TanStack Query — useCodeAutocomplete, useEnumOptions)
 *
 * 실제 ko.json 메시지를 주입해 번역 키 누락 경고를 억제한다.
 * QueryClient 는 테스트마다 새 인스턴스를 생성해 캐시 오염을 차단한다.
 */
import React from "react";

// jsdom 에는 ResizeObserver 가 없다.
// use-grid-cell-selection.ts 의 mount effect 가 new ResizeObserver() 를 호출하므로
// GridList 를 포함한 패널 렌더 시 throw 를 막기 위해 no-op stub 을 주입한다.
class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}
if (typeof globalThis.ResizeObserver === "undefined") {
  globalThis.ResizeObserver = ResizeObserverStub as unknown as typeof ResizeObserver;
}
import { NextIntlClientProvider } from "next-intl";
import { FormProvider, useForm } from "react-hook-form";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import messages from "@/messages/ko.json";

function makeFreshQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        // 테스트 환경에서 네트워크 재시도로 인한 지연 방지
        retry: false,
      },
    },
  });
}

interface ProvidersProps {
  children: React.ReactNode;
}

export function Providers({ children }: ProvidersProps) {
  const methods = useForm();
  // 컴포넌트 수명 동안 단일 QueryClient 인스턴스 유지
  const [queryClient] = React.useState(makeFreshQueryClient);

  return (
    <QueryClientProvider client={queryClient}>
      <NextIntlClientProvider locale="ko" messages={messages}>
        <FormProvider {...methods}>{children}</FormProvider>
      </NextIntlClientProvider>
    </QueryClientProvider>
  );
}
