const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    baseUrl: "https://www.google.com",
    supportFile: false,
    projectId: "tmp1cm",
    record: true,
  }
});
