# Homework 2: Block Chain  
**Class:**
2019 FinTech  
**Name:**
曹继中  
**StudentID:**
1901212561  
## A.Summary
* In this homework, I have updated the **BlockChain** class, which contains 1 constructor *BlockChain()* and 5 methods: 
*getMaxHeightBlock()*, *getMaxHeightUTXOPool()*, *getTransactionPool()*, *addBlock()* and *addTransaction()*. The idea of 
the implementation will be described in detail in part C. 
* I create a public function *getUTXOPool()* in the TxHandler.java file I have created for homework 1 and 
copy the file to my code for homework 2.
* Considering that there might be multiple forks, the data structure of the blockchain should be a tree rather than a list. 
In the implementation, I took this into consideration and used *Map* as the storage structure. The details will be illustrated in part B. 
* I also created a test suite to verify the implementation. 
The part D is the description and purpose of the test functions. 
## B.The Selection of the Storage Structure
* Given there might be multiple forks, the data structure of the blockchain should be a tree rather than a list. While, for the storage structure, there is no need to store the blocks in to the *tree* because we can create a tree data structure using a *list* by storing the hash of this block and the state of this block.
* The state of the block record the content of this block, the height of the block and the corresponding UTXOPool. In order to implementation, I created a *BlockState* class to store these three elements and the ways to get them.
* Considering that the order of storage is meaningless, it would be a little bit inconvenient if we use *list* to store the blockchain.Finally, I decided to use *Map* as the storage structure. I consider the hash of the block as the key and the state of the blcok as the value. They can form a one-to-one mapping. This can improve the efficiency of the search using the key of the *Map*. 
  ```js
    private Map<byte[], BlockState> blockChain;
  ``` 
## C.The Idea of the Implementation
### 1. ***BlockChain()***  
* This  constructor is used to create an empty block chain with just a genesis block.  
* The implementation can be devided into four steps:
  * Step 1. Initialize the genesisBlock's state, including the block, its height, and the current utxoPool.
  * Step 2. Add the genesis block to an empty block chain.
  * Step 3: Create a variable to record the latest blockis state in the longest valid branch.
    ```js 
            BlockState InitialBlockChainState = new BlockState(genesisBlock, 0, InitialUtxoPool);
            this.latestBlock = InitialBlockChainState;
    ``` 
  * Step 4: Create a global Transaction Pool.
    ```js 
            this.transactionPool = new TransactionPool();
    ``` 
### 2. ***getMaxHeightBlock()*** & ***getMaxHeightUTXOPool()***
* These two methods are used to get the maximum height block and the UTXOPool for mining a new block on top of max height block respectively.
* Since we have recorded the latest block's state in the longest valid branch, we can get them easily using the following  sentence:
  ```js 
     public Block getMaxHeightBlock() {return latestBlock.block;}
     public UTXOPool getMaxHeightUTXOPool() {return latestBlock.utxoPool;}
  ```  
### 3. ***getTransactionPool()***  
* This method is used to get the transaction pool to mine a new block.
* From the assumptions and hints, we know that we could maintain only one global Transaction Pool for the block chain. So I implemented as shown below.
  ```js 
    public TransactionPool getTransactionPool() {return new TransactionPool(transactionPool);}
  ```  
### 4. ***addBlock()***  
* This method is used to add the block to the block chain if it is valid.
* It returns true only if the block meets the following three conditions.

  **01.The block's parent node is valid.**  
  * This condition makes sure that if I receive a block which claims to be a genesis block, that is to say, it's parent node's hash is null in this function, it can return false.
  * The other case is that the block's parent node isn't in the current blockchain.
    ```js
    if (block.getHash() == null || (!blockChain.containsKey(block.getPrevBlockHash())))
        return false;
    ```   
  **02.All the transactions in the block should be valid.**  
   * This condition makes sure that there is no invalid transactions in the block.
   * To implement this, I used *block.getTransactions()* to get the block's transactions and *TxHandler().handleTxs()* to get the valid translations. If the numbers of them are equal, then it proves all the transactions in the block are valid. 
 
  **03.The CUT_OFF_AGE condition should be satisfied.** 
   * For simplicity，the block should also meet the criteria that the block's height > (maxHeight - CUT_OFF_AGE). If not, it returns false.
     ```js
        private boolean CUT_OFF_AGE_Condition(Block block) {
           BlockState parentNode = blockChain.get(block.getPrevBlockHash());
           return parentNode.getHeight() + 1 > latestBlock.getHeight() - CUT_OFF_AGE;
        }
        if (!CUT_OFF_AGE_Condition(block))
            return false;
      ``` 
* If the given block can meet the these three conditions, it returns true. We need also finish these steps: 
  * Step 1. Add this block into the blockchain by recording its block state and the hash into the map *blockChain*.
  * Step 2. Update the TransactionPool.
    * From the assumptions and hints we know that it’s okay if some transactions get dropped during a block chain reorganization. So I didn't take the block chain reorganization's effect into consideration. So I just removed the block's transactions out of the Transaction Pool.  
  * Step 3: Delete the previous block to meet the storage condition. 
    * Since the entire block chain could be huge in size, I just keep around the most recent blocks. The threshold value is represented as *STORAGE*. 
