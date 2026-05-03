import { test, expect } from '@playwright/test';
import { fillSwitchBlForm } from './helpers/switch-bl-form';

// Switch B/L 시나리오 전제:
//   - House B/L이 저장된 상태(id 존재)에서만 entry 페이지에서 Switch B/L 버튼이 활성화됨
//   - house-bl-entry.tsx: canSwitchBl = isEdit && id != null && variant.key.startsWith('sea-')
//   - 따라서 beforeAll에서 API 직접 생성 후 id를 획득하는 패턴 사용 (phase5 패턴 동일)

test.describe.serial('Switch B/L E2E — sea-exp', () => {
  let houseBlId: number;

  test.beforeAll(async ({ request }) => {
    // House B/L API 직접 생성 (SEA / EXP — Switch B/L 버튼 활성 조건 충족)
    const res = await request.post('http://localhost:8080/api/house-bl', {
      data: {
        jobDiv: 'SEA',
        bound: 'EXP',
        hblNo: `HBL${Date.now()}`,
        shipmentType: 'HOUSE',
        freightTerm: 'PREPAID',
        polCode: 'KRBSA',
        podCode: 'USLAX',
      },
      headers: { 'Content-Type': 'application/json' },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    houseBlId = body.data.id;
    expect(houseBlId).toBeGreaterThan(0);
  });

  test('sea-exp House B/L entry → Switch B/L 버튼 클릭 → 모달 입력 → 저장', async ({ page }) => {
    const ts = Date.now();

    // sea-exp entry 페이지 진입 (isEdit=true → canSwitchBl 활성)
    await page.goto(`/fms/house-bl/sea-exp/entry?id=${houseBlId}`);

    // hbl 필드에 데이터 로드 완료 대기
    await page.waitForFunction(
      () => (document.querySelector('input[name="hbl"]') as HTMLInputElement | null)?.value?.length ?? 0 > 0,
      { timeout: 10_000 },
    );

    // Switch B/L 버튼 클릭 (house-bl-entry.tsx: .btn--secondary + 텍스트 "Switch B/L")
    await page.getByRole('button', { name: 'Switch B/L' }).click();

    // 모달 오픈 확인 (switch-bl-modal.tsx: role="dialog")
    await expect(page.getByRole('dialog')).toBeVisible({ timeout: 8_000 });

    // 모달 타이틀 확인
    await expect(page.getByText('SEA Switch B/L Management')).toBeVisible();

    // 헬퍼로 폼 입력
    await fillSwitchBlForm(page, ts);

    // Save 버튼 클릭 (.btn--primary — "Save" 텍스트)
    await page.getByRole('dialog').getByRole('button', { name: 'Save' }).click();

    // 저장 완료 후 모달 닫힘 확인 (onSuccess → onClose 호출 → isOpen=false → null 반환)
    await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 10_000 });

    // API 검증: 저장된 Switch B/L 데이터 확인
    const apiRes = await page.request.get(
      `http://localhost:8080/api/switch-bl/by-house-bl/${houseBlId}`,
    );
    expect(apiRes.ok()).toBeTruthy();
    const apiBody = await apiRes.json();
    expect(apiBody.data.switchBlNo).not.toBeNull();
    expect(apiBody.data.description.marks).not.toBeNull();
  });

  test('sea-exp Switch B/L 재오픈 → 기존 데이터 로드 확인 (UPDATE 모드)', async ({ page }) => {
    await page.goto(`/fms/house-bl/sea-exp/entry?id=${houseBlId}`);

    await page.waitForFunction(
      () => (document.querySelector('input[name="hbl"]') as HTMLInputElement | null)?.value?.length ?? 0 > 0,
      { timeout: 10_000 },
    );

    await page.getByRole('button', { name: 'Switch B/L' }).click();
    await expect(page.getByRole('dialog')).toBeVisible({ timeout: 8_000 });

    // UPDATE 모드 확인: Delete 버튼이 헤더에 나타남 (switch-bl-modal.tsx: isUpdateMode && Delete 버튼)
    await expect(
      page.getByRole('dialog').getByRole('button', { name: 'Delete' }),
    ).toBeVisible({ timeout: 8_000 });

    // 기존 switchBlNo 값이 폼에 로드됨 확인 (SBL 로 시작하는 값)
    const switchBlNoVal = await page.inputValue('input[placeholder="Switch B/L No"]');
    expect(switchBlNoVal).toMatch(/^SBL/);

    // 닫기
    await page.getByRole('dialog').getByRole('button', { name: 'Close' }).click();
    await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 5_000 });
  });

  test('sea-exp Switch B/L 삭제', async ({ page }) => {
    await page.addInitScript(() => { window.confirm = () => true; });

    await page.goto(`/fms/house-bl/sea-exp/entry?id=${houseBlId}`);

    await page.waitForFunction(
      () => (document.querySelector('input[name="hbl"]') as HTMLInputElement | null)?.value?.length ?? 0 > 0,
      { timeout: 10_000 },
    );

    await page.getByRole('button', { name: 'Switch B/L' }).click();
    await expect(page.getByRole('dialog')).toBeVisible({ timeout: 8_000 });

    // Delete 버튼 클릭 (confirm은 addInitScript로 자동 승인)
    await page.getByRole('dialog').getByRole('button', { name: 'Delete' }).click();

    // 삭제 성공 후 모달 닫힘 확인
    await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 10_000 });

    // API 검증: 삭제 후 404 응답 확인
    const apiRes = await page.request.get(
      `http://localhost:8080/api/switch-bl/by-house-bl/${houseBlId}`,
    );
    // 미존재 시 404 반환 (switch-bl-modal.tsx getByHouseBlId → NotFoundError → null)
    expect(apiRes.status()).toBe(404);
  });
});

