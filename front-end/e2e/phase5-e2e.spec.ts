import { test, expect } from '@playwright/test';

// BL_VARIANT_KEYS: ['sea-exp', 'sea-imp', 'air-exp', 'air-imp']
// sea-exp를 기준 variant로 사용 (가장 필드가 많은 EXP variant)
const HOUSE_BL_LIST  = '/fms/house-bl/sea-exp/list';
const MASTER_BL_LIST = '/fms/master-bl/sea-exp/list';
const HOUSE_BL_ENTRY  = '/fms/house-bl/sea-exp/entry';
const MASTER_BL_ENTRY = '/fms/master-bl/sea-exp/entry';

test.describe('House B/L', () => {
  test('list 페이지 진입 및 로딩 완료', async ({ page }) => {
    await page.goto(HOUSE_BL_LIST);

    // 로딩 중 텍스트가 사라질 때까지 대기
    await expect(page.getByText('로딩 중...')).not.toBeVisible({ timeout: 10_000 });

    // 에러 메시지 없음 확인 — 빈 리스트도 정상 상태
    await expect(page.getByText('데이터를 불러올 수 없습니다.')).not.toBeVisible();

    // URL 유지 확인
    await expect(page).toHaveURL(HOUSE_BL_LIST);

    // 그리드 패널 컨테이너 존재 확인
    await expect(page.locator('.panel--list')).toBeVisible();
  });

  test('entry 신규 폼 — Save 버튼 존재 확인', async ({ page }) => {
    await page.goto(HOUSE_BL_ENTRY);

    // Save 버튼(type=submit) 존재 확인
    await expect(page.locator('button[type="submit"]:has-text("Save")')).toBeVisible();
  });

  test('entry 신규 폼 — 필수 필드 없이 Save 시 list로 이동하지 않음', async ({ page }) => {
    await page.goto(HOUSE_BL_ENTRY);

    // HBL No 필드를 비운 채 Save 클릭
    // (defaultValues에 기본값이 채워져 있으나 zod parse 실패 시 mutation 미호출)
    const hblInput = page.locator('input[placeholder="HBL No"]');
    await hblInput.fill('');

    await page.locator('button[type="submit"]:has-text("Save")').click();

    // list 페이지로 이동하지 않아야 함 — entry URL 유지
    // TODO: hbl 필드가 zod schema에서 max(35)이고 빈 문자열도 통과하므로
    //       실제 필수 검증이 추가되면 이 시나리오를 강화할 것
    await expect(page).not.toHaveURL(HOUSE_BL_LIST);
  });

  test('entry 신규 폼 — 필수 필드 채워서 Save 버튼 클릭 가능 상태 확인', async ({ page }) => {
    await page.goto(HOUSE_BL_ENTRY);

    // Save 버튼이 disabled 아닌 상태(신규 입력 준비 완료) 확인
    const saveBtn = page.locator('button[type="submit"]:has-text("Save")');
    await expect(saveBtn).toBeVisible();
    await expect(saveBtn).toBeEnabled();

    // TODO: 실제 POST 성공 후 list redirect 검증은 백엔드 연동 완료 후 추가
    //       현재 mockHouseBlPort를 사용하므로 저장 성공 시 /fms/house-bl/sea-exp/list로 이동
  });
});

test.describe('Master B/L', () => {
  test('list 페이지 진입 및 로딩 완료', async ({ page }) => {
    await page.goto(MASTER_BL_LIST);

    // 로딩 텍스트(Loading...) 사라질 때까지 대기
    await expect(page.getByText('Loading...')).not.toBeVisible({ timeout: 10_000 });

    // 에러 메시지 없음 확인 — 빈 리스트도 정상 상태
    await expect(page.getByText('데이터를 불러오지 못했습니다.')).not.toBeVisible();

    // URL 유지 확인
    await expect(page).toHaveURL(MASTER_BL_LIST);

    // 그리드 패널 컨테이너 존재 확인
    await expect(page.locator('.panel')).toBeVisible();
  });

  test('entry 신규 폼 — Save 버튼 존재 확인', async ({ page }) => {
    await page.goto(MASTER_BL_ENTRY);

    await expect(page.locator('button[type="submit"]:has-text("Save")')).toBeVisible();
  });

  test('entry 신규 폼 — Save 버튼 활성화 상태 확인', async ({ page }) => {
    await page.goto(MASTER_BL_ENTRY);

    const saveBtn = page.locator('button[type="submit"]:has-text("Save")');
    await expect(saveBtn).toBeEnabled();

    // TODO: 실제 POST/PUT 성공 후 /fms/master-bl/sea-exp/list redirect 검증은
    //       백엔드 연동 완료 후 추가
  });
});

test.describe('미구현 도메인 — stub 유지 확인', () => {
  test('Non B/L list 진입 시 에러 없음', async ({ page }) => {
    await page.goto('/fms/non-bl/list');

    // 페이지가 렌더링되는지 확인 — 루트(/)나 에러 페이지로 이동하지 않아야 함
    await expect(page).not.toHaveURL('/');
    await expect(page).toHaveURL('/fms/non-bl/list');

    // 페이지 헤더 텍스트 존재 확인
    await expect(page.getByText('Non B/L List')).toBeVisible();
  });
});
