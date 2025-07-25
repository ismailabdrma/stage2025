export interface Product {
    id: number
    name: string
    description: string
    displayedPrice: number
    syncedStock?: number
    dynamicPrice?: number
    lastFetched?: string
    imageUrls: string[]
    categoryName: string
    supplierName: string
    externalProductId?: string
}

export interface Category {
    id: number
    name: string
    description: string
    active: boolean
}

export interface ProductFilter {
    categoryName?: string
    supplierName?: string
    minPrice?: number
    maxPrice?: number
    search?: string
}
