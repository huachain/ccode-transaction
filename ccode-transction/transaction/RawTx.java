package org.hua.chain.ccode.transaction;

import org.hua.chain.ccode.core.datastore.InitDatastore;
import org.hua.chain.ccode.core.datastore.TxRawDatastore;
import org.hua.chain.ccode.core.model.Pagination;
import org.hua.chain.ccode.core.model.TransactionRawMO;
import org.hua.chain.ccode.core.model.TransactionReceiptMO;
import org.hua.chain.contract.Context;
import org.hua.chain.contract.ContractInterface;
import org.hua.chain.contract.annotation.Info;
import org.hua.chain.contract.annotation.Transaction;

@org.hua.chain.contract.annotation.Contract("rawTx")
public class RawTx implements ContractInterface{
	
	@Transaction
	public String chainHash(Context ctx, String txhash) {
		TxRawDatastore rawDs = new TxRawDatastore(ctx);
		return rawDs.getChainhash(txhash);
	}
	
	@Transaction
	public TransactionRawMO rawTx(Context ctx, String txhash) {
		TxRawDatastore rawDs = new TxRawDatastore(ctx);
		TransactionRawMO transactionRawMO = new TransactionRawMO().fromProto(rawDs.getRawTx(txhash));
		transactionRawMO.setChainid(new InitDatastore(ctx).getChainId());
		return transactionRawMO;
	}
	
	@Transaction
	public TransactionReceiptMO txReceipt(Context ctx, String txhash) {
		TxRawDatastore rawDs = new TxRawDatastore(ctx);
		return new TransactionReceiptMO().fromProto(rawDs.getTxReceipt(txhash));
	}
	
	@Transaction
	public Pagination<TransactionRawMO> pendingTxes(Context ctx,int pageSize,String bookmark) {
		TxRawDatastore rawDs = new TxRawDatastore(ctx);
		Pagination<TransactionRawMO> page = new Pagination<>();
		String bookMark = rawDs.pendingTxPage(pageSize, bookmark, (hash, proto)->{
			TransactionRawMO transactionRawMO = new TransactionRawMO().fromProto(proto);
			transactionRawMO.setChainid(new InitDatastore(ctx).getChainId());
			page.addData(transactionRawMO);
		});
		page.setBookmark(bookMark);
		return page;
	}
}
