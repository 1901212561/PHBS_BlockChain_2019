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
* This method is to creat a public ledger whose current UTXOPool is utxoPool.  
* In order to make a defensive copy of utxoPool, I use the UTXOPool(UTXOPool uPool) constructor.  
```js 
this.utxoPool = new UTXOPool(utxoPool);
``` 
