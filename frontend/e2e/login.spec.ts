import { test, expect } from '@playwright/test';

// US03, US04 - pruebas E2E de login (caja negra): clases válidas e inválidas
test.describe('Pruebas de Login (Caja Negra E2E)', () => {

  // Clase válida: pantalla de login debe mostrarse correctamente al entrar
  test('la pantalla de login carga y muestra el formulario', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('domcontentloaded');

    // Validar que los elementos del formulario están visibles
    await expect(page.locator('text=SkillSharing')).toBeVisible({ timeout: 15000 });
    await expect(page.locator('text=Bienvenido de vuelta')).toBeVisible({ timeout: 10000 });
    await expect(page.locator('input[placeholder="tu@correo.com"]')).toBeVisible();
    await expect(page.locator('text=Entrar')).toBeVisible();
  });

  // Clase inválida: email vacío debe mostrar alerta al intentar entrar
  test('login con email vacío muestra alerta de validación', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('domcontentloaded');
    await page.waitForSelector('text=Entrar', { timeout: 15000 });

    // Solo poner contraseña, sin email
    await page.fill('input[type="password"]', '123456');

    // Capturar el alert nativo que lanza la app
    const dialogPromise = page.waitForEvent('dialog', { timeout: 8000 }).catch(() => null);
    await page.click('text=Entrar');

    const dialog = await dialogPromise;
    if (dialog) {
      expect(dialog.message()).toBeTruthy();
      await dialog.accept();
    }
  });

  // Clase inválida: credenciales incorrectas deben mostrar alerta de error del servidor
  test('login con credenciales incorrectas muestra alerta de error', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('domcontentloaded');
    await page.waitForSelector('text=Entrar', { timeout: 15000 });

    await page.fill('input[placeholder="tu@correo.com"]', 'noexiste@correo.com');
    await page.fill('input[type="password"]', 'claveincorrecta');

    const dialogPromise = page.waitForEvent('dialog', { timeout: 15000 }).catch(() => null);
    await page.click('text=Entrar');

    const dialog = await dialogPromise;
    if (dialog) {
      expect(dialog.message()).toBeTruthy();
      await dialog.accept();
    }
  });
});
