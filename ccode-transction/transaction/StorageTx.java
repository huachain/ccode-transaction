package org.hua.chain.ccode.transaction;

import java.math.BigInteger;

import org.hua.chain.ccode.core.TransactionType;
import org.hua.chain.ccode.core.datastore.AddressDatastore;
import org.hua.chain.ccode.core.datastore.InitDatastore;
import org.hua.chain.ccode.core.datastore.TransactionWrapper;
import org.hua.chain.ccode.core.datastore.TxStorageDatastore;
import org.hua.chain.ccode.core.datastore.proto.Address.AddressState;
import org.hua.chain.ccode.core.datastore.proto.TxStorage.StorageTransaction;
import org.hua.chain.ccode.core.model.Pagination;
import org.hua.chain.ccode.core.model.TransactionStorageMO;
import org.hua.chain.ccode.core.model.TransactionTransferMO;
import org.hua.chain.contract.Context;
import org.hua.chain.contract.ContractInterface;
import org.hua.chain.contract.annotation.Info;
import org.hua.chain.contract.annotation.Transaction;
import org.hua.chain.platform.ChainRunningException;

@org.hua.chain.contract.annotation.Contract("storageTx")
public class StorageTx implements ContractInterface{
	
	@Transaction
	public long storageNonce(Context ctx,String addr) {
		AddressDatastore addressDs = new AddressDatastore(ctx);
		AddressState address = addressDs.getAddressState(addr);
		if(address == null) {
			return 0;
		}
		return address.getStoragenonce();
	}
	
	@Transaction
	public TransactionStorageMO storageTx(Context ctx, String txhash) {
		TxStorageDatastore storageDs = new TxStorageDatastore(ctx);
		TransactionWrapper<StorageTransaction> storage = storageDs.getStorageTx(txhash);
		if(storage == null) {
			throw new ChainRunningException("storage tx "+txhash+" is not exists");
		}
		TransactionStorageMO transactionStorageMO = new TransactionStorageMO(storage);
		transactionStorageMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
		return transactionStorageMO;
	}
	
	@Transaction
	public Pagination<TransactionStorageMO> storageTxes(Context ctx, int pageSize, String bookmark) {
		TxStorageDatastore storageDs = new TxStorageDatastore(ctx);
		Pagination<TransactionStorageMO> page = new Pagination<>();
		String bookMark = storageDs.storagePage(pageSize, bookmark, (hash, wrapper)->{
			TransactionStorageMO transactionStorageMO = new TransactionStorageMO(wrapper);
			transactionStorageMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
			page.addData(transactionStorageMO);
		});
		page.setBookmark(bookMark);
		return page;
	}
	
	@Transaction
	public Pagination<TransactionStorageMO> addrStorageTxes(Context ctx,String addr,int pageSize,String bookmark) {
		TxStorageDatastore storageDs = new TxStorageDatastore(ctx);
		Pagination<TransactionStorageMO> page = new Pagination<>();
		String bookMark = storageDs.addrStoragePage(addr, pageSize, bookmark, (hash, wrapper)->{
			TransactionStorageMO transactionStorageMO = new TransactionStorageMO(wrapper);
			transactionStorageMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
			page.addData(transactionStorageMO);
		});
		page.setBookmark(bookMark);
		return page;
	}
	
	@Transaction
	public TransactionStorageMO commitTransferTx(Context ctx,String storageData) {
		InitDatastore initDs = new InitDatastore(ctx);
		BigInteger chainId = initDs.getChainId();
		if(chainId == null) {
			throw new ChainRunningException("chainId is not initialized");
		}
		TxStorageDatastore storageDs = new TxStorageDatastore(ctx);
		TransactionWrapper<StorageTransaction> wrapper = storageDs.saveStorageTx(chainId, storageData);
		TransactionStorageMO transactionStorageMO = new TransactionStorageMO(wrapper);
		transactionStorageMO.getRaw().setChainid(new InitDatastore(ctx).getChainId());
		ctx.getStub().setEvent(TransactionType.storage.name(), TransactionTransferMO.toJson(transactionStorageMO).getBytes());
		return transactionStorageMO;
	}
}
