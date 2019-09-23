
import org.junit.Test;
import org.junit.Before;
import java.security.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
*
* @author <Cao Jizhong>
* @since <pre>9月 21, 2019</pre>
* @version 1.0
*/

public class TxHandlerTest {

    // Extends the transaction class and add the new function: add signature.
    public static class TX extends Transaction {
        public void signTX(PrivateKey sk, int index) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
            // Generate the signature.
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(sk);
            signature.update(this.getRawDataToSign(index));
            //Add the signature to the transaction.
            this.addSignature(signature.sign(), index);
            this.finalize();
        }
    }

    private KeyPair pkScrooge;
    private KeyPair pkBob;
    private KeyPair pkAlexa;
    private TxHandlerTest.TX tx0, tx1, tx2, tx3, tx4, tx5, tx6, tx7, tx8, tx9, tx10;
    private TxHandler txHandler;


    @Before
    public void before() throws Exception {
        //Createcoins transactions
        tx0 = new TX();
        //Step 1: generate Scrooge's pk & sk
        pkScrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        //Step 2: update the input and output
        tx0.addOutput(10, pkScrooge.getPublic());
        byte[] prevHash = new byte[0];
        tx0.addInput(prevHash, 0);
        tx0.signTX(pkScrooge.getPrivate(), 0);
        //Step 3: initialize the UTXOPool
        UTXOPool utxopool = new UTXOPool();
        UTXO utxo = new UTXO(tx0.getHash(), 0);
        utxopool.addUTXO(utxo, tx0.getOutput(0));
        txHandler = new TxHandler(utxopool);


        //Paycoins transactions 1: Scrooge -> Bob amount: 9
        tx1 = new TxHandlerTest.TX();
        pkBob = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        tx1.addInput(tx0.getHash(), 0);
        tx1.addOutput(9, pkBob.getPublic());
        tx1.addOutput(1, pkScrooge.getPublic());
        tx1.signTX(pkScrooge.getPrivate(), 0);

        //Paycoins transactions 2: Bob -> Scrooge amount: 9, but with the wrong input index.
        tx2 = new TxHandlerTest.TX();
        tx2.addInput(tx1.getHash(), 1);
        tx2.addOutput(3, pkScrooge.getPublic());
        tx2.signTX(pkBob.getPrivate(), 0);

        //Paycoins transactions 3: Scrooge -> Bob amount: 8,  Scrooge -> Alexa amount: 3
        tx3 = new TxHandlerTest.TX();
        pkAlexa = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        tx3.addInput(tx0.getHash(), 0);
        tx3.addOutput(8, pkBob.getPublic());
        tx3.addOutput(3, pkAlexa.getPublic());
        tx3.signTX(pkScrooge.getPrivate(), 0);


        //Paycoins transactions 4: Bob -> Alexa amount: 5
        tx4 = new TxHandlerTest.TX();
        tx4.addInput(tx1.getHash(), 0);
        tx4.addOutput(5, pkAlexa.getPublic());
        tx4.addOutput(4, pkBob.getPublic());
        tx4.signTX(pkBob.getPrivate(), 0);

        //Paycoins transactions 5: Alexa -> Scrooge amount: 3
        tx5 = new TxHandlerTest.TX();
        tx5.addInput(tx4.getHash(), 0);
        tx5.addOutput(3, pkScrooge.getPublic());
        tx5.addOutput(2, pkAlexa.getPublic());
        tx5.signTX(pkAlexa.getPrivate(), 0);

        //Paycoins transactions 6: Alexa -> Bob amount: 3
        tx6 = new TxHandlerTest.TX();
        tx6.addInput(tx4.getHash(), 0);
        tx6.addOutput(2, pkBob.getPublic());
        tx6.addOutput(3, pkAlexa.getPublic());
        tx6.signTX(pkAlexa.getPrivate(), 0);

        //Paycoins transactions 7: Bob -> Alexa amount: 9, Bob -> Scrooge amount: 6
        tx7 = new TxHandlerTest.TX();
        tx7.addInput(tx1.getHash(), 0);
        tx7.addInput(tx1.getHash(), 0);
        tx7.addOutput(3, pkAlexa.getPublic());
        tx7.addOutput(6, pkScrooge.getPublic());
        tx7.signTX(pkBob.getPrivate(), 0);

        //Paycoins transactions 8: Bob -> Alexa amount: -3
        tx8 = new TxHandlerTest.TX();
        tx8.addInput(tx1.getHash(), 0);
        tx8.addOutput(-3, pkAlexa.getPublic());
        tx8.addOutput(12, pkScrooge.getPublic());
        tx8.signTX(pkBob.getPrivate(), 0);

        //Paycoins transactions 9: Bob -> Alexa amount: 3, with wrong signature
        tx9 = new TxHandlerTest.TX();
        tx9.addInput(tx1.getHash(), 0);
        tx9.addOutput(3, pkAlexa.getPublic());
        tx9.signTX(pkScrooge.getPrivate(), 0);



    }

    /**
     *------------------------------------------isValidTx() Test-------------------------------------------
     * A.Function Introduction:
     * The function isValidTx is used to verify the validity of each transaction.
     * It returns Ture if
     *  (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     *  (2) the signatures on each input of {@code tx} are valid,
     *  (3) no UTXO is claimed multiple times by {@code tx},
     *  (4) all of {@code tx}s output values are non-negative, and
     *  (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values.
     * B.Test Strategy:
     * Violate each rule and test the results.
     * ----------------------------------------------------------------------------------------------------
     */

    @Test
    public void valid_transaction() {
        //This is a valid transaction.
        assertEquals(true, txHandler.isValidTx(tx1));
    }

    @Test
    public void output_NOT_in_utxopool1() {
        //Output claimed by tx4 is not in the current UTXO pool, because tx1 hasn't happened.
        assertEquals(false, txHandler.isValidTx(tx4));
    }

    //the correction for the output_NOT_in_utxopool1()
    @Test
    public void output_NOT_in_utxopool11() {
        //Output claimed by tx4 is not in the current UTXO pool, because tx1 hasn't happened.
        txHandler.handleTxs(new Transaction[]{tx1});
        assertEquals(true, txHandler.isValidTx(tx4));
    }

    @Test
    public void output_NOT_in_utxopool2() {
        //Output claimed by tx2 is not in the current UTXO pool beacuse of pointing the wrong tx1's index
        txHandler.handleTxs(new Transaction[]{tx1});
        assertEquals(false, txHandler.isValidTx(tx2));
    }

    @Test
    public void invalid_signature() {
        //Tx9's input signature is invalid
        txHandler.handleTxs(new Transaction[]{tx1});
        assertEquals(false, txHandler.isValidTx(tx9));
    }

    @Test
    public void utxo_claimed_twice() {
        //UTXO is claimed multiple times by tx7.
        txHandler.handleTxs(new Transaction[]{tx1});
        assertEquals(false, txHandler.isValidTx(tx7));
    }

    @Test
    public void output_negative() {
        //Tx8's output value is negative.
        txHandler.handleTxs(new Transaction[]{tx1});
        assertEquals(false, txHandler.isValidTx(tx8));
    }

    @Test
    public void output_larger_than_input() {
        //To test the transactions whose total output amount > total input amount
        assertEquals(false, txHandler.isValidTx(tx3));
    }

    /**
     *-------------------------------------------------handleTxs() Test---------------------------------------------------
     * A.Function Introduction:
     * The function is used to check an unordered array of proposed transactions.
     * It returns a mutually valid array of accepted transactions.
     * B.Test Strategy:
     * Given an unordered valid array of proposed transactions to see if it can tell.
     * Given an unordered invalid array of proposed transactions, such as containing double spending to see if it can tell.
     *---------------------------------------------------------------------------------------------------------------------
     */

    @Test
    public void unordered_valid_txs() {
        //Given an unordered valid array of proposed transactions.
        Transaction[] txsList = new Transaction[]{tx4, tx1, tx5};
        Transaction[] validList = new Transaction[]{tx1, tx4, tx5};
        assertArrayEquals(validList, txHandler.handleTxs(txsList));
//        assertEquals(3, txHandler.handleTxs(new Transaction[]{tx1, tx4, tx5}).length);
    }

    @Test
    public void unordered_invalid_txs() {
        //Given an unordered invalid array of proposed transactions, which contains double spending.
        Transaction[] txsList = new Transaction[]{tx4, tx6, tx1, tx5};
        Transaction[] validList = new Transaction[]{tx1, tx4, tx6};
        assertArrayEquals(validList, txHandler.handleTxs(txsList));
    }


}

