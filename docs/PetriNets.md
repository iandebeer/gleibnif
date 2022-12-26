# Stateful orchestration of workflows, expressed as Coloured Petri-Nets

The core question that I am pursuing in this proposal is: How do you develop Decentralized Applications in a cost effective and resource effective manner?  Looking at it from a Functional Programming perspective, it looks like a case for composition of smart contracts as pure functions into higher order functions - dApps. Bounded Petri Nets exhibits Categorical Semantics in the way that **concatenable processes as strict Monoidal categories** model Net computations [[1]](#1) [[2]](#2)It is easy to see (if you are that way inclined) that Petri Nets form a Category of Petri. A _Coloured_ Petri Net is traversable using a state monad to step from an initial state to subsequent state.

Formally, a Petri Net is a state transition graph that maps Places (circles) to Transitions (rectangles) and Transitions to Places via Arcs (arrows).
It is well suited for describing the flow of discrete concurrent processes. Petri Nets are more concise than other process flow descriptions (like UML or BPMN) in that they have an exact mathematical definition of their execution semantics, with a well-developed mathematical theory for process analysis.

From the proposal perspective, Petri Nets are directed graphs consisting of Places(Stages), Transitions(Actions) and Arcs(Transaction). It models state-transitions of (concurrent) processes. I contend that there is a need to handle consecutive Smart Contract invocations (the dApp Protocol) within the context of a encapsulating state machine (FSM) as expressed by a Petri Net and executed by an off-chain dApp-container.

Because of it's Markov property - state transitions depend only on the currently available state.  In addition to main artifacts mentioned above, Petri Net execution relies on Markers and Tokens. The Marker indicates a requirement-gate that needs to be satisfied before the next transition can take place. For a given set of Tokens (current state) the PetriNet can be asked to step through to the next state (set of Tokens) as indicated by the Markers - guards - placed on the Arcs that join Places and Transitions.

![alt text](/assets/images/animate.gif "flow")

Stochastic Petri Nets are also used for validating and testing the Liveness, Boundedness and Reachability of distributed networks.

1. A business analyst should  create a dApp Protocol by joining Places (States) and Transitions with Arcs
2. Configuration of a specific instance of a dApp can be programmatically created or be informed by the UI connecting to the headless dApp through a GRPC/HTTP API
3. Smart Contract templates will allow reuse and create a possible market place of tested Contracts
4. Validation of dApp can be done in terms of Liveness, Boundedness and Reachability
5. Design patterns can be be implemented by a Petri Net:

![alt text](/assets/images/place_transitions.png "Arcs")

## As applied to the ErgPlatform

I believe we require a way to compose dApps from Ergo Boxes - Wallets, Smart Contracts and Transactions, much like Functional Programming allows you to compose applications from pure functions.

I wanted to test an intuition that the Categorical  (monoidal) aspects of Petri Nets lend itself to composing a dApp from the Boxes (Places) with guarding Contracts and  Transactions (Transitions).   I am basing this intuition on prior work I have done in creating an implementation of PetriNets (see https://github.com/iandebeer/castanet). It is a generalized implementation in Scala 3, developed with intent  to compose applications from Knative Services under Kubernetes.

As a starting point for ErgoHack, I used the "Heads or Tails Game" from the Ergoscript by Example repository (https://github.com/ergoplatform/ergoscript-by-example/blob/main/headsOrTails.md), to test the concept. I picked this example to render as a Petri Net because it has finite looping (5 plays) and forking and joining concepts. I want build this out so I can visualize this dApp, and then generalize the concept from there, to a point where one use it for dApp development by visualizing the stages, validating the flow, and running an instance of the dApp in the ErgoPlayground or through Appkit on the Ergo Blockchain.

I want to implement an Ergo-Castanet Client API over GRPC that will support:

```scala
val dAppID: String                = addDApp(flowSpec: org.ergoplatform.flow.spec.flowspec.FlowSpec)
val pngURL: URL                   = visualiseDApp(dAppID: String)
val animated_GIF_URL: URL         = validateDApp(dAppID: String ,markers: Seq(FlowMarkers)
val markerBitMap: Long            = excuteStepDApp(dAppID : String, markerBitMap: Long, transaction:ErgoTransaction)
```

Using the above API with the Head-or-Tails example I expect to visualize the dApp in the following Petri Net:

![Petri Net](/assets/images/Heads-Tails-Net.png)

 I created a project on GitHub: https://github.com/iandebeer/ergo-castanet.  The project depends on another open source project I published on GitHub https://github.com/iandebeer/castanet 
 I implemented a GRPC client/server using the Typelevel FS2-GRPC framework. The protobuf I pulled from https://github.com/ergoplatform/ergo-appkit/tree/develop/docs/design-contracts.

## As applied to the Cardano

## As applied to cross-chain dApp development
