package demo.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import demo.model.bean.CartItem;
import demo.model.bean.Product;
import demo.web.OverQuantityException;

/**
 * @author Ian Y.T Tsai(zanyking)
 * 		This class provides a representation of a shopping cart
 * 
 */
@Named("shoppingCart")
@SessionScoped
public class ShoppingCart implements Serializable {

	private static final long serialVersionUID = 464821961483850854L;

	private Map<Long, CartItem> items = 
		Collections.synchronizedMap(new LinkedHashMap<Long, CartItem>());

	@Inject
	private UserCredentialManager userCredentialManager;
	
	private String description;
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public List<CartItem> getItems() {
		return new ArrayList<CartItem>(items.values());
	}

	public CartItem getItem(long prodId) {
		return items.get(prodId);
	}

	private void add(CartItem item) {
		items.put(item.getProduct().getId(), item);
	}

	public void add(Product prod, int amount) throws OverQuantityException {

		CartItem item = this.getItem(prod.getId());
		validate(item, prod, amount);
		if (item == null) {
			this.add(item = new CartItem(
					userCredentialManager.getUser().getId(), prod));
			
			item.add(amount);
		} else {
			item.add(amount);
		}
	}

	private static void validate(CartItem item, Product prod, int amount)
			throws OverQuantityException {
		int oriAmount = item == null ? 0 : item.getAmount();
		int total = oriAmount + amount;
		if (total > prod.getQuantity()) {
			String errMesg = "too much: " + oriAmount + " + " + amount + " > "
					+ prod.getQuantity();
			throw new OverQuantityException(errMesg);
		}
	}

	public void remove(CartItem cartItem) {
		items.remove(cartItem);
	}

	public void clear() {
		items.clear();
	}

	public float getTotalPrice() {
		float subTotal = 0;
		for (CartItem item : items.values()) {
			subTotal += item.getProduct().getPrice() * item.getAmount();
		}
		return subTotal;
	}

}
