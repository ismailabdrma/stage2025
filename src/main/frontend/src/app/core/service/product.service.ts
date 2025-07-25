import { Injectable } from "@angular/core"
import { type HttpClient, HttpParams } from "@angular/common/http"
import type { Observable } from "rxjs"
import type { Product, Category, ProductFilter } from "../models/product.model"

@Injectable({
    providedIn: "root",
})
export class ProductService {
    private readonly API_URL = "http://localhost:8080/api"

    constructor(private http: HttpClient) {}

    getProducts(filter?: ProductFilter): Observable<Product[]> {
        let params = new HttpParams()

        if (filter?.search) {
            params = params.set("search", filter.search)
        }
        if (filter?.categoryName) {
            params = params.set("category", filter.categoryName)
        }
        if (filter?.supplierName) {
            params = params.set("supplier", filter.supplierName)
        }
        if (filter?.minPrice) {
            params = params.set("minPrice", filter.minPrice.toString())
        }
        if (filter?.maxPrice) {
            params = params.set("maxPrice", filter.maxPrice.toString())
        }

        return this.http.get<Product[]>(`${this.API_URL}/products`, { params })
    }

    getProduct(id: number): Observable<Product> {
        return this.http.get<Product>(`${this.API_URL}/products/${id}`)
    }

    getProductsByCategory(categoryName: string): Observable<Product[]> {
        return this.http.get<Product[]>(`${this.API_URL}/products/category/${categoryName}`)
    }

    getCategories(): Observable<Category[]> {
        return this.http.get<Category[]>(`${this.API_URL}/categories`)
    }

    syncProductFromSupplier(productId: number): Observable<any> {
        return this.http.get(`${this.API_URL}/products/${productId}/sync`)
    }
}
