package org.hua.chain.ccode.transaction;

import java.util.ArrayList;
import java.util.List;

import org.hua.chain.ccode.core.datastore.AddressDatastore;
import org.hua.chain.ccode.core.datastore.proto.Address.AddressAsset;
import org.hua.chain.ccode.core.datastore.proto.Address.AddressState;
import org.hua.chain.ccode.core.model.AddressAssetMO;
import org.hua.chain.ccode.core.model.AddressInfoMO;
import org.hua.chain.ccode.core.model.AddressStateMO;
import org.hua.chain.ccode.core.model.Pagination;
import org.hua.chain.contract.Context;
import org.hua.chain.contract.ContractInterface;
import org.hua.chain.contract.annotation.Info;
import org.hua.chain.contract.annotation.Transaction;

@org.hua.chain.contract.annotation.Contract("address")
public class Address implements ContractInterface{
	@Transaction
	public long nonce(Context ctx,String addr) {
		AddressState address = getAddressState(ctx,addr);
		if(address == null) {
			return 0;
		}
		return address.getNonce();
	}
	
	@Transaction
	public String state(Context ctx,String addr) {
		AddressState address = getAddressState(ctx,addr);
		if(address == null) {
			return AddressState.State.nomal.name();
		}
		return address.getState().name();
	}
	
	private AddressState getAddressState(Context ctx, String addr) {
		AddressDatastore addressDs = new AddressDatastore(ctx);
		return addressDs.getAddressState(addr);
	}
	
	@Transaction
	public long balance(Context ctx,String addr,String symbol) {
		AddressDatastore addressDs = new AddressDatastore(ctx);
		AddressAsset asset = addressDs.getAddressAsset(addr, symbol);
		if(asset == null) {
			return 0;
		}
		return asset.getBalance();
	}
	
	@Transaction
	public AddressInfoMO addressInfo(Context ctx,String addr) {
		AddressDatastore addressDs = new AddressDatastore(ctx);
		AddressState address = addressDs.getAddressState(addr);
		if(address == null) {
			return new AddressInfoMO(new AddressStateMO(addr, 0, AddressState.State.nomal.name()), new ArrayList<>());
		}
		AddressStateMO state = new AddressStateMO().fromProto(address);
		List<AddressAssetMO> assets = new ArrayList<>();
		addressDs.getAddressAssets(addr, (eleKey, assetProto)->{
			assets.add(new AddressAssetMO().fromProto(assetProto));
		});
		return new AddressInfoMO(state, assets);
	}
	
	@Transaction
	public Pagination<AddressStateMO> addressPage(Context ctx,int pageSize,String bookmark){
		AddressDatastore addressDs = new AddressDatastore(ctx);
		Pagination<AddressStateMO> page = new Pagination<>();
		String bookMark = addressDs.addressStatePage(pageSize, bookmark, (eleKey,addressProto)->{
			page.addData(new AddressStateMO().fromProto(addressProto));
		});
		page.setBookmark(bookMark);
		return page;
	}
}
