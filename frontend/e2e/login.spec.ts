import { test, expect } from '@playwright/test';

test.describe('Pruebas de Login (Caja Negra E2E)', () => {
  
  test('Login con credenciales válidas debería ir al Home', async ({ page }) => {
    // 1. Ir a la app (Expo Web carga por defecto)
    await page.goto('/');

    // 2. Llenar credenciales
    await page.fill('input[placeholder="tu@correo.com"]', 'instructor@test.com');
    await page.fill('input[placeholder="••••••••"]', '123456');

    // 3. Hacer clic en iniciar sesión
    await page.click('text=Iniciar Sesión');

    // 4. Validar que aparece el Home (esperar un texto o elemento del home)
    await expect(page.locator('text=Explora Sesiones')).toBeVisible({ timeout: 10000 });
  });

  test('Login con credenciales inválidas debería mostrar alerta', async ({ page }) => {
    await page.goto('/');

    await page.fill('input[placeholder="tu@correo.com"]', 'invalido@test.com');
    await page.fill('input[placeholder="••••••••"]', 'mala-clave');
    
    await page.click('text=Iniciar Sesión');

    // Manejar el alert nativo (en web sale un alert de navegador)
    page.on('dialog', async dialog => {
      expect(dialog.message()).toContain('No se pudo conectar al servidor');
      await dialog.accept();
    });
  });
});
