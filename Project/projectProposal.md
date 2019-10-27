# DApp : A Charity System Based on Ethereum

Charitable organizations often encounter barriers to success due to lack of transparency, accountability issues, and limits to the ways they can accept donations. The use of blockchain technology offers an alternative solution.

## **I.** **Motivation**

There’s a set of problems charities need to address. We’ll look at three of them.

####  **1.** **Loss of Credibility**

A series of scandals have rocked the way that the public perceives the typical charity, and trust in charitable organizations.

 On the one hand, some charities fail to find out that some personal help information may be exaggerated or even fake. What’s worse, they even conspire with the beneficiaries to deceive the public. Therefore, the public begins to suspect that public compassion is really being applied to those in need.

 On the other hand, there is a suspicion that a great proportion of donations do not reach the beneficiaries they are supposed to reach. Charities failed to have transparency and clarity about how that money is spent. Even though some charities make efforts to do so, it is a time-consuming and laborious task.

 Essentially, the term charity has been used and abused, and much of the public is no longer willing to take charities’ word as bond.

#### **2.** **High Administrative Cost**

Like for-profit businesses, non-profit charities require personnel to keep their organization running. However, it turns out that some are woefully inefficient at channelling donations to the people they're supposed to help. If the donation can be used more effectively, it will benefit to more people.

#### **3.** **Information Island**

At present, the data of each charity is not shared. Therefore, different public welfare platforms are information islands. It occurred that some users repeatedly raised funds on multiple platforms. This will also cause a waste of donation and low resource allocation efficiency.

## **Ⅱ.** **Solution**

In our project, we build an application model based on Ethereum to record all information from charitable donation into the block chain, which makes sure decentralization, transparency, non-tampering and traceability. Here are design concepts:

![](\chart.png)

#### **1.Release donation project.** 

First, beneficiaries submit their information and relative evidentiary document to launch a donation project. Second, the notary authority will verify the project. And then our system establishes a *Smart Contract of Donation* signed by notary authority’s private key so that we can claim responsibility if there is any fake in the project.

#### **2.Plan donation schemes.** 

Beneficiaries and notary authority consult with how they will spend the donated funds. Besides, a *Smart Contract of Usage*, which includes conditions that trigger donation using and the specific usage of donations, is posted in the block chain. Likewise, this smart contract is signed by notary authority’s private key.

#### **3.Ether donation.** 

Donors can browse sorts of donation schemes, select one, determine the amount and donate Ether. *Smart Contract of Donation* will update states of block chain based on each donation. What’s more, donors receive feedback when their donated fund is used.

#### **4.Donation usage.** 

Once all conditions set before are met, the system will trigger *Smart Contract of Usage* to spend donated Ether in different specific ways. The usage information will be recorded into the block chain to prevent abuse and ensure traceability.

#### **5.Public supervision.** 

Charity block chain is transparent to all users. Therefore, the public can query the data in the chain from the open port. Due to information sharing, our charity system is dependable and open.

## Ⅲ. **Rationale**

Ethereum has the obvious advantages in decentralization, openness and transparency, information traceability, and automatic execution through smart contracts. These advantages can solve the problems the charities face perfectly.

#### **1.** **Global and Decentralized**

Ethereum presents high levels of decentralization, which can restore public confidence in the Philanthropy. What’s more, the global state can prevent beneficiaries raising funds repeatedly and solve the information island problem effectively.

#### **2.** **Total transparency**

Ethereum can make the donation process more open and transparent. Every donation will be recorded directly in the distributed ledger, which can ensure that donors can clearly understand the flow of their funds. The tamper-resistance ensures that financial information will not be altered without the approval of all involved members. 

####  **3.** **Smart Contract**

Through smart contracts, funds can move directly from donors to the beneficiaries if certain conditions are met. This means that they do not need to rely on a centralized government or other institution. This can effectively prevent the capital embezzlement by charities.

####  **4.** **Reduced expenses**

Ethereum has the potential to simplify the way charities are managed, automating parts of the process and reducing the overall costs by requiring fewer intermediaries. This will improve the efficiency of resource allocation.

All in all, using Ethereum we can build a decentralized autonomous charity where donors can trace their money from the moment it is given, to the moment it will be spent. This will restore people's confidence in charity greatly and may even reinvent the philanthropy.