test.describe.serial('Switch B/L E2E — sea-imp', () => {
  let houseBlId: number;

  test.beforeAll(async ({ request }) => {
    // House B/L API 직접 생성 (SEA / IMP)
    const res = await request.post('http://localhost:8080/api/house-bl', {
      data: {
        jobDiv: 'SEA',
        bound: 'IMP',
        hblNo: `HBL${Date.now()}`,
        shipmentType: 'HOUSE',
        freightTerm: 'PREPAID',
        polCode: 'USLAX',
        podCode: 'KRBSA',
      },
      headers: { 'Content-Type': 'application/json' },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    houseBlId = body.data.id;
    expect(houseBlId).toBeGreaterThan(0);
  });

  test('sea-imp House B/L entry → Switch B/L 버튼 클릭 → 모달 입력 → 저장', async ({ page }) => {
    const ts = Date.now();

    await page.goto(`/fms/house-bl/sea-imp/entry?id=${houseBlId}`);

    await page.waitForFunction(
      () => (document.querySelector('input[name="hbl"]') as HTMLInputElement | null)?.value?.length ?? 0 > 0,
      { timeout: 10_000 },
    );

    await page.getByRole('button', { name: 'Switch B/L' }).click();
    await expect(page.getByRole('dialog')).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText('SEA Switch B/L Management')).toBeVisible();

    await fillSwitchBlForm(page, ts);

    await page.getByRole('dialog').getByRole('button', { name: 'Save' }).click();

    // 모달 닫힘 확인
    await expect(page.getByRole('dialog')).not.toBeVisible({ timeout: 10_000 });

    // API 검증
    const apiRes = await page.request.get(
      `http://localhost:8080/api/switch-bl/by-house-bl/${houseBlId}`,
    );
    expect(apiRes.ok()).toBeTruthy();
    const apiBody = await apiRes.json();
    expect(apiBody.data.switchBlNo).not.toBeNull();
    expect(apiBody.data.description.marks).not.toBeNull();
  });
});
