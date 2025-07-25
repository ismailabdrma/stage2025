import { Injectable, signal } from "@angular/core"
import type { HttpClient } from "@angular/common/http"
import type { Router } from "@angular/router"
import { type Observable, BehaviorSubject, tap } from "rxjs"
import type { User, LoginRequest, SignupRequest, JwtResponse, OtpVerifyRequest } from "../models/user.model"

@Injectable({
    providedIn: "root",
})
export class AuthService {
    private readonly API_URL = "http://localhost:8080/api/auth"
    private currentUserSubject = new BehaviorSubject<User | null>(null)
    private tokenKey = "marketplace_token"

    currentUser$ = this.currentUserSubject.asObservable()
    isAuthenticated = signal(false)

    constructor(
        private http: HttpClient,
        private router: Router,
    ) {
        this.loadUserFromStorage()
    }

    signup(request: SignupRequest): Observable<any> {
        return this.http.post(`${this.API_URL}/signup`, request)
    }

    login(request: LoginRequest): Observable<any> {
        return this.http.post(`${this.API_URL}/login`, request)
    }

    verifyLoginOtp(request: OtpVerifyRequest): Observable<JwtResponse> {
        return this.http.post<JwtResponse>(`${this.API_URL}/login/verify-otp`, request).pipe(
            tap((response) => {
                this.setToken(response.token)
                this.setCurrentUser({
                    id: 0,
                    username: response.username,
                    email: response.email,
                    role: response.role as "ADMIN" | "CLIENT",
                    active: true,
                })
            }),
        )
    }

    verifyEmail(request: OtpVerifyRequest): Observable<any> {
        return this.http.post(`${this.API_URL}/verify-email`, request)
    }

    resendOtp(email: string, type: string): Observable<any> {
        return this.http.post(`${this.API_URL}/resend-otp`, { email, type })
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post(`${this.API_URL}/forgot-password`, { email })
    }

    resetPassword(email: string, otp: string, newPassword: string): Observable<any> {
        return this.http.post(`${this.API_URL}/reset-password`, { email, otp, newPassword })
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey)
        this.currentUserSubject.next(null)
        this.isAuthenticated.set(false)
        this.router.navigate(["/"])
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey)
    }

    private setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token)
    }

    private setCurrentUser(user: User): void {
        this.currentUserSubject.next(user)
        this.isAuthenticated.set(true)
    }

    private loadUserFromStorage(): void {
        const token = this.getToken()
        if (token) {
            this.isAuthenticated.set(true)
        }
    }
}
