# Gleibnif Tokens: Templates, Classes and Instances

A Token in the Decentralized Ledger domain serves as representation of an asset/liability, commodity/currency, right/proof. These tokens are normally stored in a crypto-wallet which provides secure access to the owner(s). Creation (minting), transfer and use (burning) of tokens are reflected on the decentralized ledger of a specific block-chain. Gleibnif as a platform is blockchain agnostic.

There are 2 major Token types namely, Fungible (interchangeable) and Non-Fungible (distinct). A token representing a commodity such as a bag of potatoes or 1 kWh energy is fungible - the one is the same as the other and a potential buyer does not care which specific instance het gets -  while a token representing an asset such as a car or computer is Non-Fungible. (There is a hybrid type of token that represents fungible and non-fungible aspects in a specific context)

Tokens has wide applicability far beyond proof of ownership of over-hyped NFT Artworks; e.g. Tokens can be used to transfer Voting Rights, Entrance (Ticket), Attestations, etc.

## Behaviour

Gleibnif supports **tokens** that adheres to the Token Taxonomy Framework as set out by the Interwork Alliance. It defines **behaviours** as capabilities or restrictions that applies to a token class along with Properties of a token, as information or data it contains.

The framework provides an extensible set of Behaviours, such as:

- Transferable/Non-Transferable: ability to transfer the token from one owner to the next
- Divisible/None-divisible(Whole): Decimal places a token can be divided into
- Singleton: Only one version of this token can ever exist — an art work;
- Mintable: Token class that can replenished (according to some constraints) e.g. as more commodities represented by the token are produced — energy produces, potatoes grown;
- Role-support: the token provides role-definition attributes that constrain specific actions;
- Burnable: Tokens that can be removed from supply once consumed/transformed;
- Attestable: return a cryptographic proof of authenticity, validity and provenance of  a source document
- Credible: When burned or retired a credit token is created  on its behalf;
- Delegable: Behaviours of this token can be invoked by another appointed party;
- Encumbrable: Execution of behaviours relatated to this token is prevented while encumbered;
- Burnable : Support the burning or decommissioning of token instances of the class
- Compliant : comply with legal requirements, e.g. KYC and AML.
- Encumberable : implements this behavior will have restrictions preventing certain behaviors like transferable, burnable, etc. from working while it is encumbered.
- Fabricteable : Unique tokens can be fabricated from a base tokenization
- Holdable : Every token instance has an owner. A hold specifies a payer, a payee, a maximum amount, a notary and an expiration time. When the hold is created, the specified token balance from the payer is put on hold. The hold can only be executed (partially or the full amount) by the notary, which triggers the transfer of the tokens from the payer to the payee.
- Issuable : This token has a controlling a central party, the issuer, is the only one able to create/transfer/destroy tokens.
- Logable : Record log entries from its owner with a generic payload.
- Offsetable : A token class that implements this behavior is burned or retired with its value being applied to offset another balance. Once a token is offset, it can no longer be used
- Overdraftable : This token grants an overdraft credit limit to a wallet owner, who can then make transfers or create holds without the required (positive) balance.
- Pauseable : Pausable is an influencing behavior that can be applied to other behaviors in the Token.
- Processable : A token class that implements this behavior is burned or retired when the next token is issued in a supply chain of tokens.
- Redeemable : This behavior only applies to unique tokens. Redeemed tokens can no longer be spent.
- Revocable : This token has a controlling a central party, the issuer, is able to retire/burn tokens that it has issued, regardless of owner.
- Roles : A token can have behaviors that the class will restrict invocations to a select set of parties or accounts that are members of a role or group.  This is a generic behavior that can apply to a token many times to represent many role definitions within the template. This behavior will allow you to define what role(s) to create and what behavior(s) to apply the role to in the TemplateDefinition.
- UniqueTransferable : The unique transferable behavior provides the owner the ability to transfer the ownership to another party or account of one or more unique tokens owned. This behavior and does not transfer the tokens themselves. Rather, new tokens are created by the transfer transaction. Because this behavior works with unique tokens, the invocation request can take multiple tokens as inputs to be transferred. The quantity of the assets being transferred to the recipients of the transaction needs to be the same quantity as the input tokens. If you do not want to transfer the entire quantity of the asset represented by the token, you can transfer a portion of the asset and the transaction will automatically make you the owner of the remaining balance. Using the example above, if only spend 50 dollars of the 100 dollar token, the transfer transaction will automatically create a new token worth 50 dollars with you as the owner. All input tokens of the transaction need to be of the same type and the tokens being transferred need to belong to the transaction initiator and are unspent.

## Properties

Properties of a token are used to define the information or data a token contains about itself and to record its activities. Some properties are set when the token class is created from a template, like its name and owner while others are set and updated over a token’s lifetime. How a property value is set determines what type of property it is.
  