package com.furryhub.petservices.service;

import com.furryhub.petservices.exception.ResourceNotFoundException;
import com.furryhub.petservices.model.dto.CartDto;
import com.furryhub.petservices.model.dto.CartItemDto;
import com.furryhub.petservices.model.entity.Cart;
import com.furryhub.petservices.model.entity.CartItem;
import com.furryhub.petservices.model.entity.Customer;
import com.furryhub.petservices.repository.CartItemRepository;
import com.furryhub.petservices.repository.CartRepository;
import com.furryhub.petservices.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CustomerRepository customerRepository;
    private final PackageService packageService;
    private final CartItemRepository cartItemRepository;

    /* IMPORTANT***********
    Using modelMapper to convert Entity class directly to DTO class and vice versa*/
    private final ModelMapper modelMapper;


    @Transactional
    public CartDto mergeLocalCart(String customerEmail, List<CartItemDto> clientItems) {
        //get customer details
        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        /*Find the existing cart associated with the customer
         * if not found create a new cart*/
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCustomer(customer);
                    return c;
                });

        // map existing items by packageId
        Map<Long, CartItem> existingByPackage = cart.getItems().stream()
                .collect(Collectors.toMap(CartItem::getPackageId, ci -> ci, (a, b) -> a));

        //Loop over items from frontend (clientItems)
        if (clientItems != null) {
            for (CartItemDto dto : clientItems) {
                if (dto == null || dto.getPackageId() == null || dto.getQty() == null || dto.getQty() <= 0)
                    continue;

                //Fetch the package details from DB
                var packageOpt = packageService.findById(dto.getPackageId());

                //Skip if the package doesn’t exist or isn’t available.
                if (packageOpt.isEmpty()) {
                    continue;
                }

                var servicePackage = packageOpt.get();
                if (!packageService.isAvailable(servicePackage, dto.getQty())) {
                    continue;
                }

                //get the package Price
                BigDecimal currentPrice = packageService.getCurrentPrice(servicePackage);

                //if present just increase the quantity of package
                CartItem present = existingByPackage.get(dto.getPackageId());
                if (present == null) {
                    CartItem newItem = new CartItem();
                    newItem.setPackageId(dto.getPackageId());
                    newItem.setQty(dto.getQty());
                    newItem.setUnitPrice(currentPrice);
                    newItem.setCart(cart);
                    cart.getItems().add(newItem);
                    existingByPackage.put(dto.getPackageId(), newItem);
                } else {// if not then add it as a new item in cart
                    present.setQty(present.getQty() + dto.getQty());
                    present.setUnitPrice(currentPrice);
                }
            }
        }

        // recalculation total Price
        BigDecimal total = cart.getItems().stream()
                .filter(i -> i.getUnitPrice() != null && i.getQty() != null)
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //set the total price
        cart.setTotal(total);

        //save the changes in DB
        Cart saved = cartRepository.save(cart);

        CartDto dto= modelMapper.map(saved,CartDto.class);
        dto.setUserId(customer.getId());
        return dto;
    }


    @Transactional(readOnly = true)
    public CartDto getCartForCustomer(String customerEmail) {
        //get customer details
       Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

       //get the cart from the DB
        Cart cart = cartRepository.findByCustomerId(customer.getId()).orElse(new Cart());

        CartDto dto= modelMapper.map(cart,CartDto.class);
        dto.setUserId(customer.getId());
        return dto;
    }


    @Transactional
    public CartDto addItemToCart(String customerEmail, CartItemDto itemDto) {
        if (itemDto == null || itemDto.getPackageId() == null || itemDto.getQty() == null || itemDto.getQty() <= 0) {
            throw new IllegalArgumentException("Invalid cart item");
        }

        //get customer details
        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        /*Find the existing cart associated with the customer
         if not found create a new cart*/
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCustomer(customer);
                    return c;
                });


        //find the package
        var packageOpt = packageService.findById(itemDto.getPackageId());
        if (packageOpt.isEmpty()) throw new ResourceNotFoundException("Package not found");
        var servicePackage = packageOpt.get();

        //check the availability of the package
        if (!packageService.isAvailable(servicePackage, itemDto.getQty())) {
            throw new IllegalStateException("Requested package not available");
        }

        //get the package price
        BigDecimal currentPrice = packageService.getCurrentPrice(servicePackage);

        //check if the same package is added again
        Optional<CartItem> presentOpt = cart.getItems().stream()
                .filter(ci -> Objects.equals(ci.getPackageId(), itemDto.getPackageId()))
                .findFirst();

        //if present just increase the quantity of package
        if (presentOpt.isPresent()) {
            CartItem present = presentOpt.get();
            present.setQty(present.getQty() + itemDto.getQty());
            present.setUnitPrice(currentPrice);
        } else { // if not then add it as a new item in cart
            CartItem newItem = new CartItem();
            newItem.setPackageId(itemDto.getPackageId());
            newItem.setQty(itemDto.getQty());
            newItem.setUnitPrice(currentPrice);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // calculating total price of cart
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //set the total price
        cart.setTotal(total);

        //save the cart details to DB
        Cart saved = cartRepository.save(cart);

        CartDto dto = modelMapper.map(saved, CartDto.class);
        dto.setUserId(saved.getCustomer().getId());
        return dto;
    }


    @Transactional
    public void clearCartForCustomer(String customerEmail) {
        //get customer details
        Customer customer = customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        //Find the cart associated with Customer and clear all the items
        Cart cart = cartRepository.findByCustomerId(customer.getId()).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cart.setTotal(BigDecimal.ZERO);
            cartRepository.save(cart);
        }
    }

    @Transactional
    public void clearSpecificItem(String customerEmail,Long PackageId){
        //get customer details
        Customer customer=customerRepository.findByUser_Email(customerEmail)
                .orElseThrow(()-> new ResourceNotFoundException("Customer not found"));

        //get package details and check if package exists
        var packageOpt=packageService.findById(PackageId);
        if(packageOpt.isEmpty()){
            throw new ResourceNotFoundException("Package not Found");
        }

        //get the cart associated with customers
        Cart cart=cartRepository.findByCustomerId(customer.getId()).orElse(null);
        if(cart==null){
            return;
        }

        //find the item which needs to be removed
        CartItem cartItemToRemove=null;
        for(CartItem Item: cart.getItems() ){
            if(Item.getPackageId()!=null && Item.getPackageId().equals(PackageId)){
                cartItemToRemove=Item;
                break;
            }
        }

        if(cartItemToRemove==null){
            throw new ResourceNotFoundException("Item doesn't exist in Cart");
        }

        //delete the cart Item from cart
        cart.getItems().remove(cartItemToRemove);


        //recalculate the total Price of cart
        BigDecimal Total=cart.getItems().stream()
                .filter(i-> i.getUnitPrice()!=null && i.getQty()!=null)
                .map(i-> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        cart.setTotal(Total);

        //if cart is empty just delete the cart
        if(cart.getItems().isEmpty()){
            cartRepository.delete(cart);
        }else {// if not then save the cart details
            cartRepository.save(cart);
        }

    }
}
