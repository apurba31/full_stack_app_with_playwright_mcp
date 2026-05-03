import { test, expect, request } from '@playwright/test';

test.describe('Health', () => {
  test('frontend serves the dashboard with header', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByText('FullStack Tasks')).toBeVisible();
  });

  test('backend /actuator/health reports UP via the proxy', async ({ baseURL }) => {
    const ctx = await request.newContext({ baseURL });
    const res = await ctx.get('/actuator/health');
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.status).toBe('UP');
  });

  test('health indicator shows operational status in the UI', async ({ page }) => {
    await page.goto('/');
    const indicator = page.getByTestId('health-indicator');
    await expect(indicator).toBeVisible();
    // Eventually shows the green "operational" / UP status (vs "degraded").
    await expect(indicator).toHaveAttribute('data-healthy', 'true', { timeout: 15_000 });
    await expect(indicator).toContainText(/healthy/i);
  });
});
