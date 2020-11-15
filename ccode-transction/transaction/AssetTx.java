package org.hua.chain.ccode.transaction;

import java.math.BigInteger;

import org.hua.chain.ccode.core.TransactionType;
import org.hua.chain.ccode.core.datastore.InitDatastore;
import org.hua.chain.ccode.core.datastore.TransactionWrapper;
import org.hua.chain.ccode.core.datastore.TxAssetDatastore;
import org.hua.chain.ccode.core.datastore.proto.TxAsset.TransferTransaction;
import org.hua.chain.ccode.core.model.Pagination;
import org.hua.chain.ccode.core.model.TransactionTransferMO;
import org.hua.chain.contract.Context;
import org.hua.chain.contract.ContractInterface;
import org.hua.chain.contract.annotation.Info;
import org.hua.chain.contract.annotation.Transaction;
import org.hua.chain.platform.ChainRunningException;

@org.hua.chain.contract.annotation.Contract("assetTx")
public class AssetTx implements ContractInterface {

	@Transaction
	public TransactionTransferMO transferTx(Context ctx, String txhash) {
		TxAssetDatastore assetDs = new TxAssetDatastore(ctx);
		TransactionWrapper<TransferTransaction> transfer = assetDs.getTransferTx(txhash);
		if(transfer == null) {
			throw new ChainRunningException("transfer tx "+txhash+" is not exists");
		}
		TransactionTransferMO transactionTransferMO = new TransactionTransferMO(transfer);
		transactionTransferMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
		return transactionTransferMO;
	}
	
	@Transaction
	public Pagination<TransactionTransferMO> transferTxes(Context ctx,int pageSize,String bookmark) {
		TxAssetDatastore assetDs = new TxAssetDatastore(ctx);
		Pagination<TransactionTransferMO> page = new Pagination<>();
		String bookMark = assetDs.transferPage(pageSize, bookmark, (hash, wrapper)->{
			TransactionTransferMO transactionTransferMO = new TransactionTransferMO(wrapper);
			transactionTransferMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
			page.addData(transactionTransferMO);
		});
		page.setBookmark(bookMark);
		return page;
	}
	
	@Transaction
	public Pagination<TransactionTransferMO> addrTransferTxes(Context ctx,String addr,int pageSize,String bookmark) {
		TxAssetDatastore assetDs = new TxAssetDatastore(ctx);
		Pagination<TransactionTransferMO> page = new Pagination<>();
		String bookMark = assetDs.addrTransferPage(addr, pageSize, bookmark, (hash, wrapper)->{
			TransactionTransferMO transactionTransferMO = new TransactionTransferMO(wrapper);
			transactionTransferMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
			page.addData(transactionTransferMO);
		});
		page.setBookmark(bookMark);
		return page;
	}
	
	@Transaction
	public TransactionTransferMO commitTransferTx(Context ctx,String transferData) {
		InitDatastore initDs = new InitDatastore(ctx);
		BigInteger chainId = initDs.getChainId();
		if(chainId == null) {
			throw new ChainRunningException("chainId is not initialized");
		}
		TxAssetDatastore assetDs = new TxAssetDatastore(ctx);
		TransactionWrapper<TransferTransaction> wrapper = assetDs.saveTransferTx(chainId, transferData);
		TransactionTransferMO transactionTransferMO = new TransactionTransferMO(wrapper);
		transactionTransferMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
		ctx.getStub().setEvent(TransactionType.transfer.name(), TransactionTransferMO.toJson(transactionTransferMO).getBytes());
		return transactionTransferMO;
	}
}
