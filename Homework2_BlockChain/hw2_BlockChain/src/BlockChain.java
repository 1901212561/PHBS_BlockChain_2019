// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    public static int STORAGE = 20;
    private Map<byte[], BlockState> blockChain;
    private TransactionPool transactionPool;
    private BlockState latestBlock;

    private class BlockState{
        private Block block;
        private int height;
        private UTXOPool utxoPool;

        public BlockState(Block block, int height, UTXOPool utxoPool){
            this.block = block;
            this.height = height;
            this.utxoPool = utxoPool;
        }

        public int getHeight() {
            return height;
        }

        public UTXOPool getUtxoPool() {
            return utxoPool;
        }
    }


    public Map<byte[], BlockState> getblockChain(){
        return blockChain;
    }
    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */

    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        /**
         * In oder to create an empty block chain with just a genesis block, we need to finish the following steps:
         * Step 1: Initialize the genesisBlock's state.
         * Step 2: Add the genesis block to an empty block chain.
         * Step 3: Create a variable to record the latest block in the longest valid branch.
         * Step 4: Create a global Transaction Pool.
         */

        // Step 1: Initialize the genesisBlock's state, including the block, its height, and the current utxoPool.
        Transaction CoinbaseTX = genesisBlock.getCoinbase();
        UTXOPool InitialUtxoPool = new UTXOPool();
        UTXO utxo = new UTXO(CoinbaseTX.getHash(), 0);
        InitialUtxoPool.addUTXO(utxo, CoinbaseTX.getOutput(0));
        BlockState InitialBlockChainState = new BlockState(genesisBlock, 1, InitialUtxoPool);

        // Step 2: Add the genesis block to an empty block chain.
        // In order to save the structure of the tree, I use HashMap to store the data.
        this.blockChain = new HashMap<byte[], BlockState>();
        blockChain.put(genesisBlock.getHash(), InitialBlockChainState);

        // Step 3: Create latestBlock to record the latest block's state in the longest valid branch.
        this.latestBlock = InitialBlockChainState;

        // Step 3: Create a global Transaction Pool.
        this.transactionPool = new TransactionPool();
    }



    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return latestBlock.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return latestBlock.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return new TransactionPool(transactionPool);
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS

        if (block.getHash() == null || (!this.blockChain.containsKey(block.getPrevBlockHash()))) {
//            System.out.println("1 is false");
            return false;
            // If the block claims to be a genesis block (parent is a null hash)
            // or its parent node isn't in the previous Blockchain, returns false.
        }
        else if (!AllTransactionsAreValid(block)) {
//            System.out.println("2 is false");
            return false;
            // If there are any invalid transactions, returns false.
        }
        else if( !CUT_OFF_AGE_Condition(block)){
//            System.out.println("3 is false");
            return false;
            // If the block are at {@code height <= (maxHeight - CUT_OFF_AGE)}, returns false.
        }

        else{
            // If all the conditions can be satisfied, we can add this block into the blockchain,
            // and also update the TransactionPool.

            // Step 1: Add this block into the blockchain.
            BlockState parentNode = blockChain.get(block.getPrevBlockHash());
            TxHandler txHandler = new TxHandler(parentNode.getUtxoPool());
            // Current txHandler.getUTXOPool() doesn't add the coinbase transaction into the UTXOPool.

            // Add the coinbase utxo into the UTXOPool.
            txHandler.handleTxs(block.getTransactions().toArray(new Transaction[block.getTransactions().size()]));
            UTXOPool utxoPool = txHandler.getUTXOPool();
            Transaction CoinBaseTx = block.getCoinbase();
            utxoPool.addUTXO(new UTXO(CoinBaseTx.getHash(), 0), CoinBaseTx.getOutput(0));
            // Record the new block's state.
            BlockState CurrentBlock = new BlockState(block,parentNode.getHeight() + 1, utxoPool);
            // Put the block into the blockchain.
            blockChain.put(block.getHash(),CurrentBlock);
            // Update the latestBlock's info.
            if (latestBlock.getHeight() < CurrentBlock.getHeight())
                latestBlock = CurrentBlock;

            // Step 2: Update the TransactionPool.
            List<Transaction> transactions = block.getTransactions();
            for (Transaction tx: transactions) {
                transactionPool.removeTransaction(tx.getHash());
            }

            // Step 3: Delete the previous block to meet the storage condition.
            if (latestBlock.getHeight() > STORAGE){
                Iterator iter = blockChain.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    Object key = entry.getKey();
                    Object val = entry.getValue();
                    if(((BlockState)val).getHeight() <= latestBlock.getHeight() - STORAGE)
                        iter.remove();
                }
            }
            return true;
        }
    }


    /**
     * Verify if all the block's transactions are valid
     * Implementation methods:
     * Using the method TxHandler.handleTxs() to verify the block's transactions.
     * @return true if the size of verified transactions equals to the size of the block's transactions.
     */
    private boolean AllTransactionsAreValid(Block block) {
        BlockState parentNode = blockChain.get(block.getPrevBlockHash());
        Transaction[] possibleTxs = block.getTransactions().toArray(new Transaction[block.getTransactions().size()]);
        Transaction[] validTransactions = new TxHandler(parentNode.getUtxoPool()).handleTxs(possibleTxs);
        return validTransactions.length == possibleTxs.length;
    }

    /**
     * Verify if the block are at {@code height > (maxHeight - CUT_OFF_AGE)}
     * Implementation methods:
     * Get the height of the block's parentnode.
     * @return true if the height > (maxHeight - CUT_OFF_AGE).
     */
    private boolean CUT_OFF_AGE_Condition(Block block) {
        BlockState parentNode = blockChain.get(block.getPrevBlockHash());
        return parentNode.getHeight() + 1 > latestBlock.getHeight() - CUT_OFF_AGE;
    }


    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        transactionPool.addTransaction(tx);
    }
}