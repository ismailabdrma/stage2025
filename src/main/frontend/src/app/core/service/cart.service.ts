import { Injectable } from "@angular/core"
import type { HttpClient } from "@angular/common/http"
import { type Observable, BehaviorSubject } from "rxjs"
import type { Cart, AddToCartRequest } from "../models/cart.model"

@Injectable({
    providedIn: "root",
})
export class CartService {
    private readonly API_URL = "http://localhost:8080/api/cart"
    private cartSubject = new BehaviorSubject<Cart | null>(null)

    cart$ = this.cartSubject.asObservable()

    constructor(private http: HttpClient) {}

    getCart(): Observable<Cart> {
        return this.http.get<Cart>(this.API_URL)
    }

    addToCart(request: AddToCartRequest): Observable<Cart> {
        return this.http.post<Cart>(`${this.API_URL}/add`, request)
    }

    updateQuantity(cartItemId: number, quantity: number): Observable<Cart> {
        return this.http.put<Cart>(`${this.API_URL}/item/${cartItemId}?quantity=${quantity}`, {})
    }

    removeItem(cartItemId: number): Observable<void> {
        return this.http.delete<void>(`${this.API_URL}/item/${cartItemId}`)
    }

    clearCart(): Observable<void> {
        return this.http.delete<void>(`${this.API_URL}/clear`)
    }

    updateCartState(cart: Cart): void {
        this.cartSubject.next(cart)
    }

    getCartItemsCount(): number {
        const cart = this.cartSubject.value
        return cart?.items.reduce((total, item) => total + item.quantity, 0) || 0
    }

    getCartTotal(): number {
        const cart = this.cartSubject.value
        return cart?.items.reduce((total, item) => total + item.quantity * item.unitPrice, 0) || 0
    }
}
