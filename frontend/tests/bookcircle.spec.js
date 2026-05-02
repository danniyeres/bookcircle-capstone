import { test, expect } from "@playwright/test";

const testUser = {
  email: "playwright.user@example.com",
  password: "Password123!",
};

const mockBooks = [
  {
    id: 1,
    title: "1984",
    author: "George Orwell",
    isbn: "9780451524935",
    coverUrl: "",
    description: "Dystopian novel",
    totalChapters: 24,
  },
  {
    id: 2,
    title: "Clean Code",
    author: "Robert C. Martin",
    isbn: "9780132350884",
    coverUrl: "",
    description: "Software craftsmanship",
    totalChapters: 17,
  },
];

const mockRooms = [
  {
    id: 101,
    name: "1984 Discussion Room",
    bookId: 1,
    bookTitle: "1984",
    h3Index: "89283082803ffff",
  },
];

async function setupApiMocks(page) {
  await page.route("**/auth/login", async (route) => {
    if (route.request().method() !== "POST") {
      await route.fallback();
      return;
    }

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        accessToken: "mock-jwt-token",
        tokenType: "Bearer",
        userId: 777,
        role: "USER",
      }),
    });
  });

  await page.route("**/books**", async (route) => {
    if (route.request().method() !== "GET") {
      await route.fallback();
      return;
    }

    const requestUrl = new URL(route.request().url());
    const query = (requestUrl.searchParams.get("query") || "").trim().toLowerCase();
    const books = query
      ? mockBooks.filter((book) => book.title.toLowerCase().includes(query))
      : mockBooks;

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify(books),
    });
  });

  await page.route("**/rooms**", async (route) => {
    const { method } = route.request();
    const requestUrl = new URL(route.request().url());

    const isRoomsListEndpoint = requestUrl.pathname.endsWith("/rooms");
    const isJoinEndpoint = /\/rooms\/\d+\/join$/.test(requestUrl.pathname);

    if (method === "GET" && isRoomsListEndpoint) {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(mockRooms),
      });
      return;
    }

    if (method === "POST" && isJoinEndpoint) {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: "",
      });
      return;
    }

    if (method === "POST" && isRoomsListEndpoint) {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          id: 202,
          name: "Playwright Test Room",
          bookId: 1,
          bookTitle: "1984",
          h3Index: "89283082803ffff",
        }),
      });
      return;
    }

    await route.fallback();
  });
}

test.beforeEach(async ({ page }) => {
  await setupApiMocks(page);
});

async function login(page) {
  await page.goto("/login");
  await page.evaluate(() => localStorage.clear());

  await page.getByPlaceholder("Enter your email").fill(testUser.email);
  await page.getByPlaceholder("Enter your password").fill(testUser.password);

  await page.getByRole("button", { name: "Sign In" }).click();
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
