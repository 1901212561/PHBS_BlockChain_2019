import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        //defensive copy
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        if (tx == null)
            return false;
        double totalInputAmount = 0;
        double totalOutputAmount = 0;
        // Go through all the current inputs
        for (int ii = 0; ii < tx.numInputs(); ii++) {

            Transaction.Input currentInput = tx.getInput(ii);
            UTXO currentUtxo = new UTXO(currentInput.prevTxHash, currentInput.outputIndex);
            Transaction.Output lastOutput = utxoPool.getTxOutput(currentUtxo);


            //To ensure (1) all outputs claimed by {@code tx} are in the current UTXO pool
            if (!utxoPool.contains(currentUtxo))
                return false;

            //To ensure (2) the signatures on each input of {@code tx} are valid
            if (currentInput.signature == null || lastOutput.address == null )
                return false;
            else if (!Crypto.verifySignature(lastOutput.address,tx.getRawDataToSign(ii),currentInput.signature))
                return false;

            //To ensure (3) no UTXO is claimed multiple times by {@code tx}
            //(3) ensures there is no double spending
            for (int jj = ii + 1; jj < tx.numInputs(); jj++) {
                Transaction.Input nextInput = tx.getInput(jj);
                UTXO nextUtxo = new UTXO(nextInput.prevTxHash, nextInput.outputIndex);
                if (currentUtxo.equals(nextUtxo))
                    return false;
            }
            totalInputAmount += lastOutput.value;
        }

        // Go through all the current outputs
        for (int kk = 0; kk < tx.numOutputs(); kk++) {
            Transaction.Output currentOutput = tx.getOutput(kk);

            //To ensure (4) all of {@code tx}s output values are non-negative
            if (currentOutput.value >= 0) {
                totalOutputAmount += currentOutput.value;
            } else {
                return false;
            }

            //To ensure (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values
            if (totalInputAmount < totalOutputAmount)
                return false;
        }
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        /**
         * Due to the transactions are unordered, some transactions may be valid due to other transactions' confirmation.
         * So we have to go through all the transactions over and over again until no valid transactions can be found.
         * If there are valid transactions, update the utxopool.
         */
        ArrayList<Transaction> validTransactions = new ArrayList<>();
        while (true){
            boolean flag = false;
            for(Transaction TX: possibleTxs){
                if (validTransactions.contains(TX))
                    continue;//This transaction has been checked, so continue.
                else {
                    if (isValidTx(TX)){
                        //This transaction is valid, then add it into the validTransactions and change the flag to true.
                        validTransactions.add(TX);
                        flag = true;
                        //Update the UTXOPool: Step 1 + Step 2
                        //Step 1: add new valid output
                        for(int mm=0 ; mm<TX.numOutputs(); ++mm){
                            UTXO updateUtxo = new UTXO(TX.getHash(), mm);
                            utxoPool.addUTXO(updateUtxo, TX.getOutput(mm));
                        }
                        //Step 2: delete the spent output
                        for(int nn=0 ; nn<TX.numInputs(); ++nn){
                            Transaction.Input updateInput = TX.getInput(nn);
                            UTXO updateutxo = new UTXO(updateInput.prevTxHash, updateInput.outputIndex);
                            utxoPool.removeUTXO(updateutxo);
                        }
                    }
                }
            }
            if (!flag)//If there is no valid transactions left, just break.
                break;
        }
        //Convert the ArrayList<Transaction> to Transaction
////        Method 1
//        Transaction[] acceptedTX = new Transaction[validTransactions.size()];
//        int num =0;
//        for(Transaction validtransaction : validTransactions){
//            acceptedTX[num] = validtransaction;
//            ++num;
//        }
//        return acceptedTX;

        return validTransactions.toArray(new Transaction[validTransactions.size()]);

    }

}
