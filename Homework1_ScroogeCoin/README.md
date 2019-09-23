# Homework 1:ScroogeCoin  
**Class:**
2019 FinTech  
**Name:**
曹继中  
**StudentID:**
1901212561  
## A.Summary
* In this homework, I have updated the TxHandler class, which contains 3 methods: *TxHandler()*, *isValidTx()*, and *handleTxs()*. 
The idea of the implementation will be described in detail later.  
* I also created a test suite to verify the implementation. The third part is the description and purpose of the test functions. 
* This is the first time I use java to finish a project. Thanks to everyone who gives me help selflessly and patiently.
## B.The idea of the implementation
### 1. ***TxHandler()***  
* This method is used to creat a public ledger whose current UTXOPool is utxoPool.  
* In order to make a defensive copy of utxoPool, I use the *UTXOPool(UTXOPool uPool)* constructor.  
```js 
this.utxoPool = new UTXOPool(utxoPool);
``` 
### 2. ***isValidTx()***  
* This method is used to verify the validity of each transaction. It returns true only if the transaction meets the following five conditions.  
**Ⅰ.All outputs claimed by {@code tx} are in the current UTXOpool.**  
  * This condition makes sure that all the inputs of the transaction are generated from past transactions' outputs.
  * To implement this, I go through all the inputs of the transaction, and check if the utxoPool (create from *TxHandler()*) contains the current input's utxo.
  ```js
  if (!utxoPool.contains(currentUtxo))
      return false;
  ```
**Ⅱ.The signatures on each input of {@code tx} are valid.**  
  * This condition makes sure that people who paid the money is the money's owner.
  * To implement this, I go through  all the inputs of the transaction and check if all the inputs' *publickey*, *message* and  *signature* can match.
   ```js
  if (currentInput.signature == null || lastOutput.address == null )
      return false;
  else if (!Crypto.verifySignature(lastOutput.address,tx.getRawDataToSign(ii),currentInput.signature))
      return false;
  ```
  
  

* (3) no UTXO is claimed multiple times by tx,
* (4) all of tx’s output values are non-negative, and
* (5) the sum of tx’s input values is greater than or equal to the sum of
its output values; and false otherwise.
