package edu.cornell.kfs.module.purap.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.purap.util.cxml.B2BShoppingCart;
import org.kuali.kfs.module.purap.util.cxml.Message;
import org.kuali.kfs.module.purap.util.cxml.PunchOutOrderMessage;
import org.kuali.kfs.module.purap.util.cxml.PunchOutOrderMessageHeader;

public enum CuB2BShoppingCartFixture {
	B2B_CART_USING_VENDOR_ID("200", // messageStatusCode
			"Success", // messageStatusText
			"parke", // buyerCookieText
			"500.00", // totalAmount
			"Business Purpose", // businessPurpose
			B2BShoppingCartItemFixture.B2B_ITEM_USING_VENDOR_ID // itemFixture
	);

	public String messageStatusCode;
	public String messageStatusText;
	public String buyerCookieText;
	public String totalAmount;
	public String businessPurpose;
	public List<B2BShoppingCartItemFixture> itemFixturesList;

	/**
	 * Constructs a B2BShoppingCartFixture with only one item.
	 * 
	 * @param messageStatusCode
	 * @param messageStatusText
	 * @param buyerCookieText
	 * @param totalAmount
	 * @param itemFixture
	 */
	private CuB2BShoppingCartFixture(String messageStatusCode,
			String messageStatusText, String buyerCookieText,
			String totalAmount, String businessPurpose,
			B2BShoppingCartItemFixture itemFixture) {
		this.messageStatusCode = messageStatusCode;
		this.messageStatusText = messageStatusText;
		this.buyerCookieText = buyerCookieText;
		this.totalAmount = totalAmount;
		this.businessPurpose = businessPurpose;
		itemFixturesList = new ArrayList<B2BShoppingCartItemFixture>();
		itemFixturesList.add(itemFixture);
	}

	/**
	 * Creates a B2BShoppingCart from this B2BShoppingCartFixture.
	 */
	public B2BShoppingCart createB2BShoppingCart() {
		B2BShoppingCart cart = new B2BShoppingCart();
		cart.setMessageStatusCode(messageStatusCode);
		cart.setMessageStatusText(messageStatusText);
		cart.setTotal(totalAmount);
		Message message = new Message();
		PunchOutOrderMessage punchOutOrderMessage = new PunchOutOrderMessage();
		PunchOutOrderMessageHeader header = new PunchOutOrderMessageHeader();
		header.setQuoteStatus(businessPurpose);
		punchOutOrderMessage.setPunchOutOrderMessageHeader(header);
		message.setPunchOutOrderMessage(punchOutOrderMessage);
		cart.setMessage(message);

		for (B2BShoppingCartItemFixture itemFixture : itemFixturesList) {
			cart.addShoppingCartItem(itemFixture.createB2BShoppingCartItem());
		}

		return cart;
	}
}
