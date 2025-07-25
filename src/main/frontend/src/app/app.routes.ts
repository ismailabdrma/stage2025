import type { Routes } from "@angular/router"
import { authGuard } from "./core/guards/auth.guard"

export const routes: Routes = [
    {
        path: "",
        loadComponent: () => import("./features/home/home.component").then((m) => m.HomeComponent),
    },
    {
        path: "auth",
        loadChildren: () => import("./features/auth/auth.routes").then((m) => m.authRoutes),
    },
    {
        path: "products",
        loadChildren: () => import("./features/products/products.routes").then((m) => m.productRoutes),
    },
    {
        path: "cart",
        loadComponent: () => import("./features/cart/cart.component").then((m) => m.CartComponent),
        canActivate: [authGuard],
    },
    {
        path: "checkout",
        loadComponent: () => import("./features/checkout/checkout.component").then((m) => m.CheckoutComponent),
        canActivate: [authGuard],
    },
    {
        path: "profile",
        loadChildren: () => import("./features/profile/profile.routes").then((m) => m.profileRoutes),
        canActivate: [authGuard],
    },
    {
        path: "**",
        redirectTo: "",
    },
]
