import org.junit.Before;
import org.junit.Test;
import java.security.*;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;


public class BlockChainTest {

    private BlockChain blockChain;
    private KeyPair genesisPair;
    private Block genesisBlock;
    private TransactionPool txPool = new TransactionPool();


    private KeyPair KeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        return generator.generateKeyPair();
    }

    private Transaction createTransaction(byte[] prevTxHash, KeyPair sender, KeyPair[] receivers, int outputIndex, double[] amount)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Transaction tx = new Transaction();
        tx.addInput(prevTxHash, outputIndex);
        for (int i= 0; i < receivers.length; i++){
            tx.addOutput(amount[i], receivers[i].getPublic());
        }
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(sender.getPrivate());
        signature.update(tx.getRawDataToSign(0));
        tx.addSignature(signature.sign(), 0);
        tx.finalize();
        return tx;
    }

    @Before
    // Initializing the block chain.
    public void before() throws NoSuchAlgorithmException {
        genesisPair = KeyPair();
        genesisBlock = new Block(null, genesisPair.getPublic());
        genesisBlock.finalize();
        blockChain= new BlockChain(genesisBlock);
    }
    /**
     *---------------------------------------------Single Branch Scenario--------------------------------------------
     * Test Strategy:
     * Using the constructor to create a new empty block chain and then add some blocks to see if it can add the block
     * to the blockchain correctly,
     * 01. when there is only genesis block.
     * 02. when there is a valid block with only one valid transaction. TThis transaction spent parent block's coinbase money.
     * 03. when there is a valid block with several valid transactions.
     * 04. when there is an invalid transaction in the transaction pool.
     * 05. when the block's previous is null.
     * 06. when the block's prehash isn't in the blockchain.
     * 07. when the block contains invalid transactions
     * 08. when the blockchain's height doesn't satisfy the storage condition.
     * --------------------------------------------------------------------------------------------------------------
     */

    // Test 01. when there is only genesis block.
    @Test
    public void GenesisBlockOnly() {
        Block latestBlock = blockChain.getMaxHeightBlock();
        Transaction coinBaseTx = new Transaction(25, genesisPair.getPublic());

        assertThat(latestBlock.getHash(), equalTo(genesisBlock.getHash()));
        assertThat(latestBlock.getCoinbase(), equalTo(coinBaseTx));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().get(0), equalTo(new UTXO(coinBaseTx.getHash(), 0)));
        assertThat(blockChain.getTransactionPool().getTransactions(), equalTo(txPool.getTransactions()));
        assertThat(blockChain.getTransactionPool().getTransactions().isEmpty(), equalTo(true));
    }

    // Test 02. when there is a valid block with only one valid transaction.
    @Test
    public void AValidBlockWithSpendCoinBaseTransaction() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPair[] bobKeypair = {KeyPair()};
        KeyPair aliceKeyPair = KeyPair();
        double[] amount = {25.0};
        // Create a new transaction: spending the money generated from the genesis block.
        Transaction tx = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, bobKeypair, 0,amount);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        blockHandler.processTx(tx);
        Block validBlock = blockHandler.createBlock(aliceKeyPair.getPublic());
        validBlock.finalize();

        assertThat(blockHandler.processBlock(validBlock), equalTo(true));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(validBlock.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, aliceKeyPair.getPublic())));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(2));
        assertThat(blockChain.getTransactionPool().getTransactions().isEmpty(), equalTo(true));
    }

    // Test 03. when there is a valid block with several valid transactions.
    @Test
    public void AValidBlockWithSeveralValidTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 7;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        // Create new transactions.
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, new KeyPair[]{keyPair[1], keyPair[2]}, 0,new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[3], keyPair[4]}, 0,new double[]{8, 2});
        txs[2] = createTransaction(txs[0].getHash(), keyPair[2], new KeyPair[]{keyPair[5], keyPair[6]}, 1,new double[]{7, 8});

        BlockHandler blockHandler = new BlockHandler(blockChain);
        for(int i = 0; i < numOfTransactions; i++){
            blockHandler.processTx(txs[i]);
        }
        Block validBlock = blockHandler.createBlock(keyPair[0].getPublic());
        validBlock.finalize();

        assertThat(blockHandler.processBlock(validBlock),equalTo(true));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(validBlock.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[0].getPublic())));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(5));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }

    // Test 04. when there is an invalid transaction in the transaction pool.
    @Test
    public void AnInvalidTransactionInTransactionPool() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 5;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        // Create new transactions.
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, new KeyPair[]{keyPair[1], keyPair[2]}, 0,new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[3]}, 0,new double[]{10});
        txs[2] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[4]}, 0,new double[]{10});

        BlockHandler blockHandler = new BlockHandler(blockChain);
        for(int i = 0; i < numOfTransactions; i++){
            blockHandler.processTx(txs[i]);
        }
        Block validBlock = blockHandler.createBlock(keyPair[0].getPublic());
        validBlock.finalize();

        assertThat(blockHandler.processBlock(validBlock),equalTo(true));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(validBlock.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[0].getPublic())));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(3));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(1));
    }

    // Test 05. when the block's previous is null.
    @Test
    public void BlockWithNullPreHash() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPair[] bobKeypair = {KeyPair()};
        KeyPair aliceKeyPair = KeyPair();
        double[] amount = {25.0};
        Transaction tx = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, bobKeypair, 0,amount);
        BlockHandler blockHandler = new BlockHandler(blockChain);
        blockHandler.processTx(tx);

        Block invalidBlcok = new Block(null, aliceKeyPair.getPublic());
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        TxHandler handler = new TxHandler(uPool);
        Transaction[] txs = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(txs);
        for (int i = 0; i < rTxs.length; i++)
            invalidBlcok.addTransaction(rTxs[i]);
        invalidBlcok.finalize();

        assertThat(blockHandler.processBlock(invalidBlcok), equalTo(false));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(genesisBlock.getHash()));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(1));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(1));
    }

    // Test 06. when the block's prehash isn't in the blockchain.
    @Test
    public void BlockWithWrongPreHash() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 8;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, new KeyPair[]{keyPair[1], keyPair[2]}, 0,new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[3], keyPair[4]}, 0,new double[]{8, 2});
        BlockHandler blockHandler1 = new BlockHandler(blockChain);
        for(int i = 0; i < numOfTransactions - 1; i++){
            blockHandler1.processTx(txs[i]);
        }
        Block theFirstBlock = blockHandler1.createBlock(keyPair[0].getPublic());
        theFirstBlock.finalize();

        txs[2] = createTransaction(txs[0].getHash(), keyPair[2], new KeyPair[]{keyPair[5], keyPair[6]}, 1,new double[]{7, 8});
        BlockHandler blockHandler2 = new BlockHandler(blockChain);
        blockHandler2.processTx(txs[2]);
        Block invalidBlcok = new Block(genesisBlock.getCoinbase().getHash(), keyPair[7].getPublic());// This block has the wrong prehash.
        UTXOPool uPool = blockChain.getMaxHeightUTXOPool();
        TransactionPool txPool = blockChain.getTransactionPool();
        TxHandler handler = new TxHandler(uPool);
        Transaction[] TX = txPool.getTransactions().toArray(new Transaction[0]);
        Transaction[] rTxs = handler.handleTxs(TX);
        for (int i = 0; i < rTxs.length; i++)
            invalidBlcok.addTransaction(rTxs[i]);
        invalidBlcok.finalize();

        assertThat(blockHandler1.processBlock(theFirstBlock), equalTo(true));
        assertThat(blockHandler2.processBlock(invalidBlcok), equalTo(false));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(theFirstBlock.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[0].getPublic())));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(4));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(1));
    }

    // Test 07. when the block contains invalid transactions.
    @Test
    public void BlockWithInvalidTransactions() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 5;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, new KeyPair[]{keyPair[1], keyPair[2]}, 0,new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[3]}, 0,new double[]{10});
        txs[2] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[4]}, 0,new double[]{10});

        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block invalidBlcok = new Block(genesisBlock.getHash(), keyPair[0].getPublic());
        for (int i = 0; i < numOfTransactions; i++)
            invalidBlcok.addTransaction(txs[i]);
        invalidBlcok.finalize();

        assertThat(blockHandler.processBlock(invalidBlcok), equalTo(false));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(genesisBlock.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, genesisPair.getPublic())));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(1));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }

    // Test 08. when the blockchain's height doesn't satisfy the storage condition.
    @Test
    public void StorageCondition() throws NoSuchAlgorithmException{
        int numOfPlayers = 23;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayers];

        block[0] = new Block(genesisBlock.getHash(),keyPair[0].getPublic());
        block[0].finalize();

        for (int i = 1; i < numOfPlayers; i++){
            block[i] = new Block(block[i-1].getHash(),keyPair[i].getPublic());
            block[i].finalize();
        }

        for (int i = 0; i < numOfPlayers; i++){
            assertThat(blockHandler.processBlock(block[i]), equalTo(true));
        }
        
        assertThat(blockChain.getblockChain().size(),equalTo(20));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(24));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(block[numOfPlayers-1].getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[numOfPlayers-1].getPublic())));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }



    /**
     *-----------------------------------------------------Forking Scenario----------------------------------------------
     * Test Strategy:
     * Using the constructor to create a new empty block chain, adding some blocks and then make some forks
     * to see if it can add the blocks in the blockchain correctly.
     * 01. The fork should satisfy CUT_OFF_AGE_Condition.
     * 02. If there are multiple blocks at the same height, it should consider the oldest block in the longest valid branch.
     * 03. If there are two branches with the same height, whichever has the next block first will become to the new
     * longest valid branch.
     * ------------------------------------------------------------------------------------------------------------------
     */

    // Test 01.CUT_OFF_AGE_Condition
    @Test
    public void CUT_OFF_AGE_Condition() throws NoSuchAlgorithmException{
        int numOfPlayers = 13;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayers];
        block[1] = new Block(genesisBlock.getHash(),keyPair[1].getPublic());
        block[1].finalize();
        for (int i = 2; i < 12; i++){
            block[i] = new Block(block[i-1].getHash(),keyPair[i].getPublic());
            block[i].finalize();
        }
        block[0] = new Block(genesisBlock.getHash(),keyPair[0].getPublic());
        block[0].finalize();
        block[12] = new Block(block[1].getHash(),keyPair[12].getPublic());
        block[12].finalize();
        for (int i = 1; i < 12; i++){
            assertThat(blockHandler.processBlock(block[i]), equalTo(true));
        }
        assertThat(blockHandler.processBlock(block[0]), equalTo(false));
        assertThat(blockHandler.processBlock(block[12]), equalTo(true));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(12));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(block[11].getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[11].getPublic())));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }

    // Test 02. There are multiple blocks at the same height,
    @Test
    public void MultipleBlocksAtTheSameHeight() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        int numOfPlayers = 9;
        int numOfTransactions = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        Transaction[] txs = new Transaction[numOfTransactions];
        txs[0] = createTransaction(genesisBlock.getCoinbase().getHash(), genesisPair, new KeyPair[]{keyPair[1], keyPair[2]}, 0,new double[]{10, 15});
        txs[1] = createTransaction(txs[0].getHash(), keyPair[1], new KeyPair[]{keyPair[3], keyPair[4]}, 0,new double[]{8, 2});
        txs[2] = createTransaction(txs[0].getHash(), keyPair[2], new KeyPair[]{keyPair[5], keyPair[6]}, 1,new double[]{7, 8});


        BlockHandler blockHandler = new BlockHandler(blockChain);

        blockHandler.processTx(txs[0]);
        Block block1 = new Block(genesisBlock.getHash(),keyPair[0].getPublic());
        block1.addTransaction(txs[0]);
        block1.finalize();

        blockHandler.processTx(txs[1]);
        Block block2 = new Block(block1.getHash(),keyPair[7].getPublic());
        block2.addTransaction(txs[1]);
        block2.finalize();

        blockHandler.processTx(txs[2]);
        Block block3 = new Block(block1.getHash(),keyPair[8].getPublic());
        block3.addTransaction(txs[2]);
        block3.finalize();

        assertThat(blockHandler.processBlock(block1), equalTo(true));
        assertThat(blockHandler.processBlock(block2), equalTo(true));
        assertThat(blockHandler.processBlock(block3), equalTo(true));
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(5));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(block2.getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[7].getPublic())));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }

    // Test 03. The side branch becomes to the longest valid branch.
    @Test
    public void ForkingAttack() throws NoSuchAlgorithmException{
        int numOfPlayers = 3;
        KeyPair[] keyPair = new KeyPair[numOfPlayers];
        for(int i = 0; i < numOfPlayers; i++){
            keyPair[i] = KeyPair();
        }
        BlockHandler blockHandler = new BlockHandler(blockChain);
        Block[] block = new Block[numOfPlayers];
        block[1] = new Block(genesisBlock.getHash(),keyPair[1].getPublic());
        block[1].finalize();

        block[0] = new Block(genesisBlock.getHash(),keyPair[0].getPublic());
        block[0].finalize();
        block[2] = new Block(block[0].getHash(),keyPair[2].getPublic());
        block[2].finalize();

        for (int i = 0; i < 3; i++){
            assertThat(blockHandler.processBlock(block[i]), equalTo(true));
        }
        assertThat(blockChain.getMaxHeightUTXOPool().getAllUTXO().size(), equalTo(3));
        assertThat(blockChain.getMaxHeightBlock().getHash(), equalTo(block[2].getHash()));
        assertThat(blockChain.getMaxHeightBlock().getCoinbase(), equalTo(new Transaction(25, keyPair[2].getPublic())));
        assertThat(blockChain.getTransactionPool().getTransactions().size(), equalTo(0));
    }





}
