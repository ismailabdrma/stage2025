import { Component, type OnInit, signal } from "@angular/core"
import { CommonModule } from "@angular/common"
import { RouterModule, type Router } from "@angular/router"
import { FormsModule } from "@angular/forms"
import type { AuthService } from "../../../core/services/auth.service"
import type { CartService } from "../../../core/services/cart.service"
import type { User } from "../../../core/models/user.model"

@Component({
    selector: "app-header",
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    template: `
        <header class="bg-white shadow-soft sticky top-0 z-50">
            <div class="container mx-auto px-4">
                <div class="flex items-center justify-between h-16">
                    <!-- Logo -->
                    <div class="flex items-center">
                        <a routerLink="/" class="flex items-center space-x-2">
                            <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                                <span class="text-white font-bold text-lg">M</span>
                            </div>
                            <span class="text-xl font-bold text-dark">Marketplace</span>
                        </a>
                    </div>

                    <!-- Search Bar -->
                    <div class="flex-1 max-w-xl mx-8">
                        <div class="relative">
                            <input
                                    type="text"
                                    [(ngModel)]="searchQuery"
                                    (keyup.enter)="onSearch()"
                                    placeholder="Rechercher des produits..."
                                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent"
                            >
                            <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <!-- Navigation -->
                    <nav class="flex items-center space-x-6">
                        <!-- Cart -->
                        <a routerLink="/cart" class="relative p-2 text-gray-600 hover:text-primary transition-colors">
                            <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l-1.5 6m0 0h9m-9 0V19a2 2 0 002 2h7a2 2 0 002-2v-4.5M9 7h6"></path>
                            </svg>
                            @if (cartItemsCount() > 0) {
                                <span class="absolute -top-1 -right-1 bg-secondary text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                  {{ cartItemsCount() }}
                </span>
                            }
                        </a>

                        <!-- User Menu -->
                        @if (isAuthenticated()) {
                            <div class="relative" (click)="toggleUserMenu()">
                                <button class="flex items-center space-x-2 text-gray-600 hover:text-primary transition-colors">
                                    <div class="w-8 h-8 bg-primary rounded-full flex items-center justify-center">
                    <span class="text-white text-sm font-medium">
                      {{ currentUser()?.username?.charAt(0).toUpperCase() }}
                    </span>
                                    </div>
                                    <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                                    </svg>
                                </button>

                                @if (showUserMenu()) {
                                    <div class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-strong py-2 z-50">
                                        <a routerLink="/profile" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                                            Mon Profil
                                        </a>
                                        <a routerLink="/profile/orders" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                                            Mes Commandes
                                        </a>
                                        <hr class="my-1">
                                        <button (click)="logout()" class="block w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                                            Déconnexion
                                        </button>
                                    </div>
                                }
                            </div>
                        } @else {
                            <div class="flex items-center space-x-4">
                                <a routerLink="/auth/login" class="text-gray-600 hover:text-primary transition-colors">
                                    Connexion
                                </a>
                                <a routerLink="/auth/register" class="btn-primary">
                                    Inscription
                                </a>
                            </div>
                        }
                    </nav>
                </div>
            </div>
        </header>
    `,
})
export class HeaderComponent implements OnInit {
    searchQuery = ""
    showUserMenu = signal(false)
    isAuthenticated = signal(false)
    currentUser = signal<User | null>(null)
    cartItemsCount = signal(0)

    constructor(
        private authService: AuthService,
        private cartService: CartService,
        private router: Router,
    ) {}

    ngOnInit() {
        this.authService.currentUser$.subscribe((user: User | null) => {
            this.currentUser.set(user)
            this.isAuthenticated.set(!!user)
        })

        this.cartService.cart$.subscribe((cart: any) => {
            const count = cart?.items.reduce((total: number, item: any) => total + item.quantity, 0) || 0
            this.cartItemsCount.set(count)
        })

        if (this.isAuthenticated()) {
            this.cartService.getCart().subscribe((cart: any) => {
                this.cartService.updateCartState(cart)
            })
        }
    }

    onSearch() {
        if (this.searchQuery.trim()) {
            this.router.navigate(["/products"], {
                queryParams: { search: this.searchQuery.trim() },
            })
        }
    }

    toggleUserMenu() {
        this.showUserMenu.update((show) => !show)
    }

    logout() {
        this.authService.logout()
        this.showUserMenu.set(false)
    }
}
