export interface User {
    id: number
    username: string
    email: string
    firstName?: string
    lastName?: string
    phone?: string
    role: "ADMIN" | "CLIENT"
    active: boolean
}

export interface LoginRequest {
    identifier: string
    password: string
}

export interface SignupRequest {
    username: string
    email: string
    password: string
    role: "ADMIN" | "CLIENT"
}

export interface JwtResponse {
    token: string
    username: string
    email: string
    role: string
}

export interface OtpVerifyRequest {
    email: string
    otp: string
}
