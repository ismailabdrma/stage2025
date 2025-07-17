package com.example.stage2025.service.impl;

import com.example.stage2025.dto.CartDto;
import com.example.stage2025.dto.CartItemDto;
import com.example.stage2025.entity.*;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.CartMapper;
import com.example.stage2025.mapper.CartItemMapper;
import com.example.stage2025.repository.*;
import com.example.stage2025.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Override
    public CartDto getCartByClientId(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec ID " + clientId));

        Cart cart = client.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setClient(client);
            cartRepository.save(cart);
        }

        return CartMapper.toDto(cart);
    }

    @Override
    public CartDto addItemToCart(Long clientId, CartItemDto itemDto) {
        Product product = productRepository.findById(itemDto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec ID " + itemDto.getProductId()));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec ID " + clientId));

        Cart cart = Optional.ofNullable(client.getCart()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setClient(client);
            return cartRepository.save(newCart);
        });

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPrice(product.getDisplayedPrice());
        cartItemRepository.save(item);

        return CartMapper.toDto(cart);
    }

    @Override
    public CartDto updateItemQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de panier introuvable"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        return CartMapper.toDto(item.getCart());
    }

    @Override
    public void removeItemFromCart(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item introuvable"));
        cartItemRepository.delete(item);
    }

    @Override
    public void clearCart(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        Cart cart = client.getCart();
        if (cart != null) {
            cartItemRepository.deleteAll(cart.getItems());
        }
    }
}
