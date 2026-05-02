import { test, expect } from "@playwright/test";

const testUser = {
  email: "zasulananuarbek3@gmail.com",
  password: "jasikjjj",
};

async function login(page) {
  await page.goto("/login");
  await page.evaluate(() => localStorage.clear());

  await page.getByPlaceholder("Enter your email").fill(testUser.email);
  await page.getByPlaceholder("Enter your password").fill(testUser.password);

  await page.getByRole("button", { name: "Sign In" }).click();

  // ❗ проверяем: либо успех, либо ошибка
  const error = page.getByText("Invalid credentials");

  if (await error.isVisible().catch(() => false)) {
    throw new Error("Login failed: invalid credentials");
  }

  // ✅ если всё ок — ждём редирект
  await expect(page).toHaveURL("/");
}

test("user can login and open dashboard", async ({ page }) => {
  await login(page);

  await expect(page.getByText("Available Books")).toBeVisible();
  await expect(page.getByText("Available Rooms")).toBeVisible();
});

test("user can open books page and search books", async ({ page }) => {
  await login(page);

  await page.getByRole("link", { name: "Books" }).click();

  await expect(page.getByRole("heading", { name: "Books Library" })).toBeVisible();

  await page.getByPlaceholder("Search books...").fill("1984");

  await expect(page.getByText("1984")).toBeVisible();
});

test("user can open rooms page", async ({ page }) => {
  await login(page);

  await page.getByRole("link", { name: "Rooms" }).click();

  await expect(page.getByRole("heading", { name: "Rooms" })).toBeVisible();
  await expect(page.getByText("All Rooms")).toBeVisible();
});

test("user can open create room page", async ({ page }) => {
  await login(page);

  await page.getByRole("link", { name: "Create Room" }).click();

  await expect(page.getByRole("heading", { name: "Create Reading Room" })).toBeVisible();
  await expect(page.getByPlaceholder("Enter room name")).toBeVisible();
  await expect(page.getByText("Choose Location")).toBeVisible();
});

test("user can fill create room form", async ({ page }) => {
  await login(page);

  await page.getByRole("link", { name: "Create Room" }).click();

  await page.getByPlaceholder("Enter room name").fill("Playwright Test Room");

  await page.locator('input[name="lat"]').fill("43.238949");
  await page.locator('input[name="lon"]').fill("76.889709");
  await page.locator('input[name="resolution"]').fill("9");

  await expect(page.getByPlaceholder("Enter room name")).toHaveValue("Playwright Test Room");
  await expect(page.locator('input[name="lat"]')).toHaveValue("43.238949");
  await expect(page.locator('input[name="lon"]')).toHaveValue("76.889709");
});

test("user can open profile page and see account info", async ({ page }) => {
  await login(page);

  await page.getByRole("link", { name: "Profile" }).click();

  await expect(page.getByRole("heading", { name: "Profile" })).toBeVisible();
  await expect(page.getByText("Account Information")).toBeVisible();
  await expect(page.getByText("USER")).toBeVisible();
});