### 5. ***addBlock()***  
* This method is used to add a transaction to the transaction pool. 
  ```js 
     public void addTransaction(Transaction tx) {transactionPool.addTransaction(tx);}
  ``` 
## D.The Description and Purpose of the Test Functions
### 01. The Design of the Test  
* In this homework, I have implemented the constructor *BlockChain()* and 5 methods in total. Given the state of the blockchain can be divided into two parts: the single branch scenario and the forking scenario, I test them in the scenarios respectively.
* For preparation, I created the  genesisBlock and initialized the BlockChain in the *@Before* module. The design and the results can be found in the next part. 
### 02.The Results of the Test 
#### Ⅰ. Single Branch Scenario  
* Using the constructor to create a new empty block chain and then add some blocks to see if it can add the block to the blockchain correctly. By this process, the six methods can be test. The details are shown in the following table. 

  Test Function  | Test Purpose  |  Data for testing  | The functions being test 
   ---- | ----- | ------ | ------ 
   *GenesisBlockOnly()*  |Test for only genesis block existing.  | No other data. | *BlockChain()*<br>*getMaxHeightBlock()*<br>*getMaxHeightUTXOPool()*<br>*getTransactionPool()* 
   *AValidBlockWithSpendCoinBaseTransaction()*  |Test for a valid block linked the genesis block. The valid block contains a transaction spending parent block's coinbase money.  |  A valid block with only one valid transaction.  | All the six methods I have implemented.   
   *AValidBlockWithSeveralValidTransactions()*  |Test for a valid block linked the genesis block.  |  A valid block with three valid transactions. | All the six methods I have implemented. 
   *AnInvalidTransactionInTransactionPool*  |Test for whether it can tell the invalid transactions in the transaction pool and leave them in the transaction pool rather than add them in the block.  |  A transaction pool with 2 valid transactions and an invalid transaction.  | All the six methods I have implemented. 
   *BlockWithNullPreHash* |Test if *addBlock()* can reject a block with null prehash.  |  A block whose previous hash is *null*.  | All the six methods I have implemented.  
   *BlockWithWrongPreHash()* |Test if *addBlock()* can reject a block whose prehash isn't in the blockchain.  |  A block whose prehash isn't in the blockchain.  | All the six methods I have implemented.  
   *BlockWithInvalidTransactions()* |Test if *addBlock()* can reject a block which contains invalid transactions.  |  A block whose prehash isn't in the blockchain.  | All the six methods I have implemented.  
   *StorageCondition()* |Test if the block will throw some blocks when the blockchain's max height doesn't satisfy the storage condition.  |  Genesis block and 21 valid blocks( The storage condition is 20 in my program).  | All the six methods I have implemented.  
* Note that when testing whether it can tell the invalid transactions in the transaction pool, I just test one case (double spending) because the other cases have been test in the last homework. So I just use the double spending as representative. 
#### Ⅱ. Forking Scenario
* In this scenario, I use the constructor to create a new empty block chain and then try to make some forks to see if it can add the blocks in the blockchain correctly. 
* All the cases can test all the six method I have been implemented so I won't mention  this in the following table. 

  Test Function  | Test Purpose  |  Data for testing  | Diagram 
     ---- | ----- | ------ | ------ 
  *CUT_OFF_AGE_Condition（）*  | Test for CUT_OFF_AGE_Condition. | Genesis block, 11 valid blocks linked one by one, a block whose parent node is genesis block and a block whose parent node is the first block linked to the genesis block.  | Fig. 1
  *MultipleBlocksAtTheSameHeight()*  |If there are multiple blocks at the same height, it should consider the oldest block in the longest valid branch. However, all of them should be in the clockchain. | Two valid blocks adding to the genesis block one after another. |
   *ForkingAttack()* |Test if there are two branches with the same height, whichever has the next block first will become to the new longest valid branch.  |  Genesis block and three valid blocks.  |  Fig. 3  
   
   
   Fig.1  <div align=center><img width="300" height="100" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework2_BlockChain/%20images/Forking01.png"/></div> 
   Fig.2  <div align=center><img width="300" height="100" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework2_BlockChain/%20images/Forking02.png"/></div>  
   Fig.3  <div align=center><img width="300" height="100" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework2_BlockChain/%20images/Forking03.png"/></div>   
### 03.The results from *IntelliJ IDEA*
* By programming the above test functions, all the tests can pass. The results from *IntelliJ IDEA* is below.
<div align=center><img width="900" height="250" src="https://github.com/1901212561/PHBS_BlockChain_2019/blob/master/Homework2_BlockChain/%20images/TestResult.png"/></div>




 
   
  
  

