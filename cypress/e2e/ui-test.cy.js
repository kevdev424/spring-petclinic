//Positive test case that checks that the Google search bar is visible

describe("Google Search", () => {
  it("Should display the search bar", () => {
    cy.visit("https://www.google.com");
    cy.get('input[name="q"]').should("be.visible");
  });
});

//Negative test case that checks that an invalid search term returns no results

describe("Google Search", () => {
  it("Should return no results for an invalid search term", () => {
    cy.visit("https://www.google.com");
    cy.get('input[name="q"]').type("asdfghjklqwertyuiop");
    cy.get("#center_col").should("contain", "No results found for");
  });
});
