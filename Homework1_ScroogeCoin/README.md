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
* This method is used to create a public ledger whose current UTXOPool is utxoPool.  
* In order to make a defensive copy of utxoPool, I use the *UTXOPool(UTXOPool uPool)* constructor.  
  ```js 
  this.utxoPool = new UTXOPool(utxoPool);
  ``` 
### 2. ***isValidTx()***  
* This method is used to verify the validity of the given transaction. 
* It returns true only if the transaction meets the following five conditions.  
  **Ⅰ.All outputs claimed by {@code tx} are in the current UTXOpool.**  
  * This condition makes sure that all the inputs of the transaction are generated from past transactions' outputs.
  * To implement this, I go through all the inputs of the transaction, and check if the utxoPool (create from *TxHandler()*) contains the current input's utxo.
    ```js
    if (!utxoPool.contains(currentUtxo))
        return false;
    ```  
  **Ⅱ.The signatures on each input of {@code tx} are valid.**  
   * This condition makes sure that people who paid the money is the money's owner.
   * To implement this, I go through all the inputs of the transaction and check if all the inputs' *publickey*, *message* and  *signature* can match.
      ```js
     if (currentInput.signature == null || lastOutput.address == null )
         return false;
     else if (!Crypto.verifySignature(lastOutput.address,tx.getRawDataToSign(ii),currentInput.signature))
         return false;
     ```
   **Ⅲ.No UTXO is claimed multiple times by {@code tx}.**  
   * This condition makes sure that there is no double spending in the given transaction.
   * To implement this, for a certain input, I go through all the inputs after it to see if there is any input using the same UTXO with it. If there is, then return false.
      ```js
     for (int jj = ii + 1; jj < tx.numInputs(); jj++) {
         Transaction.Input nextInput = tx.getInput(jj);
         UTXO nextUtxo = new UTXO(nextInput.prevTxHash, nextInput.outputIndex);
         if (currentUtxo.equals(nextUtxo))
             return false;
     }
     ```
  **Ⅳ.All of {@code tx}s output values are non-negative**  
   * This condition is easy to understand because nobody will spend negative amount of money.
   * To implement this, I go through all the outputs and check if their output amount is non-negative.
      ```js
     if (currentOutput.value < 0)
         return false;
     ```
   **Ⅴ.The sum of {@code tx}s input values is greater than or equal to the sum of its output values**  
   * This condition is also easy to understand because except  coinbase transaction, no paycoins transactions will have the output amount larger than the input amount.
   * To implement this, I go through all the outputs and outpus, and sum the amounts of inputs and outputs respectively. If the outputs larger than inputs, it returns false.
      ```js
     if (totalInputAmount < totalOutputAmount)
         return false;
     ```
* If the given transaction can meet the these five conditions, it returns true which means it is a valid transaction.  
### 3. ***handleTxs()***  
* Given an unordered array of proposed transactions, this method can check each transaction for correctness and then return a mutually valid array of accepted transactions, updating the current UTXO pool as well.  
* Considering the input array is unorder, some transactions may be valid due to other transactions' confirmation. So I go through all the transactions over and over again until no valid transactions can be found. If there are valid transactions, update the utxopool. This could be kind of inefficient, but it can make sure there won't be any mistake.
* To implement this, I divide it into three steps.  
  * Step 1. Go through all the transactions. If the transaction has been checked, change another transaction back to Step 1.  
  * Step 2. Check the validity of this transaction using *isValidTx()* . If it is valid,  
    * add it into the validTransactions.  
    * update the UTXOPool: add new valid output & delete the spent output.  
  * Step 3. If there is no new valid transactions can be found, stop going through all the transactions. The list *validTransactions* is what we need.
## C.The description and purpose of the test functions
### 01. The prepare of the test  
* I extend the transaction class and add a new method *signTX()* to add signature for each transaction.
* In the @Before module, I create the coinbase transaction *tx0* and initialize the UTXOPool. I also create 9 translations. Some of them are correct  while others are incorrect. They will be tested in the following @test modules.
### 02.The results of the test 
* In order to express the test results succinctly and clearly, I made the following two tables.
#### Ⅰ. ***isValidTx()***  
Test Function  | Test Pursue  |  Data for testing  |Expected Result  |Actual Result
 ---- | ----- | ------ | ------ | ------  
 *valid_transaction()*  |Test for valid transactions.  | Valid transaction *tx1* | True | True 
 *output_NOT_in_utxopool1()*  | Test for UTXO not being containing in UTXOPool because the previous transaction hasn't happened. | Transaction *tx4* without previous transaction *tx1* | False | False
  *output_NOT_in_utxopool2()*  | Test for UTXO not being contained in UTXOPool because of pointing the wrong previous transaction's index. | Transaction *tx2* with correct previous transaction *tx1* 's hashValue but wrong index| False | False
  *invalid_signature()*  | Test for transaction with wrong signature. | Transaction *tx9* with wrong signature| False | False
  *utxo_claimed_twice()*  | Test for no UTXO is claimed multiple times. | Transaction *tx7* with double spending| False | False
  *output_negative()*  | Test for all of transactions' output values are non-negative. | Transaction *tx8* with negative amount of outputs even though the total amount of outputs less than the total inputs.| False | False
  *output_larger_than_input()*  | Test for the sum of transactions' input values is greater than or equal to the sum of its output values. | Transaction *tx3* with output > input even though each output amount is less than the total inputs.| False | False
* Note that all the testing data with certain deficiencies can become the valid transactions only if we fix the deficiencies. This can be ensure that the data for testing are suitable for the certain circumstances we want.  
#### Ⅱ. ***handleTxs()***  
Test Function  | Test Pursue  |  Data for testing  |Expected Result (validList) |Actual Result
 ---- | ----- | ------ | ------ | ------  
 *unordered_valid_txs()*  |Test for an unordered valid array of proposed transactions.  | {*tx4, tx1, tx0, tx5*} | {*tx0, tx1, tx4, tx5*} | {*tx0, tx1, tx4, tx5*}  
 *unordered_invalid_txs()*  |Test for an unordered invalid array of proposed transactions.  | {*tx4, tx6, tx0, tx5, tx1*}                while *tx5* and *tx6* are double spending.| {*tx0, tx1, tx4, tx6*} | {*tx0, tx1, tx4, tx6*}  
* Note that for the input array {*tx4, tx1, tx0, tx5*}, each transaction is valid. Logically it can only work when the transctions happen as the orders describing bellow. But no matter what orders we send into the *unordered_valid_txs()*, it will return a mutually valid array of accepted transactions. So the method *handleTxs()* works.
<div align=center><img width="250" height="300" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework1_ScroogeCoin/unordered_valid_txs.png"/></div>

* For the second test, the input array {*tx4, tx6, tx0, tx5, tx1*} contains the double spending transactions *tx5* and *tx6*. So no matter what orders we send into the *unordered_valid_txs()*,  *tx5* and *tx6* won't pass the test at the same time. It only pass the transactions handle first. In this case, it is *tx6*.  
<div align=center><img width="400" height="300" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework1_ScroogeCoin/unordered_invalid_txs.png"/></div>








 
   
  
  

