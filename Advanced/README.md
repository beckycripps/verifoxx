## Advanced CorDapp Samples

This folder features Advanced sample projects, each of them demonstrates a complex CorDapp that uses multiple features of Corda.

### [Auction CorDapp](./auction-cordapp):
A blockchain application that leverages different features of Corda like [SchedulableState](https://docs.corda.net/docs/corda-os/event-scheduling.html#how-to-implement-scheduled-events), [StatePointer](https://docs.corda.net/docs/corda-os/api-states.html#state-pointers)
and [OwnableState](https://docs.corda.net/docs/corda-os/api-states.html#ownablestate). It also demonstrates how to perform a DvP (Delivery vs Payment) transaction on Corda.
<p align="center">
  <img src="./auction-cordapp/snaps/setup.png" alt="Corda" width="600">
</p>

### [Due Diligence CorDapp](./duediligence-cordapp):
A capital market-themed app that depicts an ideal shareable due diligence process between banks. It uses advance feature attachment functionality to whitelist the trusted auditors.

### [Negotiation CorDapp](./negotiation-cordapp):
An application that depicts the businsess negotiation and communication process over a distributed ledger system.
It consists of the proposing, negotiating, and settling a corda transaction.

### [Obligation CorDapp](./obligation-cordapp):
A simple i-owe-you application illustrates all of the steps of creating an obligation for a resource to change owners.

### [Secret Santa CorDapp](./secretsanta-cordapp):
This is an imlementation of Secret Santa using Corda as a tool to store multiple game states.It has a material-ui frontend that lets users create and self-service their own secret santa games. The frontend is implemented in ReactJS and the backend is implemented with a Spring Boot server and some corda flows. It is also equipped with an external emailing package(sendgrid), which you can utilze and turn the app into a live app and send the secret santa assignments to your friends'emails.
<p align="center">
  <img src="./secretsanta-cordapp/clients/src/main/webapp/src/Components/img/secret_corda.png" alt="Corda" width="200">
</p>

### [Snake and Ladder Game CorDapp](./snakesandladders-cordapp):
This sample implements a simple Snakes And Ladder Game on Corda. This CorDapp demonstrate the use of multiple features, including Corda Account Library and Oracle service.
<p align="center">
  <img src="./snakesandladders-cordapp/snaps/game.png" alt="Corda" width="500">
</p>

### [Superyacht CorDapp](./superyacht-cordapp):
This Superyacht CorDapp allows the creation, issuance, and purchase of yachts on the ledger.
This example also integrates TokenSDK to issue fiat currency to the buyer so that they can purchase the desired yacht.

### [Syndicated Lending CorDapp](./syndicated-lending):
This is a sample CorDapp which demonstrate a high level Syndicated Lending scenario on a Corda network.