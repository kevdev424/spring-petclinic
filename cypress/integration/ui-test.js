import { configureAxe } from 'cypress-axe'

describe('Accessibility Test', () => {
  beforeEach(() => {
    cy.visit('https://www.google.com')
    cy.wait(1000)
  })

  it('Search query should be accessible', () => {
    cy.get('input[title="Search"]').type('Accessibility Testing')
    cy.get('input[value="Google Search"]').click()

    cy.wait(3000)

    cy.injectAxe()
    cy.checkA11y(null, null, configureAxe())
  })
})
